package com.example.feature.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Agent
import com.example.domain.model.ForgeConstants
import com.example.domain.model.GitHubConfig
import com.example.domain.model.Project
import com.example.domain.model.SshConfig
import com.example.domain.model.UserSession
import com.example.domain.model.VaultConfig
import com.example.feature.workspace.ActiveModal
import com.example.ui.theme.Amber400
import com.example.ui.theme.AmberTint
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun ForgeModalHost(
    activeModal: ActiveModal,
    project: Project,
    user: UserSession,
    onDismiss: () -> Unit,
    onCreateProject: (name: String, kind: String) -> Unit,
    onSaveSsh: (SshConfig) -> Unit,
    onSaveGitHub: (GitHubConfig) -> Unit,
    onSaveVault: (VaultConfig) -> Unit,
    onSelectModel: (String) -> Unit,
    onCreateAgent: (Agent) -> Unit,
    onCreateFile: (filename: String) -> Unit,
    onCreateFolder: (folderPath: String) -> Unit,
    onUpgradePlan: (String) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeModal == ActiveModal.None) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Slate900)
                .border(1.dp, Slate700, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {}
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            when (activeModal) {
                is ActiveModal.NewProject -> {
                    NewProjectContent(
                        onDismiss = onDismiss,
                        onCreate = onCreateProject,
                        onToast = onToast
                    )
                }

                is ActiveModal.Ssh -> {
                    SshConfigContent(
                        currentSsh = project.tools.ssh,
                        onDismiss = onDismiss,
                        onSave = onSaveSsh,
                        onToast = onToast
                    )
                }

                is ActiveModal.GitHub -> {
                    GitHubConfigContent(
                        currentGh = project.tools.gh,
                        projectName = project.name,
                        onDismiss = onDismiss,
                        onSave = onSaveGitHub,
                        onToast = onToast
                    )
                }

                is ActiveModal.Vault -> {
                    VaultConfigContent(
                        currentVault = project.tools.vault,
                        projectName = project.name,
                        onDismiss = onDismiss,
                        onSave = onSaveVault,
                        onToast = onToast
                    )
                }

                is ActiveModal.ModelSelector -> {
                    ModelSelectorContent(
                        project = project,
                        user = user,
                        onDismiss = onDismiss,
                        onSelectModel = onSelectModel,
                        onToast = onToast
                    )
                }

                is ActiveModal.AgentBuilder -> {
                    AgentBuilderContent(
                        onDismiss = onDismiss,
                        onCreate = onCreateAgent,
                        onToast = onToast
                    )
                }

                is ActiveModal.Membership -> {
                    MembershipContent(
                        user = user,
                        reason = activeModal.reason,
                        onDismiss = onDismiss,
                        onUpgrade = onUpgradePlan,
                        onToast = onToast
                    )
                }

                is ActiveModal.NewFile -> {
                    NewFileContent(
                        isFolder = false,
                        initialParentDir = activeModal.parentDir,
                        onDismiss = onDismiss,
                        onCreate = onCreateFile,
                        onToast = onToast
                    )
                }

                is ActiveModal.NewFolder -> {
                    NewFileContent(
                        isFolder = true,
                        initialParentDir = activeModal.parentDir,
                        onDismiss = onDismiss,
                        onCreate = onCreateFolder,
                        onToast = onToast
                    )
                }

                is ActiveModal.UploadFile -> {
                    UploadFileContent(
                        onDismiss = onDismiss,
                        onToast = onToast
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun NewProjectContent(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    onToast: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf("Website") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("New project", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Starts with every tool off. Set them up per project.", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        // Project Name Input
        Column {
            Text("Project name", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate100),
                    cursorBrush = SolidColor(Emerald400),
                    modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                )
            }
        }

        // Segmented Kind Selector: Website | App | Blank
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Website", "App", "Blank").forEach { kind ->
                val isSelected = selectedKind == kind
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Slate950 else Slate800)
                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Emerald400 else Slate800, RoundedCornerShape(9.dp))
                        .clickable { selectedKind = kind },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = kind,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = if (isSelected) Emerald400 else Slate400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Create Project Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    if (name.isBlank()) {
                        onToast("Enter a project name")
                    } else {
                        onCreate(name, selectedKind)
                        onDismiss()
                        onToast("Project created")
                    }
                }
                .testTag("create_project_confirm_button"),
            contentAlignment = Alignment.Center
        ) {
            Text("Create project", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }

        // Cancel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Text("Cancel", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate100)
        }
    }
}

