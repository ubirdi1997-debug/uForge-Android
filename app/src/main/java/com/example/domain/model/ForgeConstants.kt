package com.example.domain.model

object ForgeConstants {

    val TOOL14 = listOf(
        ToolDefinition("changes", "🔄", "changes", "Get diffs of changed files", "\"Summarize what changed.\""),
        ToolDefinition("edit", "✏️", "edit", "Edit files in your workspace", "\"Add a comment to this file.\""),
        ToolDefinition("extensions", "📦", "extensions", "Search for extensions", "\"Find the Rust tools.\""),
        ToolDefinition("fetch", "🌐", "fetch", "Fetch content from web pages", "\"Fetch the Express docs.\""),
        ToolDefinition("githubRepo", "🐙", "githubRepo", "Search GitHub repositories", "\"Find a chess engine example.\""),
        ToolDefinition("new", "🆕", "new", "Scaffold a new workspace", "\"Create a FastAPI project.\""),
        ToolDefinition("openSimpleBrowser", "🧭", "openSimpleBrowser", "Preview hosted resources", "\"Open the preview.\""),
        ToolDefinition("problems", "⚠️", "problems", "Check errors in files", "\"Fix all TypeScript errors.\""),
        ToolDefinition("runCommands", "💻", "runCommands", "Execute terminal commands", "\"Run npm test.\""),
        ToolDefinition("search", "🔎", "search", "Search across workspace files", "\"Where is ship() used?\""),
        ToolDefinition("findTestFiles", "🧪", "findTestFiles", "Find test files for code", "\"Which tests cover server.ts?\""),
        ToolDefinition("testFailure", "🚨", "testFailure", "Read failing test output", "\"Why did the last test fail?\""),
        ToolDefinition("usages", "🔗", "usages", "Find where symbols are used", "\"Show usages of docker.restart.\""),
        ToolDefinition("terminalLastCommand", "⌨️", "terminalLastCommand", "Read the last command and output", "\"Explain the last error.\"")
    )

    val PLUGINS = listOf(
        Triple("cloudflare", "Cloudflare", "DNS, Workers and CDN"),
        Triple("vercel", "Vercel", "Deploy frontends"),
        Triple("supabase", "Supabase", "Postgres and auth"),
        Triple("stripe", "Stripe", "Payments"),
        Triple("sentry", "Sentry", "Error tracking"),
        Triple("docker", "Docker Hub", "Images and registries"),
        Triple("linear", "Linear", "Issue tracking"),
        Triple("slack", "Slack", "Team alerts"),
        Triple("datadog", "Datadog", "Monitoring"),
        Triple("netlify", "Netlify", "Static hosting")
    )

    val PROVIDERS = mapOf(
        "do" to ProviderDefinition(
            key = "do",
            displayName = "DigitalOcean",
            fields = listOf(
                ProviderField("Model access key", "password")
            )
        ),
        "gc" to ProviderDefinition(
            key = "gc",
            displayName = "Google Cloud",
            fields = listOf(
                ProviderField("API key", "password"),
                ProviderField("Project ID", "text"),
                ProviderField("Region", "text", "us-central1")
            )
        ),
        "aws" to ProviderDefinition(
            key = "aws",
            displayName = "AWS Bedrock",
            fields = listOf(
                ProviderField("Access key ID", "text"),
                ProviderField("Secret access key", "password"),
                ProviderField("Region", "text", "us-east-1")
            )
        )
    )

    val MODELS = listOf(
        ModelOption("copilot", "Forge Co-Pilot (auto)", "", planRequirement = "one"),
        ModelOption("aura", "Aura (uSafe)", ""),
        ModelOption("dollama", "Llama 3.3 70B", "do"),
        ModelOption("dods", "DeepSeek R1 Distill", "do"),
        ModelOption("gpro", "Gemini 2.5 Pro", "gc"),
        ModelOption("gflash", "Gemini 2.5 Flash", "gc"),
        ModelOption("bsonnet", "Claude Sonnet (Bedrock)", "aws"),
        ModelOption("nova", "Amazon Nova Pro", "aws")
    )

