package com.example.domain.repository

import com.example.domain.model.Agent
import com.example.domain.model.ByokConfig
import com.example.domain.model.ChatMessage
import com.example.domain.model.PreviewState
import com.example.domain.model.Project
import com.example.domain.model.ProjectTools
import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class AuthResult(
    val isSuccess: Boolean,
    val user: UserSession? = null,
    val errorMessage: String? = null
)

interface AuthRepository {
    val currentUser: StateFlow<UserSession?>
    suspend fun login(email: String, password: String): AuthResult
    suspend fun signup(name: String, email: String, password: String): AuthResult
    suspend fun updatePlan(plan: String)
    suspend fun consumeTokens(count: Long)
    suspend fun updateVaultUsed(gb: Double)
    suspend fun logout()
}

interface ProjectRepository {
    val projects: StateFlow<List<Project>>
    val currentProjectId: StateFlow<String>
    val currentProject: StateFlow<Project>
    fun getProjectFileEntities(projectId: String): Flow<List<com.example.core.database.ProjectFileEntity>>

    fun selectProject(id: String)
    fun createProject(name: String, templateKind: String = "Website"): Project
    fun updateProject(project: Project)
    fun updateTools(projectId: String, tools: ProjectTools)
    fun updateByok(projectId: String, byok: ByokConfig)
    fun updateModel(projectId: String, model: String)
    fun updateAgent(projectId: String, agent: String)
    fun addCustomAgent(projectId: String, agent: Agent)
    fun updateFileContent(projectId: String, filename: String, content: String)
    fun createFile(projectId: String, filename: String, content: String = "")
    fun createFolder(projectId: String, folderPath: String)
    fun deleteFile(projectId: String, filename: String)
    fun renameFile(projectId: String, oldPath: String, newPath: String)
    fun toggleFolderExpanded(projectId: String, folderPath: String, isExpanded: Boolean)
    fun selectOpenFile(projectId: String, filename: String)
    fun togglePlugin(projectId: String, pluginId: String)
    fun toggleTool(projectId: String, toolId: String, enabled: Boolean)
    fun setAllTools(projectId: String, enabled: Boolean)
    fun updatePreview(projectId: String, preview: PreviewState)
    fun addMessage(projectId: String, message: ChatMessage)
    fun updateMessage(projectId: String, message: ChatMessage)
}

data class AgentContext(
    val project: Project,
    val currentFile: String,
    val isPreviewShared: Boolean,
    val previewSummary: String
)

sealed interface AgentEvent {
    data class AssistantMessage(val text: String) : AgentEvent
    data class ToolCallStarted(val tool: String, val icon: String, val argument: String) : AgentEvent
    data class ToolCallCompleted(val tool: String, val result: String) : AgentEvent
    data class AskCommandApproval(val command: String) : AgentEvent
    data class TerminalStream(val line: String, val style: String = "") : AgentEvent
    data class TerminalCompleted(val success: Boolean) : AgentEvent
    data class FileChanged(val filename: String, val newContent: String) : AgentEvent
    data object Completed : AgentEvent
    data class Error(val message: String) : AgentEvent
}

interface CodingAgent {
    suspend fun sendMessage(
        projectId: String,
        message: String,
        context: AgentContext
    ): Flow<AgentEvent>
}
