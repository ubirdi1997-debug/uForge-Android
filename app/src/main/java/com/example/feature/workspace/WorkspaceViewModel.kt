package com.example.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ForgeCodingAgentImpl
import com.example.data.terminal.TerminalEngine
import com.example.domain.model.ChatMessage
import com.example.domain.model.ForgeConstants
import com.example.domain.model.UserSession
import com.example.domain.repository.AgentContext
import com.example.domain.repository.AgentEvent
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CodingAgent
import com.example.domain.repository.ProjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkspaceViewModel(
    val projectRepository: ProjectRepository,
    val authRepository: AuthRepository,
    val codingAgent: CodingAgent = ForgeCodingAgentImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WorkspaceUiState(
            project = projectRepository.currentProject.value,
            user = authRepository.currentUser.value ?: UserSession(),
            mode = WorkspaceMode.Code
        )
    )
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private var toastJob: Job? = null
    private var pendingApprovalContinuation: ((Boolean) -> Unit)? = null

    init {
        viewModelScope.launch {
            projectRepository.currentProject.collect { proj ->
                _uiState.update { current ->
                    current.copy(
                        project = proj,
                        terminalSuggestions = TerminalEngine.getSuggestions(current.terminalInput, proj)
                    )
                }
            }
        }

        viewModelScope.launch {
            authRepository.currentUser.collect { usr ->
                _uiState.update { it.copy(user = usr ?: UserSession()) }
            }
        }

        // Reactively observe Room Database file hierarchy for current project
        viewModelScope.launch {
            projectRepository.currentProjectId.flatMapLatest { pid ->
                projectRepository.getProjectFileEntities(pid)
            }.collect { entities ->
                _uiState.update { it.copy(roomFiles = entities) }
            }
        }
    }

    fun onEvent(event: WorkspaceEvent) {
        when (event) {
            is WorkspaceEvent.ToggleDrawer -> {
                _uiState.update { it.copy(isDrawerOpen = !it.isDrawerOpen) }
            }

            is WorkspaceEvent.CloseDrawer -> {
                _uiState.update { it.copy(isDrawerOpen = false) }
            }

            is WorkspaceEvent.ToggleMode -> {
                _uiState.update {
                    val nextMode = if (it.mode == WorkspaceMode.Code) WorkspaceMode.Chat else WorkspaceMode.Code
                    it.copy(mode = nextMode)
                }
            }

            is WorkspaceEvent.OpenPreview -> {
                val proj = _uiState.value.project
                val updatedPreview = if (!proj.preview.isShared) {
                    proj.preview.copy(isShared = true)
                } else proj.preview
                projectRepository.updatePreview(proj.id, updatedPreview)
                _uiState.update { it.copy(isPreviewOpen = true) }
            }

            is WorkspaceEvent.ClosePreview -> {
                _uiState.update { it.copy(isPreviewOpen = false) }
            }

            is WorkspaceEvent.OpenTerminal -> {
                val proj = _uiState.value.project
                if (_uiState.value.terminalLogs.isEmpty()) {
                    val initialLogs = listOf(
                        TerminalEngine.OutputLine("Forge terminal · Aura completes as you type", "mt"),
                        TerminalEngine.OutputLine("Start with # to ask in plain words. Forge CLI: npm i -g @usafe/forge-cli", "mt")
                    )
                    _uiState.update {
                        it.copy(
                            isTerminalOpen = true,
                            terminalLogs = initialLogs,
                            terminalSuggestions = TerminalEngine.getSuggestions("", proj)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTerminalOpen = true,
                            terminalSuggestions = TerminalEngine.getSuggestions(it.terminalInput, proj)
                        )
                    }
                }
            }

            is WorkspaceEvent.CloseTerminal -> {
                _uiState.update { it.copy(isTerminalOpen = false) }
            }

            is WorkspaceEvent.SendMessage -> {
                sendMessage(event.text)
            }

            is WorkspaceEvent.SelectProject -> {
                projectRepository.selectProject(event.id)
                showToast("Switched to ${event.id}")
                _uiState.update { it.copy(isDrawerOpen = false) }
            }

            is WorkspaceEvent.OpenModal -> {
                _uiState.update { it.copy(activeModal = event.modal, isDrawerOpen = false) }
            }

            is WorkspaceEvent.DismissModal -> {
                _uiState.update { it.copy(activeModal = ActiveModal.None) }
            }

            is WorkspaceEvent.SetPreviewMode -> {
                val proj = _uiState.value.project
                val summary = if (event.mode == "html") {
                    "${proj.name} · 1 links · headings: ${proj.name}"
                } else {
                    "URL ${proj.preview.url} (embedded, page text not readable)"
                }
                val next = proj.preview.copy(mode = event.mode, summary = summary)
                projectRepository.updatePreview(proj.id, next)
            }

            is WorkspaceEvent.SetPreviewUrl -> {
                val proj = _uiState.value.project
                val next = proj.preview.copy(
                    url = event.url,
                    summary = "URL ${event.url} (embedded, page text not readable)"
                )
                projectRepository.updatePreview(proj.id, next)
                if (proj.preview.isShared) {
                    projectRepository.addMessage(
                        proj.id,
                        ChatMessage.Assistant("m_${System.currentTimeMillis()}", "👁 Preview updated: ${next.summary}")
                    )
                }
            }

            is WorkspaceEvent.TogglePreviewShared -> {
                val proj = _uiState.value.project
                val nextShared = !proj.preview.isShared
                val next = proj.preview.copy(isShared = nextShared)
                projectRepository.updatePreview(proj.id, next)
                showToast(if (nextShared) "Agent can see the preview" else "Preview not shared")
                if (nextShared) {
                    projectRepository.addMessage(
                        proj.id,
                        ChatMessage.Assistant("m_${System.currentTimeMillis()}", "👁 Preview shared: ${next.summary}")
                    )
                }
            }

            is WorkspaceEvent.SwitchOpenFile -> {
                projectRepository.selectOpenFile(_uiState.value.project.id, event.filename)
                _uiState.update {
                    it.copy(
                        mode = WorkspaceMode.Code,
                        isFileExplorerSidebarOpen = false,
                        isDrawerOpen = false
                    )
                }
                showToast("Opened ${event.filename}")
            }

            is WorkspaceEvent.UpdateFileText -> {
                projectRepository.updateFileContent(_uiState.value.project.id, event.filename, event.content)
            }

            is WorkspaceEvent.CreateNewFile -> {
                val targetPath = if (event.parentDir.isNotBlank()) "${event.parentDir.trimEnd('/')}/${event.filename.trimStart('/')}" else event.filename.trim()
                projectRepository.createFile(_uiState.value.project.id, targetPath, "")
                showToast("Created $targetPath")
            }

            is WorkspaceEvent.CreateNewFolder -> {
                val targetDir = if (event.parentDir.isNotBlank()) "${event.parentDir.trimEnd('/')}/${event.folderName.trimStart('/')}" else event.folderName.trim()
                projectRepository.createFolder(_uiState.value.project.id, targetDir)
                showToast("Created directory $targetDir")
            }

            is WorkspaceEvent.DeleteFile -> {
                projectRepository.deleteFile(_uiState.value.project.id, event.path)
                showToast("Deleted ${event.path}")
            }

            is WorkspaceEvent.RenameFile -> {
                projectRepository.renameFile(_uiState.value.project.id, event.oldPath, event.newPath)
                showToast("Renamed to ${event.newPath}")
            }

            is WorkspaceEvent.ToggleFolderExpanded -> {
                projectRepository.toggleFolderExpanded(_uiState.value.project.id, event.path, event.isExpanded)
            }

            is WorkspaceEvent.UpdateTerminalInput -> {
                val proj = _uiState.value.project
                _uiState.update {
                    it.copy(
                        terminalInput = event.input,
                        terminalSuggestions = TerminalEngine.getSuggestions(event.input, proj)
                    )
                }
            }

            is WorkspaceEvent.ExecuteTerminal -> {
                executeTerminalCommand(event.command)
            }

            is WorkspaceEvent.ApproveCommand -> {
                pendingApprovalContinuation?.invoke(event.approve)
                pendingApprovalContinuation = null
                _uiState.update { it.copy(pendingApprovalCommand = null) }
            }

            is WorkspaceEvent.ShowToast -> {
                showToast(event.text)
            }

            is WorkspaceEvent.DismissToast -> {
                _uiState.update { it.copy(toastMessage = null) }
            }

            is WorkspaceEvent.OpenPlugins -> {
                _uiState.update { it.copy(isPluginsPageOpen = true, isDrawerOpen = false) }
            }

            is WorkspaceEvent.ClosePlugins -> {
                _uiState.update { it.copy(isPluginsPageOpen = false) }
            }

            is WorkspaceEvent.OpenTools -> {
                _uiState.update { it.copy(isToolsPageOpen = true, isDrawerOpen = false) }
            }

            is WorkspaceEvent.CloseTools -> {
                _uiState.update { it.copy(isToolsPageOpen = false) }
            }

            is WorkspaceEvent.OpenFileExplorerSidebar -> {
                _uiState.update { it.copy(isFileExplorerSidebarOpen = true, isDrawerOpen = false) }
            }

            is WorkspaceEvent.CloseFileExplorerSidebar -> {
                _uiState.update { it.copy(isFileExplorerSidebarOpen = false) }
            }

            is WorkspaceEvent.ToggleFileExplorerSidebar -> {
                _uiState.update { it.copy(isFileExplorerSidebarOpen = !it.isFileExplorerSidebarOpen, isDrawerOpen = false) }
            }

            is WorkspaceEvent.CycleAgent -> {
                val proj = _uiState.value.project
                val list = listOf("Forge agent") + proj.agents.map { it.name }
                val idx = list.indexOf(proj.agent)
                val next = list[(idx + 1) % list.size]
                projectRepository.updateAgent(proj.id, next)
            }
        }
    }

    private fun showToast(msg: String) {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = msg) }
        toastJob = viewModelScope.launch {
            delay(1800)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    private fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isAgentBusy) return

        val proj = _uiState.value.project
        val userMsg = ChatMessage.User("u_${System.currentTimeMillis()}", trimmed)
        projectRepository.addMessage(proj.id, userMsg)

        viewModelScope.launch {
            authRepository.consumeTokens((800..1500).random().toLong())
            _uiState.update { it.copy(isAgentBusy = true) }

            val context = AgentContext(
                project = proj,
                currentFile = proj.openFile,
                isPreviewShared = proj.preview.isShared,
                previewSummary = proj.preview.summary
            )

            codingAgent.sendMessage(proj.id, trimmed, context).collect { event ->
                when (event) {
                    is AgentEvent.AssistantMessage -> {
                        projectRepository.addMessage(
                            proj.id,
                            ChatMessage.Assistant("a_${System.currentTimeMillis()}", event.text)
                        )
                    }

                    is AgentEvent.ToolCallStarted -> {
                        projectRepository.addMessage(
                            proj.id,
                            ChatMessage.ToolCall(
                                id = "t_${System.currentTimeMillis()}",
                                tool = event.tool,
                                icon = event.icon,
                                argument = event.argument,
                                status = "run",
                                result = ""
                            )
                        )
                    }

                    is AgentEvent.ToolCallCompleted -> {
                        val lastTool = projectRepository.currentProject.value.messages.filterIsInstance<ChatMessage.ToolCall>().lastOrNull()
                        if (lastTool != null) {
                            projectRepository.updateMessage(
                                proj.id,
                                lastTool.copy(status = "ok", result = event.result)
                            )
                        }
                    }

                    is AgentEvent.AskCommandApproval -> {
                        val askMsg = ChatMessage.AskApproval(
                            id = "ask_${System.currentTimeMillis()}",
                            command = event.command,
                            status = "wait"
                        )
                        projectRepository.addMessage(proj.id, askMsg)
                        _uiState.update { it.copy(pendingApprovalCommand = event.command) }

                        val approved = kotlinx.coroutines.suspendCancellableCoroutine<Boolean> { cont ->
                            pendingApprovalContinuation = { ok ->
                                if (cont.isActive) cont.resumeWith(Result.success(ok))
                            }
                        }

                        projectRepository.updateMessage(
                            proj.id,
                            askMsg.copy(status = if (approved) "ok" else "no")
                        )

                        if (approved) {
                            runInlineAgentTerminal(proj.id, event.command)
                        }
                    }

                    is AgentEvent.FileChanged -> {
                        projectRepository.updateFileContent(proj.id, event.filename, event.newContent)
                    }

                    is AgentEvent.Completed -> {
                        _uiState.update { it.copy(isAgentBusy = false) }
                    }

                    is AgentEvent.Error -> {
                        projectRepository.addMessage(
                            proj.id,
                            ChatMessage.Assistant("e_${System.currentTimeMillis()}", "Error: ${event.message}")
                        )
                        _uiState.update { it.copy(isAgentBusy = false) }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun runInlineAgentTerminal(projectId: String, command: String) {
        val lines = mutableListOf<Pair<String, String>>()
        val termMsg = ChatMessage.Terminal(
            id = "term_${System.currentTimeMillis()}",
            lines = lines,
            isDone = false
        )
        projectRepository.addMessage(projectId, termMsg)

        val subCommands = command.split("&&").map { it.trim() }
        for (sub in subCommands) {
            lines.add("$ $sub" to "cm")
            projectRepository.updateMessage(projectId, termMsg.copy(lines = lines.toList()))
            delay(250)

            val out = TerminalEngine.execute(
                sub,
                projectRepository.currentProject.value,
                _uiState.value.user,
                onFileCreated = { fn, text -> projectRepository.createFile(projectId, fn, text) },
                onFileDeleted = { fn -> projectRepository.deleteFile(projectId, fn) },
                onFolderCreated = { dir -> projectRepository.createFolder(projectId, dir) },
                onFileModified = { fn, text -> projectRepository.updateFileContent(projectId, fn, text) },
                onGitUpdate = { dirty, staged, log, ahead ->
                    val proj = projectRepository.currentProject.value
                    projectRepository.updateProject(proj.copy(git = proj.git.copy(dirty = dirty, staged = staged, log = log, ahead = ahead)))
                }
            )

            out?.forEach { line ->
                lines.add(line.text to line.style)
                projectRepository.updateMessage(projectId, termMsg.copy(lines = lines.toList()))
                delay(180)
            }
        }

        projectRepository.updateMessage(projectId, termMsg.copy(lines = lines.toList(), isDone = true))
    }

    private fun executeTerminalCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return

        val proj = _uiState.value.project
        val user = _uiState.value.user

        val header = TerminalEngine.OutputLine("${proj.name} $ $trimmed", "cm")
        val currentLogs = _uiState.value.terminalLogs.toMutableList()
        currentLogs.add(header)

        var finalCommand = trimmed
        if (trimmed.startsWith("#") || trimmed.startsWith("?")) {
            val suggestions = TerminalEngine.getSuggestions(trimmed, proj)
            val top = suggestions.firstOrNull()?.first
            if (top != null) {
                currentLogs.add(TerminalEngine.OutputLine("Aura Code → $top", "mt"))
                finalCommand = top
            }
        }

        val result = TerminalEngine.execute(
            finalCommand,
            proj,
            user,
            onFileCreated = { fn, text ->
                projectRepository.createFile(proj.id, fn, text)
            },
            onFileDeleted = { fn ->
                projectRepository.deleteFile(proj.id, fn)
            },
            onFolderCreated = { dir ->
                projectRepository.createFolder(proj.id, dir)
            },
            onFileModified = { fn, text ->
                projectRepository.updateFileContent(proj.id, fn, text)
            },
            onGitUpdate = { dirty, staged, log, ahead ->
                projectRepository.updateProject(
                    proj.copy(git = proj.git.copy(dirty = dirty, staged = staged, log = log, ahead = ahead))
                )
            }
        )

        if (result == null) {
            currentLogs.clear()
        } else {
            currentLogs.addAll(result)
        }

        _uiState.update {
            it.copy(
                terminalLogs = currentLogs,
                terminalInput = "",
                terminalSuggestions = TerminalEngine.getSuggestions("", proj)
            )
        }
    }
}
