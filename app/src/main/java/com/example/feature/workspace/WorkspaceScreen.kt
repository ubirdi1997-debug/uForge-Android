package com.example.feature.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ProjectFileEntity
import com.example.feature.auth.AuthScreen
import com.example.feature.chat.ChatPane
import com.example.feature.editor.ViewCodePane
import com.example.feature.editor.components.FileExplorerSidebar
import com.example.feature.modals.ForgeModalHost
import com.example.feature.onboarding.OnboardingOverlay
import com.example.feature.plugins.PluginsPage
import com.example.feature.preview.PreviewOverlay
import com.example.feature.terminal.TerminalOverlay
import com.example.feature.tools.ToolsPage
import com.example.feature.workspace.components.FooterModeToggle
import com.example.feature.workspace.components.NavigationHubDrawer
import com.example.feature.workspace.components.TopWorkspaceBar
import com.example.ui.theme.GlossyBorderStroke
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate950

@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val allProjects by viewModel.projectRepository.projects.collectAsState()

    BackHandler(
        enabled = state.activeModal != ActiveModal.None ||
                state.isFileExplorerSidebarOpen ||
                state.isDrawerOpen ||
                state.isPreviewOpen ||
                state.isTerminalOpen ||
                state.isPluginsPageOpen ||
                state.isToolsPageOpen
    ) {
        when {
            state.activeModal != ActiveModal.None -> viewModel.onEvent(WorkspaceEvent.DismissModal)
            state.isFileExplorerSidebarOpen -> viewModel.onEvent(WorkspaceEvent.CloseFileExplorerSidebar)
            state.isDrawerOpen -> viewModel.onEvent(WorkspaceEvent.CloseDrawer)
            state.isPreviewOpen -> viewModel.onEvent(WorkspaceEvent.ClosePreview)
            state.isTerminalOpen -> viewModel.onEvent(WorkspaceEvent.CloseTerminal)
            state.isPluginsPageOpen -> viewModel.onEvent(WorkspaceEvent.ClosePlugins)
            state.isToolsPageOpen -> viewModel.onEvent(WorkspaceEvent.CloseTools)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        containerColor = Slate950,
        topBar = {
            TopWorkspaceBar(
                projectName = state.project.name,
                isGitHubSynced = state.project.tools.gh.on,
                mode = state.mode,
                onMenuClick = { viewModel.onEvent(WorkspaceEvent.ToggleDrawer) },
                onFilesClick = { viewModel.onEvent(WorkspaceEvent.ToggleFileExplorerSidebar) },
                onActionClick = {
                    if (state.mode == WorkspaceMode.Chat) {
                        viewModel.onEvent(WorkspaceEvent.OpenPreview)
                    } else {
                        viewModel.onEvent(WorkspaceEvent.OpenTerminal)
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            FooterModeToggle(
                mode = state.mode,
                onToggle = { viewModel.onEvent(WorkspaceEvent.ToggleMode) },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Workspace Body: Animated flip between Chat and View Code
            AnimatedContent(
                targetState = state.mode,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "workspace_mode_transition"
            ) { mode ->
                when (mode) {
                    WorkspaceMode.Chat -> {
                        ChatPane(
                            project = state.project,
                            isAgentBusy = state.isAgentBusy,
                            onSendMessage = { viewModel.onEvent(WorkspaceEvent.SendMessage(it)) },
                            onOpenModelSelector = { viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.ModelSelector)) },
                            onCycleAgent = { viewModel.onEvent(WorkspaceEvent.CycleAgent) },
                            onApproveCommand = { viewModel.onEvent(WorkspaceEvent.ApproveCommand(it)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WorkspaceMode.Code -> {
                        ViewCodePane(
                            project = state.project,
                            roomFiles = state.roomFiles,
                            terminalLogs = state.terminalLogs,
                            terminalInput = state.terminalInput,
                            terminalSuggestions = state.terminalSuggestions,
                            onOpenFile = { viewModel.onEvent(WorkspaceEvent.SwitchOpenFile(it)) },
                            onFileContentChange = { fn, text ->
                                viewModel.onEvent(WorkspaceEvent.UpdateFileText(fn, text))
                            },
                            onAddFolder = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFolder(parent))) },
                            onAddFile = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFile(parent))) },
                            onDeleteFile = { viewModel.onEvent(WorkspaceEvent.DeleteFile(it)) },
                            onRenameFile = { oldPath, newPath -> viewModel.onEvent(WorkspaceEvent.RenameFile(oldPath, newPath)) },
                            onToggleFolderExpanded = { path, isExp -> viewModel.onEvent(WorkspaceEvent.ToggleFolderExpanded(path, isExp)) },
                            onUploadFile = { viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.UploadFile)) },
                            onTerminalInputChange = { viewModel.onEvent(WorkspaceEvent.UpdateTerminalInput(it)) },
                            onExecuteTerminal = { viewModel.onEvent(WorkspaceEvent.ExecuteTerminal(it)) },
                            onOpenFileExplorerSidebar = { viewModel.onEvent(WorkspaceEvent.OpenFileExplorerSidebar) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Dedicated File Explorer Sidebar Component
            FileExplorerSidebar(
                isOpen = state.isFileExplorerSidebarOpen,
                projectName = state.project.name,
                files = if (state.roomFiles.isNotEmpty()) state.roomFiles else state.project.files.map { (p, c) ->
                    ProjectFileEntity(
                        id = "${state.project.id}:$p",
                        projectId = state.project.id,
                        path = p,
                        name = p.substringAfterLast('/'),
                        parentPath = if (p.contains('/')) p.substringBeforeLast('/') else "",
                        isDirectory = false,
                        content = c,
                        extension = p.substringAfterLast('.', "")
                    )
                },
                selectedFile = state.project.openFile,
                onClose = { viewModel.onEvent(WorkspaceEvent.CloseFileExplorerSidebar) },
                onSelectFile = { viewModel.onEvent(WorkspaceEvent.SwitchOpenFile(it)) },
                onAddFile = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFile(parent))) },
                onAddFolder = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFolder(parent))) },
                onDeleteFile = { viewModel.onEvent(WorkspaceEvent.DeleteFile(it)) },
                onRenameFile = { old, new -> viewModel.onEvent(WorkspaceEvent.RenameFile(old, new)) },
                onToggleFolder = { path, isExp -> viewModel.onEvent(WorkspaceEvent.ToggleFolderExpanded(path, isExp)) },
                onUploadClick = { viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.UploadFile)) }
            )

            // Left Navigation Drawer Overlay (includes Room-backed project file explorer)
            NavigationHubDrawer(
                isOpen = state.isDrawerOpen,
                user = state.user,
                projects = allProjects,
                currentProject = state.project,
                roomFiles = state.roomFiles,
                onClose = { viewModel.onEvent(WorkspaceEvent.CloseDrawer) },
                onSelectProject = { viewModel.onEvent(WorkspaceEvent.SelectProject(it)) },
                onOpenFile = { viewModel.onEvent(WorkspaceEvent.SwitchOpenFile(it)) },
                onOpenModal = { viewModel.onEvent(WorkspaceEvent.OpenModal(it)) },
                onToggleTool = { toolKey ->
                    val proj = state.project
                    val tools = proj.tools
                    val updated = when (toolKey) {
                        "ssh" -> tools.copy(ssh = tools.ssh.copy(on = !tools.ssh.on))
                        "gh" -> tools.copy(gh = tools.gh.copy(on = !tools.gh.on))
                        "vault" -> tools.copy(vault = tools.vault.copy(on = !tools.vault.on))
                        else -> tools
                    }
                    viewModel.projectRepository.updateTools(proj.id, updated)
                    viewModel.onEvent(WorkspaceEvent.ShowToast("✓ Saved"))
                },
                onToggleByok = {
                    val proj = state.project
                    val nextOn = !proj.byok.on
                    viewModel.projectRepository.updateByok(proj.id, proj.byok.copy(on = nextOn))
                    viewModel.onEvent(WorkspaceEvent.ShowToast(if (nextOn) "BYOK enabled" else "BYOK disabled"))
                },
                onOpenPlugins = { viewModel.onEvent(WorkspaceEvent.OpenPlugins) },
                onOpenTools = { viewModel.onEvent(WorkspaceEvent.OpenTools) },
                onAddFile = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFile(parent))) },
                onAddFolder = { parent -> viewModel.onEvent(WorkspaceEvent.OpenModal(ActiveModal.NewFolder(parent))) },
                onDeleteFile = { viewModel.onEvent(WorkspaceEvent.DeleteFile(it)) },
                onRenameFile = { oldPath, newPath -> viewModel.onEvent(WorkspaceEvent.RenameFile(oldPath, newPath)) },
                onToggleFolderExpanded = { path, isExp -> viewModel.onEvent(WorkspaceEvent.ToggleFolderExpanded(path, isExp)) }
            )

            // Full-screen Overlays: Preview, Terminal, Plugins, Tools
            PreviewOverlay(
                isOpen = state.isPreviewOpen,
                project = state.project,
                onClose = { viewModel.onEvent(WorkspaceEvent.ClosePreview) },
                onSetMode = { viewModel.onEvent(WorkspaceEvent.SetPreviewMode(it)) },
                onSetUrl = { viewModel.onEvent(WorkspaceEvent.SetPreviewUrl(it)) },
                onToggleShare = { viewModel.onEvent(WorkspaceEvent.TogglePreviewShared) }
            )

            TerminalOverlay(
                isOpen = state.isTerminalOpen,
                projectName = state.project.name,
                logs = state.terminalLogs,
                input = state.terminalInput,
                suggestions = state.terminalSuggestions,
                onInputChange = { viewModel.onEvent(WorkspaceEvent.UpdateTerminalInput(it)) },
                onExecute = { viewModel.onEvent(WorkspaceEvent.ExecuteTerminal(it)) },
                onClose = { viewModel.onEvent(WorkspaceEvent.CloseTerminal) }
            )

            PluginsPage(
                isOpen = state.isPluginsPageOpen,
                project = state.project,
                onClose = { viewModel.onEvent(WorkspaceEvent.ClosePlugins) },
                onTogglePlugin = { viewModel.projectRepository.togglePlugin(state.project.id, it) },
                onToast = { viewModel.onEvent(WorkspaceEvent.ShowToast(it)) }
            )

            ToolsPage(
                isOpen = state.isToolsPageOpen,
                project = state.project,
                onClose = { viewModel.onEvent(WorkspaceEvent.CloseTools) },
                onToggleTool = { tid, en -> viewModel.projectRepository.toggleTool(state.project.id, tid, en) },
                onSetAllTools = { en -> viewModel.projectRepository.setAllTools(state.project.id, en) }
            )

            // Modals Host (New project, Ssh, Github, Vault, Model, Agent, Membership, File)
            ForgeModalHost(
                activeModal = state.activeModal,
                project = state.project,
                user = state.user,
                onDismiss = { viewModel.onEvent(WorkspaceEvent.DismissModal) },
                onCreateProject = { name, kind ->
                    viewModel.projectRepository.createProject(name, kind)
                },
                onSaveSsh = { ssh ->
                    viewModel.projectRepository.updateTools(state.project.id, state.project.tools.copy(ssh = ssh))
                },
                onSaveGitHub = { gh ->
                    viewModel.projectRepository.updateTools(state.project.id, state.project.tools.copy(gh = gh))
                },
                onSaveVault = { vault ->
                    viewModel.projectRepository.updateTools(state.project.id, state.project.tools.copy(vault = vault))
                },
                onSelectModel = { modelId ->
                    viewModel.projectRepository.updateModel(state.project.id, modelId)
                },
                onCreateAgent = { agent ->
                    viewModel.projectRepository.addCustomAgent(state.project.id, agent)
                },
                onCreateFile = { fn ->
                    viewModel.projectRepository.createFile(state.project.id, fn, "")
                },
                onCreateFolder = { folder ->
                    viewModel.projectRepository.createFolder(state.project.id, folder)
                },
                onUpgradePlan = { plan ->
                    kotlinx.coroutines.runBlocking {
                        viewModel.authRepository.updatePlan(plan)
                    }
                },
                onToast = { viewModel.onEvent(WorkspaceEvent.ShowToast(it)) }
            )

            // Auth Screen Overlay
            AuthScreen(
                isOpen = state.isAuthScreenOpen,
                authRepository = viewModel.authRepository,
                onAuthSuccess = { isSignup ->
                    if (isSignup) {
                        viewModel.onEvent(WorkspaceEvent.ShowToast("Welcome! Setting up workspace..."))
                    }
                },
                onToast = { viewModel.onEvent(WorkspaceEvent.ShowToast(it)) }
            )

            // Onboarding Overlay
            OnboardingOverlay(
                isOpen = state.isOnboardingOpen,
                onComplete = { newProject ->
                    viewModel.projectRepository.createProject(newProject.name)
                    viewModel.onEvent(WorkspaceEvent.ShowToast("Workspace ready"))
                }
            )

            // Lightweight In-app Toast / Feedback pill (`tst`)
            AnimatedVisibility(
                visible = state.toastMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp)
            ) {
                state.toastMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate100)
                            .border(GlossyBorderStroke, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("forge_toast")
                    ) {
                        Text(
                            text = msg,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = Slate950
                        )
                    }
                }
            }
        }
    }
}
