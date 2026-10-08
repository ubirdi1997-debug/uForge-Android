package com.example.feature.workspace

import com.example.core.database.ProjectFileEntity
import com.example.data.terminal.TerminalEngine
import com.example.domain.model.Project
import com.example.domain.model.UserSession

enum class WorkspaceMode {
    Chat,
    Code
}

sealed interface ActiveModal {
    data object None : ActiveModal
    data class NewProject(val initialName: String = "") : ActiveModal
    data object Ssh : ActiveModal
    data object GitHub : ActiveModal
    data object Vault : ActiveModal
    data object ModelSelector : ActiveModal
    data object AgentBuilder : ActiveModal
    data class Membership(val reason: String = "") : ActiveModal
    data object UploadFile : ActiveModal
    data class NewFile(val parentDir: String = "") : ActiveModal
    data class NewFolder(val parentDir: String = "") : ActiveModal
}

data class WorkspaceUiState(
    val project: Project,
    val user: UserSession,
    val roomFiles: List<ProjectFileEntity> = emptyList(),
    val mode: WorkspaceMode = WorkspaceMode.Code, // Blueprint defaults to code view initially
    val isDrawerOpen: Boolean = false,
    val isPreviewOpen: Boolean = false,
    val isTerminalOpen: Boolean = false,
    val isPluginsPageOpen: Boolean = false,
    val isToolsPageOpen: Boolean = false,
    val isFileExplorerSidebarOpen: Boolean = false,
    val activeModal: ActiveModal = ActiveModal.None,
    val toastMessage: String? = null,
    val isAgentBusy: Boolean = false,
    val pendingApprovalCommand: String? = null,
    val terminalLogs: List<TerminalEngine.OutputLine> = emptyList(),
    val terminalInput: String = "",
    val terminalSuggestions: List<Pair<String, String>> = emptyList(),
    val isOnboardingOpen: Boolean = false,
    val isAuthScreenOpen: Boolean = false
)

sealed interface WorkspaceEvent {
    data object ToggleDrawer : WorkspaceEvent
    data object CloseDrawer : WorkspaceEvent
    data object ToggleMode : WorkspaceEvent
    data object OpenPreview : WorkspaceEvent
    data object ClosePreview : WorkspaceEvent
    data object OpenTerminal : WorkspaceEvent
    data object CloseTerminal : WorkspaceEvent
    data class SendMessage(val text: String) : WorkspaceEvent
    data class SelectProject(val id: String) : WorkspaceEvent
    data class OpenModal(val modal: ActiveModal) : WorkspaceEvent
    data object DismissModal : WorkspaceEvent
    data class SetPreviewMode(val mode: String) : WorkspaceEvent
    data class SetPreviewUrl(val url: String) : WorkspaceEvent
    data object TogglePreviewShared : WorkspaceEvent
    data class SwitchOpenFile(val filename: String) : WorkspaceEvent
    data class UpdateFileText(val filename: String, val content: String) : WorkspaceEvent
    data class CreateNewFile(val parentDir: String, val filename: String) : WorkspaceEvent
    data class CreateNewFolder(val parentDir: String, val folderName: String) : WorkspaceEvent
    data class DeleteFile(val path: String) : WorkspaceEvent
    data class RenameFile(val oldPath: String, val newPath: String) : WorkspaceEvent
    data class ToggleFolderExpanded(val path: String, val isExpanded: Boolean) : WorkspaceEvent
    data class ExecuteTerminal(val command: String) : WorkspaceEvent
    data class UpdateTerminalInput(val input: String) : WorkspaceEvent
    data class ApproveCommand(val approve: Boolean) : WorkspaceEvent
    data class ShowToast(val text: String) : WorkspaceEvent
    data object DismissToast : WorkspaceEvent
    data object OpenPlugins : WorkspaceEvent
    data object ClosePlugins : WorkspaceEvent
    data object OpenTools : WorkspaceEvent
    data object CloseTools : WorkspaceEvent
    data object OpenFileExplorerSidebar : WorkspaceEvent
    data object CloseFileExplorerSidebar : WorkspaceEvent
    data object ToggleFileExplorerSidebar : WorkspaceEvent
    data object CycleAgent : WorkspaceEvent
}
