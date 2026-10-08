package com.example.domain.model

data class Project(
    val id: String,
    val name: String,
    val files: Map<String, String>,
    val openFile: String = "src/ship.ts",
    val tools: ProjectTools = ProjectTools(),
    val byok: ByokConfig = ByokConfig(),
    val model: String = "aura",
    val agent: String = "Forge agent",
    val agents: List<Agent> = emptyList(),
    val plugins: List<String> = emptyList(),
    val ptools: Map<String, Boolean> = emptyMap(),
    val preview: PreviewState = PreviewState(),
    val git: GitState = GitState(),
    val messages: List<ChatMessage> = emptyList()
)

data class ProjectTools(
    val ssh: SshConfig = SshConfig(),
    val gh: GitHubConfig = GitHubConfig(),
    val vault: VaultConfig = VaultConfig()
)

data class SshConfig(
    val on: Boolean = false,
    val user: String = "root",
    val host: String = "",
    val how: String = "pw", // "pw" or "key"
    val hasKey: Boolean = false
)

data class GitHubConfig(
    val on: Boolean = false,
    val repo: String = "",
    val auto: Boolean = true
)

data class VaultConfig(
    val on: Boolean = false,
    val folder: String = ""
)

data class ByokConfig(
    val on: Boolean = false,
    val prov: String = "aws", // "do", "gc", "aws"
    val saved: Map<String, String> = mapOf("do" to "", "gc" to "", "aws" to "")
)

data class Agent(
    val name: String,
    val role: String,
    val skills: List<String>,
    val tools: List<String> = listOf("edit", "runCommands", "problems")
)

data class GitState(
    val dirty: List<String> = emptyList(),
    val staged: List<String> = emptyList(),
    val log: List<String> = listOf("a3f9c21 Add layout", "7be0d14 Initial commit"),
    val ahead: Int = 0
)

data class PreviewState(
    val mode: String = "html", // "html" or "url"
    val url: String = "http://178.62.14.90:3000",
    val isShared: Boolean = false,
    val summary: String = ""
)

sealed interface ChatMessage {
    val id: String

    data class User(
        override val id: String,
        val text: String
    ) : ChatMessage

    data class Assistant(
        override val id: String,
        val text: String
    ) : ChatMessage

    data class ToolCall(
        override val id: String,
        val tool: String,
        val icon: String,
        val argument: String,
        val status: String, // "run", "ok", "off"
        val result: String = ""
    ) : ChatMessage

    data class AskApproval(
        override val id: String,
        val command: String,
        val status: String // "wait", "ok", "no"
    ) : ChatMessage

    data class Terminal(
        override val id: String,
        val lines: List<Pair<String, String>>, // line to style tag ("cm", "ok", "er", "")
        val isDone: Boolean
    ) : ChatMessage
}

data class UserSession(
    val name: String = "Mara Dev",
    val email: String = "mara@usafe.in",
    val plan: String = "free", // "free" or "one"
    val tokensUsed: Long = 18400L,
    val vaultUsedGb: Double = 3.2
)

data class ToolDefinition(
    val id: String,
    val icon: String,
    val name: String,
    val description: String,
    val exampleQuery: String
)

data class ProviderDefinition(
    val key: String,
    val displayName: String,
    val fields: List<ProviderField>
)

data class ProviderField(
    val label: String,
    val type: String, // "text" or "password"
    val defaultValue: String = ""
)

data class ModelOption(
    val id: String,
    val displayName: String,
    val requiredProvider: String = "", // "do", "gc", "aws", or empty
    val planRequirement: String = "" // "one" or empty
)
