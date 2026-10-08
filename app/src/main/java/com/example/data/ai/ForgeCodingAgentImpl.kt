package com.example.data.ai

import com.example.domain.model.ChatMessage
import com.example.domain.model.ForgeConstants
import com.example.domain.repository.AgentContext
import com.example.domain.repository.AgentEvent
import com.example.domain.repository.CodingAgent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ForgeCodingAgentImpl : CodingAgent {

    override suspend fun sendMessage(
        projectId: String,
        message: String,
        context: AgentContext
    ): Flow<AgentEvent> = flow {
        val query = message.trim().lowercase()
        val project = context.project
        val model = ForgeConstants.MODELS.find { it.id == project.model } ?: ForgeConstants.MODELS[1]
        val agentName = project.agent

        val calls = mutableListOf<Pair<String, String>>()
        fun addCall(tool: String, arg: String) {
            if (calls.none { it.first == tool }) calls.add(tool to arg)
        }

        if (query.contains("error") || query.contains("problem") || query.contains("lint") || query.contains("broken")) {
            addCall("problems", "workspace")
        }
        if (query.contains("diff") || query.contains("change") || query.contains("summar")) {
            addCall("changes", "git diff")
        }
        if (query.contains("add") || query.contains("fix") || query.contains("edit") || query.contains("update") || query.contains("refactor") || query.contains("write")) {
            addCall("edit", context.currentFile)
        }
        if (query.contains("http://") || query.contains("https://") || query.contains("doc") || query.contains("fetch")) {
            val url = Regex("https?://\\S+").find(message)?.value ?: "docs.usafe.in"
            addCall("fetch", url)
        }
        if (query.contains("github") || query.contains("example") || query.contains("repo")) {
            addCall("githubRepo", message.take(28))
        }
        if (query.contains("plugin") || query.contains("extension")) {
            addCall("extensions", message.take(28))
        }
        if (query.contains("new project") || query.contains("scaffold") || query.contains("bootstrap")) {
            addCall("new", "template")
        }
        if (query.contains("test")) {
            addCall("findTestFiles", "src/")
            addCall("runCommands", "npm test")
        }

        val terminalCommand = when {
            query.contains("restart") -> "docker-compose restart"
            query.contains("deploy") -> "forge deploy"
            query.contains("pull") -> "git pull origin main"
            query.contains("commit") || query.contains("push") -> "git add . && git commit -m \"agent update\""
            query.contains("install") -> "npm install"
            query.contains("build") -> "npm run build"
            query.contains("run") || query.contains("start") -> "npm test"
            else -> ""
        }
        if (terminalCommand.isNotEmpty()) {
            addCall("runCommands", terminalCommand)
        }
        if (query.contains("preview") || query.contains("browser") || query.contains("open")) {
            addCall("openSimpleBrowser", "preview")
        }

        if (calls.isEmpty()) {
            delay(400)
            val previewNotice = if (context.isPreviewShared) " [Preview shared: ${context.previewSummary}]" else ""
            emit(
                AgentEvent.AssistantMessage(
                    "$agentName · ${model.displayName}$previewNotice. I'm ready to inspect code, run terminal commands, or make edits. Try \"run npm test\", \"add a comment\", \"check problems\" or \"restart web service\"."
                )
            )
            emit(AgentEvent.Completed)
            return@flow
        }

        // Introduction
        val toolNames = calls.map { it.first }.joinToString(", ")
        emit(
            AgentEvent.AssistantMessage(
                "$agentName · ${model.displayName}. Using: $toolNames."
            )
        )
        delay(350)

        for ((tool, arg) in calls) {
            val def = ForgeConstants.TOOL14.find { it.id == tool } ?: ForgeConstants.TOOL14[0]
            emit(AgentEvent.ToolCallStarted(tool, def.icon, arg))
            delay(500)

            // Check if tool is disabled in project
            if (project.ptools[tool] == false) {
                emit(AgentEvent.ToolCallCompleted(tool, "Disabled. Turn it on in Tools."))
                continue
            }

            if (tool == "runCommands") {
                emit(AgentEvent.ToolCallCompleted(tool, "Waiting for approval"))
                emit(AgentEvent.AskCommandApproval(arg))
                // The UI will handle AskCommandApproval and resume terminal simulation
                continue
            }

            val result = when (tool) {
                "edit" -> {
                    val target = if (project.files.containsKey(arg)) arg else context.currentFile
                    val existing = project.files[target] ?: ""
                    val updated = existing + "\n// forge agent: ${message.take(48)}"
                    emit(AgentEvent.FileChanged(target, updated))
                    "+1 line in $target"
                }
                "problems" -> {
                    val count = project.files.values.sumOf { Regex("TODO|FIXME").findAll(it).count() }
                    if (count > 0) "$count problem(s) found" else "0 problems found"
                }
                "changes" -> {
                    if (project.git.dirty.isNotEmpty()) "Changed: ${project.git.dirty.joinToString(", ")}" else "No pending changes"
                }
                "fetch" -> "Fetched $arg (3 sections parsed)"
                "githubRepo" -> "4 matching repositories found"
                "extensions" -> "Matches found. See Plugins in drawer."
                "new" -> "Template scaffold ready"
                "findTestFiles" -> {
                    val tests = project.files.keys.filter { it.contains("test") || it.contains("spec") }
                    if (tests.isNotEmpty()) tests.joinToString(", ") else "No test files yet"
                }
                "openSimpleBrowser" -> "Opened in-app preview"
                else -> "ok"
            }

            emit(AgentEvent.ToolCallCompleted(tool, result))
            delay(200)
        }

        delay(300)
        emit(AgentEvent.AssistantMessage("Done. Review the changes in View Code, or ask me to commit."))
        emit(AgentEvent.Completed)
    }
}
