package com.example

import com.example.core.database.ProjectFileEntity
import com.example.data.ai.ForgeCodingAgentImpl
import com.example.data.terminal.TerminalEngine
import com.example.domain.model.FileTreeNode
import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
import com.example.domain.model.UserSession
import com.example.domain.repository.AgentContext
import com.example.domain.repository.AgentEvent
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgeStudioUnitTest {

    private val sampleProject = ForgeConstants.initialProjects().first()
    private val sampleUser = UserSession()

    @Test
    fun `test terminal ls command lists project files`() {
        val output = TerminalEngine.execute("ls", sampleProject, sampleUser)
        assertNotNull(output)
        assertTrue(output!!.any { it.text.contains("src/ship.ts") })
        assertTrue(output.any { it.text.contains("package.json") })
    }

    @Test
    fun `test terminal tree command renders hierarchy`() {
        val output = TerminalEngine.execute("tree", sampleProject, sampleUser)
        assertNotNull(output)
        assertTrue(output!!.any { it.text.contains("src/") || it.text.contains("├── src/") })
        assertTrue(output.any { it.text.contains("ship.ts") })
    }

    @Test
    fun `test terminal gradle build and test commands`() {
        val buildOut = TerminalEngine.execute("gradle build", sampleProject, sampleUser)
        assertNotNull(buildOut)
        assertTrue(buildOut!!.any { it.text.contains("BUILD SUCCESSFUL") })

        val testOut = TerminalEngine.execute("gradle test", sampleProject, sampleUser)
        assertNotNull(testOut)
        assertTrue(testOut!!.any { it.text.contains("PASSED") })
    }

    @Test
    fun `test terminal pwd command returns workspace path`() {
        val output = TerminalEngine.execute("pwd", sampleProject, sampleUser)
        assertNotNull(output)
        assertEquals("/workspace/billing-api", output!!.first().text)
    }

    @Test
    fun `test terminal git status reports clean when no dirty files`() {
        val output = TerminalEngine.execute("git status", sampleProject, sampleUser)
        assertNotNull(output)
        assertTrue(output!!.any { it.text.contains("working tree clean") })
    }

    @Test
    fun `test terminal npm test runs suite`() {
        val output = TerminalEngine.execute("npm test", sampleProject, sampleUser)
        assertNotNull(output)
        assertTrue(output!!.any { it.text.contains("PASS") })
    }

    @Test
    fun `test terminal Aura query recommendations`() {
        val output = TerminalEngine.execute("# restart web", sampleProject, sampleUser)
        assertNotNull(output)
        assertTrue(output!!.any { it.text.contains("docker-compose restart") })
    }

    @Test
    fun `test Aura suggestions autocomplete`() {
        val suggestions = TerminalEngine.getSuggestions("git ", sampleProject)
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.first.startsWith("git ") })
    }

    @Test
    fun `test FileTreeNode buildTree generates nested directory hierarchy`() {
        val entities = listOf(
            ProjectFileEntity(
                id = "p1:src",
                projectId = "p1",
                path = "src",
                name = "src",
                parentPath = "",
                isDirectory = true
            ),
            ProjectFileEntity(
                id = "p1:src/ship.ts",
                projectId = "p1",
                path = "src/ship.ts",
                name = "ship.ts",
                parentPath = "src",
                isDirectory = false,
                extension = "ts"
            ),
            ProjectFileEntity(
                id = "p1:index.html",
                projectId = "p1",
                path = "index.html",
                name = "index.html",
                parentPath = "",
                isDirectory = false,
                extension = "html"
            )
        )

        val tree = FileTreeNode.buildTree(entities)
        assertEquals(2, tree.size) // "src" dir and "index.html"

        val srcDir = tree.find { it.name == "src" }
        assertNotNull(srcDir)
        assertTrue(srcDir!!.isDirectory)
        assertEquals(1, srcDir.children.size)
        assertEquals("ship.ts", srcDir.children[0].name)
    }

    @Test
    fun `test coding agent event streaming on restart command`() = runBlocking {
        val agent = ForgeCodingAgentImpl()
        val context = AgentContext(
            project = sampleProject,
            currentFile = "src/ship.ts",
            isPreviewShared = false,
            previewSummary = ""
        )

        val events = agent.sendMessage(sampleProject.id, "restart the web service", context).toList()
        assertTrue(events.isNotEmpty())
        assertTrue(events.any { it is AgentEvent.AssistantMessage })
        assertTrue(events.any { it is AgentEvent.ToolCallStarted })
    }

    @Test
    fun `test initial seed projects data structure`() {
        val projects = ForgeConstants.initialProjects()
        assertEquals(2, projects.size)
        assertEquals("billing-api", projects[0].id)
        assertEquals("auth-gateway", projects[1].id)
        assertTrue(projects[0].tools.ssh.on)
        assertTrue(projects[0].tools.gh.on)
    }
}
