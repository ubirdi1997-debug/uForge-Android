package com.example.data.projects

import com.example.core.database.FileDao
import com.example.core.database.ForgeDatabase
import com.example.core.database.ProjectFileEntity
import com.example.core.security.KeystoreManager
import com.example.domain.model.Agent
import com.example.domain.model.ByokConfig
import com.example.domain.model.ChatMessage
import com.example.domain.model.ForgeConstants
import com.example.domain.model.GitState
import com.example.domain.model.PreviewState
import com.example.domain.model.Project
import com.example.domain.model.ProjectTools
import com.example.domain.repository.ProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemoProjectRepository(
    private val keystoreManager: KeystoreManager,
    private val database: ForgeDatabase? = null
) : ProjectRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val fileDao: FileDao? = database?.fileDao()

    private val _projects = MutableStateFlow<List<Project>>(ForgeConstants.initialProjects())
    override val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _currentProjectId = MutableStateFlow<String>("billing-api")
    override val currentProjectId: StateFlow<String> = _currentProjectId.asStateFlow()

    override val currentProject: StateFlow<Project> = combine(_projects, _currentProjectId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull() ?: ForgeConstants.initialProjects().first()
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        ForgeConstants.initialProjects().first()
    )

    init {
        // Seed Room Database with initial project files if empty
        scope.launch {
            fileDao?.let { dao ->
                _projects.value.forEach { proj ->
                    val count = dao.countProjectFiles(proj.id)
                    if (count == 0) {
                        seedRoomForProject(proj)
                    }
                }
            }
        }
    }

    private suspend fun seedRoomForProject(proj: Project) {
        val dao = fileDao ?: return
        val entities = mutableListOf<ProjectFileEntity>()
        val createdDirs = mutableSetOf<String>()

        proj.files.forEach { (filePath, content) ->
            val segments = filePath.split('/')
            if (segments.size > 1) {
                var curParent = ""
                for (i in 0 until segments.size - 1) {
                    val dirName = segments[i]
                    val dirPath = if (curParent.isEmpty()) dirName else "$curParent/$dirName"
                    if (createdDirs.add(dirPath)) {
                        entities.add(
                            ProjectFileEntity(
                                id = "${proj.id}:$dirPath",
                                projectId = proj.id,
                                path = dirPath,
                                name = dirName,
                                parentPath = curParent,
                                isDirectory = true,
                                content = "",
                                extension = "",
                                sizeBytes = 0L,
                                isExpanded = true
                            )
                        )
                    }
                    curParent = dirPath
                }
            }

            val fileName = segments.last()
            val parent = if (segments.size > 1) segments.dropLast(1).joinToString("/") else ""
            val ext = fileName.substringAfterLast('.', "")
            entities.add(
                ProjectFileEntity(
                    id = "${proj.id}:$filePath",
                    projectId = proj.id,
                    path = filePath,
                    name = fileName,
                    parentPath = parent,
                    isDirectory = false,
                    content = content,
                    extension = ext,
                    sizeBytes = content.toByteArray().size.toLong(),
                    lastModified = System.currentTimeMillis()
                )
            )
        }
        dao.insertFiles(entities)
    }

    override fun getProjectFileEntities(projectId: String): Flow<List<ProjectFileEntity>> {
        return fileDao?.getFilesForProject(projectId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override fun selectProject(id: String) {
        if (_projects.value.any { it.id == id }) {
            _currentProjectId.value = id
        }
    }

    override fun createProject(name: String, templateKind: String): Project {
        val sanitized = name.trim().lowercase().replace(Regex("[^a-z0-9-]+"), "-").take(24)
        val files = when (templateKind) {
            "Blank" -> mapOf("README.md" to "# $sanitized")
            "App" -> mapOf(
                "index.html" to ForgeConstants.seedIndexHtml(sanitized),
                "src/app.ts" to "export const app = { name: '$sanitized', version: '1.0.0' }\n",
                "README.md" to "# $sanitized"
            )
            else -> ForgeConstants.seedFiles(sanitized)
        }
        val openFile = if (templateKind == "Blank") "README.md" else "index.html"
        val newProj = Project(
            id = sanitized,
            name = sanitized,
            files = files,
            openFile = openFile,
            preview = PreviewState(
                mode = "html",
                summary = "$sanitized · 1 links · headings: $sanitized"
            ),
            git = GitState(
                dirty = emptyList(),
                staged = emptyList(),
                log = listOf("7be0d14 Initial commit for $sanitized")
            ),
            messages = listOf(
                ChatMessage.Assistant("m0", "Welcome to $sanitized. Connect SSH, GitHub or uVault from the tools deck.")
            )
        )

        _projects.update { it + newProj }
        _currentProjectId.value = sanitized

        scope.launch {
            seedRoomForProject(newProj)
        }

        return newProj
    }

    override fun updateProject(project: Project) {
        _projects.update { list ->
            list.map { if (it.id == project.id) project else it }
        }
    }

    override fun updateTools(projectId: String, tools: ProjectTools) {
        if (tools.ssh.on && tools.ssh.host.isNotEmpty()) {
            keystoreManager.encryptAndStore("ssh_${projectId}_host", tools.ssh.host)
        }
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(tools = tools) else it }
        }
    }

    override fun updateByok(projectId: String, byok: ByokConfig) {
        byok.saved.forEach { (prov, keyEnd) ->
            if (keyEnd.isNotEmpty()) {
                keystoreManager.encryptAndStore("byok_${projectId}_$prov", "key_ending_$keyEnd")
            }
        }
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(byok = byok) else it }
        }
    }

    override fun updateModel(projectId: String, model: String) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(model = model) else it }
        }
    }

    override fun updateAgent(projectId: String, agent: String) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(agent = agent) else it }
        }
    }

    override fun addCustomAgent(projectId: String, agent: Agent) {
        _projects.update { list ->
            list.map {
                if (it.id == projectId) {
                    it.copy(
                        agents = it.agents + agent,
                        agent = agent.name
                    )
                } else it
            }
        }
    }

    override fun updateFileContent(projectId: String, filename: String, content: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedFiles = proj.files.toMutableMap().apply { put(filename, content) }
                    val dirty = if (!proj.git.dirty.contains(filename)) proj.git.dirty + filename else proj.git.dirty
                    proj.copy(
                        files = updatedFiles,
                        git = proj.git.copy(dirty = dirty)
                    )
                } else proj
            }
        }

        scope.launch {
            fileDao?.updateFileContent(
                projectId = projectId,
                path = filename,
                content = content,
                sizeBytes = content.toByteArray().size.toLong(),
                lastModified = System.currentTimeMillis()
            )
        }
    }

    override fun createFile(projectId: String, filename: String, content: String) {
        val sanitized = filename.trim().removePrefix("/")
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedFiles = proj.files.toMutableMap().apply { put(sanitized, content) }
                    proj.copy(
                        files = updatedFiles,
                        openFile = sanitized
                    )
                } else proj
            }
        }

        scope.launch {
            val segments = sanitized.split('/')
            val fileName = segments.last()
            val parent = if (segments.size > 1) segments.dropLast(1).joinToString("/") else ""
            val ext = fileName.substringAfterLast('.', "")

            // Ensure parent directory entity exists in Room
            if (parent.isNotEmpty()) {
                var curParent = ""
                for (i in 0 until segments.size - 1) {
                    val dirName = segments[i]
                    val dirPath = if (curParent.isEmpty()) dirName else "$curParent/$dirName"
                    fileDao?.insertFile(
                        ProjectFileEntity(
                            id = "$projectId:$dirPath",
                            projectId = projectId,
                            path = dirPath,
                            name = dirName,
                            parentPath = curParent,
                            isDirectory = true,
                            content = "",
                            extension = "",
                            sizeBytes = 0L,
                            isExpanded = true
                        )
                    )
                    curParent = dirPath
                }
            }

            fileDao?.insertFile(
                ProjectFileEntity(
                    id = "$projectId:$sanitized",
                    projectId = projectId,
                    path = sanitized,
                    name = fileName,
                    parentPath = parent,
                    isDirectory = false,
                    content = content,
                    extension = ext,
                    sizeBytes = content.toByteArray().size.toLong(),
                    lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override fun createFolder(projectId: String, folderPath: String) {
        val sanitized = folderPath.trim().removePrefix("/").removeSuffix("/")
        val segments = sanitized.split('/')
        val dirName = segments.last()
        val parent = if (segments.size > 1) segments.dropLast(1).joinToString("/") else ""

        scope.launch {
            fileDao?.insertFile(
                ProjectFileEntity(
                    id = "$projectId:$sanitized",
                    projectId = projectId,
                    path = sanitized,
                    name = dirName,
                    parentPath = parent,
                    isDirectory = true,
                    content = "",
                    extension = "",
                    sizeBytes = 0L,
                    isExpanded = true
                )
            )
        }

        val keepPath = "$sanitized/.gitkeep"
        createFile(projectId, keepPath, "")
    }

    override fun deleteFile(projectId: String, filename: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedFiles = proj.files.toMutableMap().apply { remove(filename) }
                    val newOpen = if (proj.openFile == filename) updatedFiles.keys.firstOrNull() ?: "" else proj.openFile
                    proj.copy(
                        files = updatedFiles,
                        openFile = newOpen
                    )
                } else proj
            }
        }

        scope.launch {
            fileDao?.deleteFile(projectId, filename)
            fileDao?.deleteDirectoryAndChildren(projectId, filename, "$filename/%")
        }
    }

    override fun renameFile(projectId: String, oldPath: String, newPath: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val content = proj.files[oldPath] ?: ""
                    val updatedFiles = proj.files.toMutableMap().apply {
                        remove(oldPath)
                        put(newPath, content)
                    }
                    val newOpen = if (proj.openFile == oldPath) newPath else proj.openFile
                    proj.copy(
                        files = updatedFiles,
                        openFile = newOpen
                    )
                } else proj
            }
        }

        scope.launch {
            fileDao?.deleteFile(projectId, oldPath)
            createFile(projectId, newPath, _projects.value.find { it.id == projectId }?.files?.get(newPath) ?: "")
        }
    }

    override fun toggleFolderExpanded(projectId: String, folderPath: String, isExpanded: Boolean) {
        scope.launch {
            fileDao?.setFolderExpanded(projectId, folderPath, isExpanded)
        }
    }

    override fun selectOpenFile(projectId: String, filename: String) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(openFile = filename) else it }
        }
    }

    override fun togglePlugin(projectId: String, pluginId: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val has = proj.plugins.contains(pluginId)
                    val next = if (has) proj.plugins - pluginId else proj.plugins + pluginId
                    proj.copy(plugins = next)
                } else proj
            }
        }
    }

    override fun toggleTool(projectId: String, toolId: String, enabled: Boolean) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updated = proj.ptools.toMutableMap().apply { put(toolId, enabled) }
                    proj.copy(ptools = updated)
                } else proj
            }
        }
    }

    override fun setAllTools(projectId: String, enabled: Boolean) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val map = ForgeConstants.TOOL14.associate { it.id to enabled }
                    proj.copy(ptools = map)
                } else proj
            }
        }
    }

    override fun updatePreview(projectId: String, preview: PreviewState) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(preview = preview) else it }
        }
    }

    override fun addMessage(projectId: String, message: ChatMessage) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(messages = it.messages + message) else it }
        }
    }

    override fun updateMessage(projectId: String, message: ChatMessage) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val msgs = proj.messages.map { if (it.id == message.id) message else it }
                    proj.copy(messages = msgs)
                } else proj
            }
        }
    }
}
