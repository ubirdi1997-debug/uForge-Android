package com.example.data.terminal

import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
import com.example.domain.model.UserSession

object TerminalEngine {

    data class OutputLine(val text: String, val style: String = "") // "cm", "ok", "er", "mt", "warn", ""

    fun execute(
        command: String,
        project: Project,
        user: UserSession,
        onFileModified: ((filename: String, content: String) -> Unit)? = null,
        onFileCreated: ((filename: String, content: String) -> Unit)? = null,
        onFileDeleted: ((filename: String) -> Unit)? = null,
        onFolderCreated: ((folderPath: String) -> Unit)? = null,
        onGitUpdate: ((dirty: List<String>, staged: List<String>, log: List<String>, ahead: Int) -> Unit)? = null
    ): List<OutputLine>? {
        val trimmed = command.trim()
        if (trimmed == "clear" || trimmed == "cls") return null

        val parts = trimmed.split(Regex("\\s+"))
        val primary = parts.getOrNull(0)?.lowercase() ?: return emptyList()
        val args = parts.drop(1)
        val arg1 = args.getOrNull(0) ?: ""
        val arg2 = args.getOrNull(1) ?: ""
        val output = mutableListOf<OutputLine>()

        fun add(text: String, style: String = "") {
            output.add(OutputLine(text, style))
        }

        when (primary) {
            "ls" -> {
                val showAll = args.contains("-la") || args.contains("-a") || args.contains("-l")
                if (showAll) {
                    add("total ${project.files.size * 4}K", "mt")
                    add("drwxr-xr-x 4 dev dev 4096 Oct 07 13:00 .", "mt")
                    add("drwxr-xr-x 8 dev dev 4096 Oct 07 12:45 ..", "mt")
                    add("drwxr-xr-x 8 dev dev 4096 Oct 07 13:00 .git", "mt")
                }
                val visible = project.files.keys.filter { showAll || !it.endsWith(".gitkeep") }
                if (visible.isEmpty()) {
                    add("(empty directory)", "mt")
                } else {
                    visible.sorted().forEach {
                        if (showAll) {
                            val size = project.files[it]?.length ?: 0
                            add("-rw-r--r-- 1 dev dev ${size.toString().padStart(5)} Oct 07 13:00 $it", if (it.endsWith(".sh")) "ok" else "")
                        } else {
                            add(it, if (it.endsWith(".sh")) "ok" else if (it.contains("/")) "mt" else "")
                        }
                    }
                }
            }

            "tree" -> {
                add(".", "cm")
                val files = project.files.keys.sorted()
                val dirs = mutableMapOf<String, MutableList<String>>()
                files.forEach { f ->
                    val slash = f.lastIndexOf('/')
                    val dir = if (slash != -1) f.substring(0, slash) else ""
                    val name = if (slash != -1) f.substring(slash + 1) else f
                    dirs.getOrPut(dir) { mutableListOf() }.add(name)
                }

                dirs[""]?.forEachIndexed { i, f ->
                    val isLast = i == (dirs[""]?.size ?: 1) - 1 && dirs.keys.none { it.isNotEmpty() }
                    add("${if (isLast) "└── " else "├── "}$f", if (f.endsWith(".html")) "warn" else "")
                }
                dirs.filter { it.key.isNotEmpty() }.forEach { (dir, fList) ->
                    add("├── $dir/", "ok")
                    fList.forEachIndexed { i, f ->
                        val isLast = i == fList.size - 1
                        add("│   ${if (isLast) "└── " else "├── "}$f")
                    }
                }
                add("${dirs.size - 1} directories, ${files.size} files", "mt")
            }

            "pwd" -> {
                add("/workspace/${project.name}")
            }

            "cd" -> {
                if (arg1.isEmpty() || arg1 == "~" || arg1 == ".") {
                    add("/workspace/${project.name}", "mt")
                } else {
                    add("cd: /workspace/${project.name}/$arg1", "mt")
                }
            }

            "cat" -> {
                if (arg1.isEmpty()) {
                    add("usage: cat <filename>", "er")
                } else {
                    val fileContent = project.files[arg1] ?: project.files.entries.find { it.key.endsWith(arg1) }?.value
                    if (fileContent == null) {
                        add("cat: $arg1: No such file or directory", "er")
                    } else {
                        fileContent.lines().take(30).forEach { add(it) }
                        if (fileContent.lines().size > 30) {
                            add("... (${fileContent.lines().size - 30} more lines)", "mt")
                        }
                    }
                }
            }

            "head" -> {
                if (arg1.isEmpty()) {
                    add("usage: head <filename>", "er")
                } else {
                    val fileContent = project.files[arg1] ?: project.files.entries.find { it.key.endsWith(arg1) }?.value
                    if (fileContent == null) {
                        add("head: cannot open '$arg1': No such file", "er")
                    } else {
                        fileContent.lines().take(10).forEach { add(it) }
                    }
                }
            }

            "tail" -> {
                if (arg1.isEmpty()) {
                    add("usage: tail <filename>", "er")
                } else {
                    val fileContent = project.files[arg1] ?: project.files.entries.find { it.key.endsWith(arg1) }?.value
                    if (fileContent == null) {
                        add("tail: cannot open '$arg1': No such file", "er")
                    } else {
                        fileContent.lines().takeLast(10).forEach { add(it) }
                    }
                }
            }

            "touch" -> {
                if (arg1.isEmpty()) {
                    add("usage: touch <filename>", "er")
                } else {
                    onFileCreated?.invoke(arg1, "")
                    add("Created $arg1", "ok")
                }
            }

            "mkdir" -> {
                if (arg1.isEmpty()) {
                    add("usage: mkdir <directory>", "er")
                } else {
                    onFolderCreated?.invoke(arg1)
                    add("Created directory $arg1", "ok")
                }
            }

            "rm" -> {
                val target = if (arg1 == "-rf" || arg1 == "-r") arg2 else arg1
                if (target.isEmpty()) {
                    add("usage: rm [-rf] <filename>", "er")
                } else {
                    onFileDeleted?.invoke(target)
                    add("Removed $target", "ok")
                }
            }

            "grep" -> {
                if (arg1.isEmpty() || arg2.isEmpty()) {
                    add("usage: grep <pattern> <file>", "er")
                } else {
                    val content = project.files[arg2]
                    if (content == null) {
                        add("grep: $arg2: No such file", "er")
                    } else {
                        val matches = content.lines().filter { it.contains(arg1, ignoreCase = true) }
                        if (matches.isEmpty()) {
                            add("(no matches found)", "mt")
                        } else {
                            matches.forEach { add(it, "ok") }
                        }
                    }
                }
            }

            "gradle" -> {
                when (arg1) {
                    "build", "assembleDebug" -> {
                        add("> Task :app:compileKotlin UP-TO-DATE", "mt")
                        add("> Task :app:kspKotlin UP-TO-DATE", "mt")
                        add("> Task :app:mergeDebugResources UP-TO-DATE", "mt")
                        add("> Task :app:assembleDebug", "mt")
                        add("BUILD SUCCESSFUL in 1.4s", "ok")
                        add("24 actionable tasks: 4 executed, 20 up-to-date", "mt")
                    }
                    "test", "testDebugUnitTest" -> {
                        add("> Task :app:testDebugUnitTest", "mt")
                        add("ForgeStudioUnitTest > test terminal ls PASSED", "ok")
                        add("ForgeStudioUnitTest > test git status PASSED", "ok")
                        add("ForgeStudioUnitTest > test Aura query PASSED", "ok")
                        add("BUILD SUCCESSFUL in 1.8s", "ok")
                    }
                    else -> add("gradle: task '$arg1' completed successfully.", "ok")
                }
            }

            "git" -> {
                val git = project.git
                when (arg1) {
                    "status" -> {
                        add("On branch main")
                        add("Your branch is up to date with 'origin/main'.", "mt")
                        if (git.dirty.isNotEmpty()) {
                            add("Changes not staged for commit:")
                            git.dirty.forEach { add("  modified: $it", "er") }
                        } else {
                            add("nothing to commit, working tree clean", "ok")
                        }
                    }

                    "diff" -> {
                        if (git.dirty.isEmpty()) {
                            add("No diff. Working directory clean.", "mt")
                        } else {
                            git.dirty.forEach { f ->
                                add("diff --git a/$f b/$f", "cm")
                                add("--- a/$f", "er")
                                add("+++ b/$f", "ok")
                                add("@@ -1,5 +1,6 @@", "warn")
                                add("+ // modified via Forge Studio", "ok")
                            }
                        }
                    }

                    "branch" -> {
                        add("* main", "ok")
                        add("  feature/mobile-ide", "mt")
                        add("  staging", "mt")
                    }

                    "checkout" -> {
                        add("Switched to branch '${arg2.ifEmpty { "main" }}'", "ok")
                    }

                    "add" -> {
                        val newStaged = git.dirty.toList()
                        onGitUpdate?.invoke(git.dirty, newStaged, git.log, git.ahead)
                        add("staged ${newStaged.size} file(s)", "ok")
                    }

                    "commit" -> {
                        val commitMsg = Regex("-m\\s+[\"']?(.+?)[\"']?$").find(command)?.groupValues?.get(1) ?: "update"
                        if (git.staged.isEmpty() && git.dirty.isEmpty()) {
                            add("nothing to commit, working tree clean", "er")
                        } else {
                            val hash = (1..7).map { "0123456789abcdef".random() }.joinToString("")
                            val newLog = listOf("$hash $commitMsg") + git.log
                            onGitUpdate?.invoke(emptyList(), emptyList(), newLog, 1)
                            add("[main $hash] $commitMsg", "ok")
                            add(" 2 files changed, 14 insertions(+), 3 deletions(-)", "mt")
                        }
                    }

                    "push" -> {
                        if (project.tools.gh.on) {
                            add("Pushing to ${project.tools.gh.repo} (branch: main)...")
                            add("Counting objects: 3, done.", "mt")
                            add("Compressing objects: 100% (2/2), done.", "mt")
                            add("Writing objects: 100% (3/3), 420 bytes | 420.00 KiB/s, done.", "mt")
                            add("To github.com:${project.tools.gh.repo}.git", "ok")
                            add("   ${project.git.log.firstOrNull()?.split(" ")?.get(0) ?: "a3f9c21"}..${(1..7).map { "0123456789abcdef".random() }.joinToString("")}  main -> main", "ok")
                            onGitUpdate?.invoke(git.dirty, git.staged, git.log, 0)
                        } else {
                            add(if (git.ahead > 0) "main -> origin/main (encrypted)" else "Everything up-to-date", "ok")
                            onGitUpdate?.invoke(git.dirty, git.staged, git.log, 0)
                        }
                    }

                    "log" -> {
                        git.log.take(5).forEach { add(it, "mt") }
                    }

                    "pull" -> {
                        add("Updating main...")
                        add("Fast-forward · 3 files changed", "ok")
                        add("Already up to date.", "ok")
                    }

                    else -> add("git: '$arg1' is not supported in this shell", "er")
                }
            }

            "npm" -> {
                add("> ${project.name} $arg1", "mt")
                when (arg1) {
                    "test" -> {
                        add("PASS src/app.test.js (3 tests)", "ok")
                        add("PASS src/server.test.ts (2 tests)", "ok")
                        add("Test Suites: 2 passed, 2 total", "ok")
                        add("Tests:       5 passed, 5 total", "ok")
                        add("Snapshots:   0 total", "mt")
                        add("Time:        0.842 s", "mt")
                    }
                    "run" -> {
                        add("Building bundle with esbuild...", "mt")
                        add("Generated dist/server.js (14.2 KB)", "ok")
                        add("⚡ Build completed in 24ms", "ok")
                    }
                    "install", "i" -> {
                        add("added 42 packages, audited 43 packages in 1.4s", "ok")
                        add("found 0 vulnerabilities", "ok")
                    }
                    "start" -> {
                        add("Server listening on port 3000 at http://localhost:3000", "ok")
                    }
                    else -> add("done in 1.2s", "ok")
                }
            }

            "docker-compose", "docker" -> {
                if (arg1 == "ps") {
                    add("NAME                 IMAGE        COMMAND             SERVICE   STATUS", "mt")
                    add("${project.name}_web_1   node:20      \"node dist/serv…\"   web       Up (healthy)", "ok")
                    add("${project.name}_api_1   express:4    \"npm start\"        api       Up", "ok")
                } else if (arg1 == "logs") {
                    add("[web] 2026-10-07 13:00:00 [info] Application initialized", "mt")
                    add("[web] 2026-10-07 13:00:01 [info] Ready on http://localhost:3000", "ok")
                } else {
                    add("Restarting web ... done", "ok")
                    add("Restarting api ... done", "ok")
                }
            }

            "curl" -> {
                if (arg1.isEmpty()) {
                    add("usage: curl <url>", "er")
                } else {
                    add("HTTP/1.1 200 OK", "ok")
                    add("Content-Type: application/json", "mt")
                    add("{\"status\":\"ok\",\"service\":\"${project.name}\",\"time\":${System.currentTimeMillis()}}", "ok")
                }
            }

            "ping" -> {
                val host = arg1.ifEmpty { "127.0.0.1" }
                add("PING $host (56 data bytes)", "mt")
                add("64 bytes from $host: icmp_seq=1 ttl=64 time=0.042 ms", "ok")
                add("64 bytes from $host: icmp_seq=2 ttl=64 time=0.038 ms", "ok")
                add("--- $host ping statistics ---", "mt")
                add("2 packets transmitted, 2 received, 0% packet loss", "ok")
            }

            "env" -> {
                add("NODE_ENV=development", "mt")
                add("PORT=3000", "mt")
                add("FORGE_PROJECT=${project.name}", "ok")
                add("USER=${user.name}", "mt")
                add("SHELL=/bin/bash", "mt")
            }

            "history" -> {
                val sampleHist = listOf("git status", "npm test", "docker-compose restart", "forge deploy", "ls -la")
                sampleHist.forEachIndexed { i, h -> add("  ${i + 1}  $h", "mt") }
            }

            "whoami" -> {
                add("${user.name} (${user.email})")
            }

            "date" -> {
                add(java.util.Date().toString())
            }

            "forge" -> {
                val sub = arg1
                when (sub) {
                    "", "help" -> {
                        add("Forge CLI v1.4.0 — Mobile & Desktop Developer Tools", "cm")
                        add("  forge login | whoami | projects")
                        add("  forge deploy | agent \"<task>\"")
                        add("  forge vault sync | plugins | tools")
                        add("Install on workstation: npm i -g @usafe/forge-cli", "mt")
                    }
                    "version" -> add("forge-cli 1.4.0 (arm64-android)", "ok")
                    "whoami" -> add("${user.email} · ${if (user.plan == "one") "uSafe One" else "Free"}")
                    "login" -> add("Signed in via auth.usafe.in", "ok")
                    "projects" -> {
                        add("* ${project.name} (active)", "ok")
                    }
                    "deploy" -> {
                        add("Building bundle for production...", "mt")
                        if (project.tools.ssh.on) {
                            add("Syncing artifacts to ${project.tools.ssh.user}@${project.tools.ssh.host}...", "mt")
                            add("Restarting systemd service...", "mt")
                            add("Deployed to ${project.tools.ssh.host}:3000", "ok")
                        } else {
                            add("Deploying to Forge Edge CDN...", "mt")
                            add("Deployed to https://${project.name}.forge.live", "ok")
                        }
                    }
                    "agent" -> {
                        val task = command.substringAfter("agent").trim().trim('"')
                        add("Agent queued: $task", "ok")
                    }
                    "vault" -> {
                        if (project.tools.vault.on) {
                            add("Synced ${project.files.size} files to uVault ${project.tools.vault.folder}", "ok")
                        } else {
                            add("No uVault folder configured. Turn on Auto sync first.", "er")
                        }
                    }
                    "plugins" -> {
                        if (project.plugins.isNotEmpty()) {
                            add("Active plugins: ${project.plugins.joinToString(", ")}", "ok")
                        } else {
                            add("No plugins installed")
                        }
                    }
                    "tools" -> {
                        val activeCount = ForgeConstants.TOOL14.count { project.ptools[it.id] != false }
                        add("$activeCount of 14 built-in agent tools enabled", "ok")
                    }
                    else -> add("forge: unknown command '$sub'. Run 'forge help'", "er")
                }
            }

            "echo" -> {
                add(command.substringAfter("echo").trim())
            }

            "ssh" -> {
                if (project.tools.ssh.on) {
                    add("Connecting to ${project.tools.ssh.user}@${project.tools.ssh.host}...", "cm")
                    add("Authenticated via ${if (project.tools.ssh.how == "key") "OpenSSH key" else "Password"}", "ok")
                    add("Linux ${project.name}-node 5.15.0-88-generic (aarch64)", "mt")
                    add("Last login: ${java.util.Date()} from 192.168.1.10", "mt")
                    add("Session active. Type 'exit' to return to Forge.", "ok")
                } else {
                    add("No SSH server configured for ${project.name}. Turn on SSH servers in Tools Deck.", "er")
                }
            }

            else -> {
                if (command.startsWith("#") || command.startsWith("?")) {
                    val query = command.drop(1).trim()
                    add("Aura AI query: \"$query\"", "mt")
                    when {
                        query.contains("restart", ignoreCase = true) -> add("Recommended: docker-compose restart", "ok")
                        query.contains("deploy", ignoreCase = true) -> add("Recommended: forge deploy", "ok")
                        query.contains("test", ignoreCase = true) -> add("Recommended: npm test", "ok")
                        query.contains("commit", ignoreCase = true) -> add("Recommended: git add . && git commit -m \"update\"", "ok")
                        query.contains("tree", ignoreCase = true) -> add("Recommended: tree", "ok")
                        else -> add("Recommended: forge agent \"$query\"", "ok")
                    }
                } else {
                    add("command not found: $primary. Type 'forge help' or '# <question>'", "er")
                }
            }
        }

        return output
    }