    fun seedIndexHtml(name: String) = """
<!DOCTYPE html>
<html>
<head>
  <title>$name</title>
</head>
<body style="font-family:system-ui;margin:0;background:#0F172A;color:#F1F5F9">
  <h1 style="padding:20px">$name</h1>
  <h2 style="padding:0 20px">Invoices</h2>
  <ul style="padding:0 20px">
    <li>#1042 Acme · 48,200</li>
    <li>#1041 Globex · paid</li>
  </ul>
  <a style="padding:20px;display:block;color:#4ADE80" href="#">Pay now</a>
</body>
</html>
""".trimIndent()

    fun seedFiles(name: String): Map<String, String> = mapOf(
        "src/ship.ts" to """
import { git, docker } from "@forge/ops"

export async function ship() {
  await git.pull("origin", "main")
  await docker.restart("web")
  return status("live")
}

// deploy runs on the linked server
""".trimIndent(),
        "src/server.ts" to """
import express from "express"

const app = express()
app.get("/health", (_, r) => r.send("ok"))
app.listen(3000)
""".trimIndent(),
        "deploy.sh" to """
#!/bin/sh
git pull origin main
docker-compose restart
""".trimIndent(),
        "package.json" to """
{
  "name": "$name",
  "scripts": { "start": "node dist/server.js" }
}
""".trimIndent(),
        "index.html" to seedIndexHtml(name)
    )

    fun initialProjects(): List<Project> = listOf(
        Project(
            id = "billing-api",
            name = "billing-api",
            files = seedFiles("billing-api"),
            openFile = "src/ship.ts",
            tools = ProjectTools(
                ssh = SshConfig(on = true, user = "root", host = "178.62.14.90", how = "key", hasKey = true),
                gh = GitHubConfig(on = true, repo = "mara-dev/billing-api", auto = true),
                vault = VaultConfig(on = false)
            ),
            byok = ByokConfig(
                on = true,
                prov = "aws",
                saved = mapOf("do" to "", "gc" to "", "aws" to "a1b2")
            ),
            model = "bsonnet",
            agent = "Forge agent",
            plugins = listOf("cloudflare"),
            preview = PreviewState(
                mode = "html",
                url = "http://178.62.14.90:3000",
                isShared = false,
                summary = "billing-api · 1 links · headings: billing-api, Invoices"
            ),
            git = GitState(
                dirty = emptyList(),
                staged = emptyList(),
                log = listOf("a3f9c21 Add layout", "7be0d14 Initial commit")
            ),
            messages = listOf(
                ChatMessage.User("m1", "Pull the latest and restart the web service"),
                ChatMessage.Assistant("m2", "Forge agent · Claude Sonnet (Bedrock). Using: runCommands."),
                ChatMessage.ToolCall("m3", "runCommands", "💻", "git pull origin main && docker-compose restart", "ok", "Approved"),
                ChatMessage.Terminal(
                    "m4",
                    listOf(
                        "$ git pull origin main" to "cm",
                        "Fast-forward · 3 files changed" to "",
                        "$ docker-compose restart" to "cm",
                        "Restarting web ... done" to "ok",
                        "Restarting api ... done" to "ok"
                    ),
                    isDone = true
                ),
                ChatMessage.Assistant("m5", "Done. Review the changes in View Code, or ask me to commit.")
            )
        ),
        Project(
            id = "auth-gateway",
            name = "auth-gateway",
            files = seedFiles("auth-gateway"),
            openFile = "src/server.ts",
            tools = ProjectTools(
                ssh = SshConfig(on = true, user = "deploy", host = "10.0.4.7", how = "pw"),
                gh = GitHubConfig(on = false),
                vault = VaultConfig(on = true, folder = "/projects/auth")
            ),
            byok = ByokConfig(
                on = true,
                prov = "do",
                saved = mapOf("do" to "9c3f", "gc" to "", "aws" to "")
            ),
            model = "dollama",
            agent = "Forge agent",
            preview = PreviewState(
                mode = "html",
                url = "http://10.0.4.7:8080",
                isShared = false,
                summary = "auth-gateway · 1 links"
            )
        )
    )
}