@Composable
fun SshConfigContent(
    currentSsh: SshConfig,
    onDismiss: () -> Unit,
    onSave: (SshConfig) -> Unit,
    onToast: (String) -> Unit
) {
    var host by remember { mutableStateOf(currentSsh.host) }
    var user by remember { mutableStateOf(currentSsh.user) }
    var how by remember { mutableStateOf(currentSsh.how) } // "pw" or "key"
    var password by remember { mutableStateOf("") }
    var hasKey by remember { mutableStateOf(currentSsh.hasKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add SSH server", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Secrets stay on this device and go to uVault encrypted.", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        // Host Input
        Column {
            Text("Host or IP", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = host,
                    onValueChange = { host = it },
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = Slate100),
                    cursorBrush = SolidColor(Emerald400),
                    modifier = Modifier.fillMaxWidth().testTag("ssh_host_input")
                )
            }
        }

        // User Input
        Column {
            Text("User", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = user,
                    onValueChange = { user = it },
                    textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = Slate100),
                    cursorBrush = SolidColor(Emerald400),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Segmented: User + password vs OpenSSH key file
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (how == "pw") Slate950 else Slate800)
                    .border(if (how == "pw") 1.dp else 0.dp, if (how == "pw") Emerald400 else Slate800, RoundedCornerShape(9.dp))
                    .clickable { how = "pw" },
                contentAlignment = Alignment.Center
            ) {
                Text("User + password", fontFamily = InterFontFamily, fontSize = 12.sp, color = if (how == "pw") Emerald400 else Slate400)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (how == "key") Slate950 else Slate800)
                    .border(if (how == "key") 1.dp else 0.dp, if (how == "key") Emerald400 else Slate800, RoundedCornerShape(9.dp))
                    .clickable { how = "key" },
                contentAlignment = Alignment.Center
            ) {
                Text("OpenSSH key file", fontFamily = InterFontFamily, fontSize = 12.sp, color = if (how == "key") Emerald400 else Slate400)
            }
        }

        if (how == "pw") {
            Column {
                Text("Password", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = Slate100),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier.fillMaxWidth().testTag("ssh_password_input")
                    )
                }
            }
        } else {
            // Key file drop zone
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (hasKey) Emerald400 else Slate700, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate950)
                    .clickable {
                        hasKey = true
                        onToast("OpenSSH key loaded (id_ed25519)")
                    }
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Upload OpenSSH key", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate100)
                Text("id_ed25519 or .pem", fontFamily = InterFontFamily, fontSize = 11.5.sp, color = Slate400)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (hasKey) EmeraldTint else Slate800)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(if (hasKey) "Key detected · id_ed25519" else "Tap to select key", fontSize = 11.sp, color = if (hasKey) Emerald400 else Slate400)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    if (host.isBlank()) {
                        onToast("Enter a host or IP")
                    } else if (how == "pw" && password.isBlank()) {
                        onToast("Enter password")
                    } else if (how == "key" && !hasKey) {
                        onToast("Upload private key")
                    } else {
                        onSave(SshConfig(on = true, user = user, host = host, how = how, hasKey = hasKey))
                        onDismiss()
                        onToast("Server connected")
                    }
                }
                .testTag("ssh_connect_button"),
            contentAlignment = Alignment.Center
        ) {
            Text("Connect server", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }
    }
}