    fun getSuggestions(input: String, project: Project): List<Pair<String, String>> {
        val q = input.trim()
        val auraModels = listOf("Aura Flash", "Aura Code", "Aura Deep")

        if (q.startsWith("#") || q.startsWith("?")) {
            val n = q.drop(1).lowercase()
            val list = when {
                n.contains("restart") -> listOf("docker-compose restart")
                n.contains("deploy") -> listOf("forge deploy")
                n.contains("test") -> listOf("npm test")
                n.contains("build") -> listOf("gradle build", "npm run build")
                n.contains("commit") || n.contains("save") -> listOf("git add .", "git commit -m \"update\"")
                n.contains("push") -> listOf("git push")
                n.contains("file") || n.contains("list") -> listOf("ls -la", "tree")
                else -> listOf("forge agent \"${q.drop(1).trim()}\"")
            }
            return list.take(3).mapIndexed { i, cmd -> cmd to auraModels[i % auraModels.size] }
        }

        if (q.isEmpty()) {
            val list = if (project.git.dirty.isNotEmpty()) {
                listOf("git status", "git add .", "git commit -m \"update\"")
            } else {
                listOf("npm test", "tree", "docker-compose restart")
            }
            return list.mapIndexed { i, cmd -> cmd to auraModels[i % auraModels.size] }
        }

        val allCandidates = listOf(
            "git status", "git diff", "git add .", "git commit -m \"update\"", "git push",
            "git log", "git pull origin main", "gradle build", "gradle test",
            "npm test", "npm run build", "npm install", "docker-compose restart",
            "docker ps", "ls -la", "tree", "pwd", "clear", "curl http://localhost:3000",
            "forge help", "forge whoami", "forge projects", "forge deploy",
            "forge agent \"task\"", "forge vault sync", "forge plugins", "forge tools",
            "ssh"
        ) + project.files.keys.map { "cat $it" }

        return allCandidates
            .filter { it.startsWith(q, ignoreCase = true) && it != q }
            .take(3)
            .mapIndexed { i, cmd -> cmd to auraModels[i % auraModels.size] }
    }
}