@Composable
fun GitHubConfigContent(
    currentGh: GitHubConfig,
    projectName: String,
    onDismiss: () -> Unit,
    onSave: (GitHubConfig) -> Unit,
    onToast: (String) -> Unit
) {
    val sampleRepos = listOf("mara-dev/billing-api", "mara-dev/auth-gateway", "mara-dev/marketing-site", "mara-dev/forge-labs")
    var selectedRepo by remember { mutableStateOf(if (currentGh.repo.isNotEmpty()) currentGh.repo else "mara-dev/$projectName") }
    var autoCommit by remember { mutableStateOf(currentGh.auto) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Auto-commit repo", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Signed in as @mara-dev", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        // Pick a repo
        sampleRepos.forEach { r ->
            val isSelected = selectedRepo == r
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) EmeraldTint else Slate800)
                    .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(12.dp))
                    .clickable { selectedRepo = r }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Emerald400 else Slate700)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(r, fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = Slate100)
            }
        }

        // Auto commit toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .clickable { autoCommit = !autoCommit }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (autoCommit) Emerald400 else Slate700)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Auto-commit on save", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate100)
                Text("Agent commits every accepted change", fontFamily = InterFontFamily, fontSize = 11.5.sp, color = Slate400)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    onSave(GitHubConfig(on = true, repo = selectedRepo, auto = autoCommit))
                    onDismiss()
                    onToast("GitHub repo linked")
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Save & Link", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }
    }
}

@Composable
fun VaultConfigContent(
    currentVault: VaultConfig,
    projectName: String,
    onDismiss: () -> Unit,
    onSave: (VaultConfig) -> Unit,
    onToast: (String) -> Unit
) {
    val folders = listOf("/projects", "/projects/backups", "/forge", "/projects/$projectName")
    var selectedFolder by remember { mutableStateOf(if (currentVault.folder.isNotEmpty()) currentVault.folder else "/projects/$projectName") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Auto sync to uVault", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Choose the folder in your uVault for this project.", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        folders.forEach { f ->
            val isSelected = selectedFolder == f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) EmeraldTint else Slate800)
                    .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(12.dp))
                    .clickable { selectedFolder = f }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Emerald400 else Slate700)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(f, fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp, color = Slate100)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    onSave(VaultConfig(on = true, folder = selectedFolder))
                    onDismiss()
                    onToast("Syncing to uVault")
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Start auto sync", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }
    }
}

@Composable
fun ModelSelectorContent(
    project: Project,
    user: UserSession,
    onDismiss: () -> Unit,
    onSelectModel: (String) -> Unit,
    onToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Select model", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Only models from connected API keys or plans are available.", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        ForgeConstants.MODELS.forEach { model ->
            val isSelected = model.id == project.model
            val isAvailable = when {
                model.planRequirement == "one" -> user.plan == "one"
                model.requiredProvider.isNotEmpty() -> project.byok.on && !project.byok.saved[model.requiredProvider].isNullOrEmpty()
                else -> true
            }

            val subtitle = when {
                model.planRequirement == "one" -> if (user.plan == "one") "Included with uSafe One" else "Requires uSafe One membership"
                model.requiredProvider.isNotEmpty() -> {
                    val provName = ForgeConstants.PROVIDERS[model.requiredProvider]?.displayName ?: "BYOK"
                    if (isAvailable) "Connected via $provName" else "Connect $provName in drawer"
                }
                else -> "Included with uSafe"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) EmeraldTint else Slate800)
                    .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(12.dp))
                    .clickable(enabled = isAvailable) {
                        onSelectModel(model.id)
                        onDismiss()
                        onToast("Model: ${model.displayName}")
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Emerald400 else if (isAvailable) Slate700 else Slate800)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.displayName,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isAvailable) Slate100 else Slate500
                    )
                    Text(
                        text = subtitle,
                        fontFamily = InterFontFamily,
                        fontSize = 11.5.sp,
                        color = if (isAvailable) Slate400 else Slate500
                    )
                }
            }
        }
    }
}

@Composable
fun AgentBuilderContent(
    onDismiss: () -> Unit,
    onCreate: (Agent) -> Unit,
    onToast: (String) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var agentName by remember { mutableStateOf("") }
    var agentRole by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf("Refactoring", "Testing", "Code review") }
    var customSkill by remember { mutableStateOf("") }
    val selectedTools = remember { mutableStateListOf("edit", "runCommands", "problems") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Build an agent", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Step ${step + 1} of 3", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400)

        if (step == 0) {
            Column {
                Text("Agent name", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 13.5.sp, color = Slate100),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier.fillMaxWidth().testTag("agent_name_input")
                    )
                }
            }

            Column {
                Text("What should it do?", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = agentRole,
                        onValueChange = { agentRole = it },
                        textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 13.5.sp, color = Slate100),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (step == 1) {
            Text("Skills it needs", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400)
            val availableSkills = listOf("Code review", "Refactoring", "Testing", "DevOps", "Security audit", "Docs writing", "SQL", "UI polish")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                availableSkills.take(4).forEach { s ->
                    val isHas = skills.contains(s)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isHas) EmeraldTint else Slate800)
                            .border(1.dp, if (isHas) Emerald400 else Slate700, RoundedCornerShape(8.dp))
                            .clickable { if (isHas) skills.remove(s) else skills.add(s) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(s, fontSize = 11.5.sp, color = if (isHas) Emerald400 else Slate100)
                    }
                }
            }
        }

        if (step == 2) {
            Text("Tools it can call", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400)
            ForgeConstants.TOOL14.take(6).forEach { t ->
                val isSelected = selectedTools.contains(t.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldTint else Slate800)
                        .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(10.dp))
                        .clickable { if (isSelected) selectedTools.remove(t.id) else selectedTools.add(t.id) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${t.icon} ${t.id}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = Slate100)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    if (step < 2) {
                        if (step == 0 && agentName.isBlank()) {
                            onToast("Name your agent")
                        } else {
                            step++
                        }
                    } else {
                        onCreate(Agent(name = agentName, role = agentRole, skills = skills.toList(), tools = selectedTools.toList()))
                        onDismiss()
                        onToast("Agent created")
                    }
                }
                .testTag("agent_wizard_next_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(if (step < 2) "Next" else "Create agent", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }
    }
}

@Composable
fun MembershipContent(
    user: UserSession,
    reason: String,
    onDismiss: () -> Unit,
    onUpgrade: (String) -> Unit,
    onToast: (String) -> Unit
) {
    val isOne = user.plan == "one"
    val benefits = listOf(
        Triple("Forge Co-Pilot tokens", "100K", "2M"),
        Triple("Aura auto-routes models", "—", "✓"),
        Triple("Projects per month", "3", "10"),
        Triple("uVault disk space", "5 GB", "100 GB"),
        Triple("VPN access", "—", "✓"),
        Triple("Privacy shield", "Basic", "Full")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Emerald400)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("✦ uSafe One", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate950)
        }

        Text(
            text = if (reason.isNotEmpty()) reason else "One plan. Everything.",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Slate100
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text("$5", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 42.sp, color = Slate100)
            Text(" / month", fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate400, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Benefits table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            benefits.forEach { (feat, free, one) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(feat, fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate100, modifier = Modifier.weight(1f))
                    Text(free, fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.width(50.dp))
                    Text(one, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald400, modifier = Modifier.width(60.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    val nextPlan = if (isOne) "free" else "one"
                    onUpgrade(nextPlan)
                    onDismiss()
                    onToast(if (isOne) "Back on Free" else "Welcome to uSafe One")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isOne) "Cancel membership" else "Get uSafe One · $5/mo",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate950
            )
        }
    }
}

@Composable
fun NewFileContent(
    isFolder: Boolean,
    initialParentDir: String = "",
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onToast: (String) -> Unit
) {
    var path by remember { mutableStateOf(if (initialParentDir.isNotEmpty()) "$initialParentDir/" else "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (isFolder) "New folder" else "New file", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text(if (isFolder) "Use a path like src/utils" else "Use a name like src/app.ts or index.html", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate950)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = path,
                onValueChange = { path = it },
                textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.5.sp, color = Slate100),
                cursorBrush = SolidColor(Emerald400),
                modifier = Modifier.fillMaxWidth().testTag("new_file_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Emerald400)
                .clickable {
                    if (path.isBlank()) {
                        onToast("Enter name")
                    } else {
                        onCreate(path.trim())
                        onDismiss()
                        onToast(if (isFolder) "Folder created" else "File created")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Create", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
        }
    }
}

@Composable
fun UploadFileContent(
    onDismiss: () -> Unit,
    onToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Upload files", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate100)
        Text("Supported: .ts .tsx .js .jsx .json .html .css .md .py .go .rs .sh .sql .txt. Up to 200 KB each.", fontFamily = InterFontFamily, fontSize = 12.5.sp, color = Slate400)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Slate950)
                .clickable {
                    onToast("2 files uploaded")
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Choose files", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Slate100)
                Text("text and code files", fontFamily = InterFontFamily, fontSize = 11.5.sp, color = Slate400)
            }
        }
    }
}
