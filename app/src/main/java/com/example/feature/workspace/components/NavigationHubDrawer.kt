package com.example.feature.workspace.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ProjectFileEntity
import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
import com.example.domain.model.UserSession
import com.example.feature.editor.components.HierarchicalFileExplorer
import com.example.feature.workspace.ActiveModal
import com.example.ui.theme.Amber400
import com.example.ui.theme.AmberTint
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.GlassHighlightOverlay
import com.example.ui.theme.GlossyBorderStroke
import com.example.ui.theme.GlossyCardGradient
import com.example.ui.theme.GlossyEmeraldStroke
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
fun NavigationHubDrawer(
    isOpen: Boolean,
    user: UserSession,
    projects: List<Project>,
    currentProject: Project,
    roomFiles: List<ProjectFileEntity>,
    onClose: () -> Unit,
    onSelectProject: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onOpenModal: (ActiveModal) -> Unit,
    onToggleTool: (String) -> Unit,
    onToggleByok: () -> Unit,
    onOpenPlugins: () -> Unit,
    onOpenTools: () -> Unit,
    onAddFile: (String) -> Unit,
    onAddFolder: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (oldPath: String, newPath: String) -> Unit,
    onToggleFolderExpanded: (path: String, isExpanded: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var isSidebarExplorerOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950.copy(alpha = 0.75f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(GlossyCardGradient)
                .border(GlossyBorderStroke, RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
                .testTag("navigation_drawer")
        ) {
            // Header: Avatar, Name, Email, Plan, Close ✕
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldTint)
                            .border(GlossyEmeraldStroke, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercase() ?: "M",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Emerald300
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Slate100
                        )
                        Text(
                            text = user.email,
                            fontFamily = InterFontFamily,
                            fontSize = 11.5.sp,
                            color = Slate400
                        )
                    }

                    // Plan Chip
                    val isOne = user.plan == "one"
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isOne) Emerald400 else AmberTint)
                            .border(1.dp, if (isOne) Emerald400 else Amber400, CircleShape)
                            .clickable { onOpenModal(ActiveModal.Membership()) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOne) "✦ One" else "Upgrade",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isOne) Slate950 else Amber400
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate800)
                            .border(GlossyBorderStroke, RoundedCornerShape(10.dp))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Slate100, fontSize = 13.sp)
                    }
                }
            }

            // Section 1: Recent Projects Carousel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT PROJECTS",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.sp,
                        color = Slate400
                    )
                    Text(
                        text = "${projects.size}/${if (user.plan == "one") 10 else 3}",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    projects.forEach { proj ->
                        val isSelected = proj.id == currentProject.id
                        val toolsCount = listOf(proj.tools.ssh.on, proj.tools.gh.on, proj.tools.vault.on).count { it }

                        Column(
                            modifier = Modifier
                                .width(138.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate800.copy(alpha = 0.9f))
                                .border(
                                    if (isSelected) GlossyEmeraldStroke else GlossyBorderStroke,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectProject(proj.id) }
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Slate950)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.BottomStart)
                                        .height(6.dp)
                                        .width(50.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Emerald400)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = proj.name,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Slate100,
                                maxLines = 1
                            )
                            Text(
                                text = "$toolsCount tools on",
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                color = Slate400
                            )
                        }
                    }

                    // + New project card
                    Box(
                        modifier = Modifier
                            .width(138.dp)
                            .height(118.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800.copy(alpha = 0.6f))
                            .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onOpenModal(ActiveModal.NewProject()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "+",
                                fontSize = 28.sp,
                                color = Emerald400,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "New project",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Emerald400
                            )
                        }
                    }
                }
            }

            // Section 2: Sidebar Hierarchical File Explorer (Room Database)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .clickable { isSidebarExplorerOpen = !isSidebarExplorerOpen }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📁", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PROJECT FILES (ROOM DB)",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Emerald300
                        )
                    }
                    Text(if (isSidebarExplorerOpen) "▲" else "▼", fontSize = 11.sp, color = Slate400)
                }

                AnimatedVisibility(visible = isSidebarExplorerOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 6.dp)
                    ) {
                        HierarchicalFileExplorer(
                            projectName = currentProject.name,
                            files = if (roomFiles.isNotEmpty()) roomFiles else currentProject.files.map { (p, c) ->
                                ProjectFileEntity(
                                    id = "${currentProject.id}:$p",
                                    projectId = currentProject.id,
                                    path = p,
                                    name = p.substringAfterLast('/'),
                                    parentPath = if (p.contains('/')) p.substringBeforeLast('/') else "",
                                    isDirectory = false,
                                    content = c,
                                    extension = p.substringAfterLast('.', "")
                                )
                            },
                            selectedFile = currentProject.openFile,
                            onOpenFile = {
                                onOpenFile(it)
                                onClose()
                            },
                            onAddFile = { onAddFile(it) },
                            onAddFolder = { onAddFolder(it) },
                            onDeleteFile = onDeleteFile,
                            onRenameFile = onRenameFile,
                            onToggleFolder = onToggleFolderExpanded,
                            onUploadClick = { onOpenModal(ActiveModal.UploadFile) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Section 3: Tools Deck
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "TOOLS DECK · ${currentProject.name}",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.08.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SSH servers
                ToolRow(
                    icon = "▣",
                    title = "SSH servers",
                    subtitle = if (!currentProject.tools.ssh.on) "Add a server" else "${currentProject.tools.ssh.user}@${currentProject.tools.ssh.host} · ${if (currentProject.tools.ssh.how == "key") "key" else "password"}",
                    isOn = currentProject.tools.ssh.on,
                    onToggle = { onToggleTool("ssh") },
                    onEdit = { onOpenModal(ActiveModal.Ssh) }
                )

                // GitHub auto-commit
                ToolRow(
                    icon = "⎇",
                    title = "GitHub auto-commit",
                    subtitle = if (!currentProject.tools.gh.on) "Connect and pick a repo" else "${currentProject.tools.gh.repo}${if (currentProject.tools.gh.auto) " · auto-commit" else ""}",
                    isOn = currentProject.tools.gh.on,
                    onToggle = { onToggleTool("gh") },
                    onEdit = { onOpenModal(ActiveModal.GitHub) }
                )

                // uVault auto sync
                ToolRow(
                    icon = "◈",
                    title = "Auto sync to uVault",
                    subtitle = if (!currentProject.tools.vault.on) "Pick a uVault folder" else currentProject.tools.vault.folder,
                    isOn = currentProject.tools.vault.on,
                    onToggle = { onToggleTool("vault") },
                    onEdit = { onOpenModal(ActiveModal.Vault) }
                )
            }

            // Section 4: Agents
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AGENTS",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.sp,
                        color = Slate400
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                            .border(GlossyBorderStroke, RoundedCornerShape(8.dp))
                            .clickable { onOpenModal(ActiveModal.AgentBuilder) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "＋ Build",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val allAgents = listOf(
                    com.example.domain.model.Agent(
                        name = "Forge agent",
                        role = "General assistant",
                        skills = listOf("General coding")
                    )
                ) + currentProject.agents

                allAgents.forEach { ag ->
                    val isSelected = ag.name == currentProject.agent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) EmeraldTint else Slate800.copy(alpha = 0.8f))
                            .border(
                                if (isSelected) GlossyEmeraldStroke else GlossyBorderStroke,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectProject(currentProject.id) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Slate950),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⬡", color = Emerald400, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = ag.name,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Slate100
                            )
                            Text(
                                text = ag.skills.take(2).joinToString(" · "),
                                fontFamily = InterFontFamily,
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Section 5: BYOK Inference
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "BYOK INFERENCE",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.08.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate800.copy(alpha = 0.9f))
                        .border(GlossyBorderStroke, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bring your own key",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Slate100
                            )
                            ToggleSwitch(
                                checked = currentProject.byok.on,
                                onCheckedChange = { onToggleByok() }
                            )
                        }

                        if (currentProject.byok.on) {
                            val provDef = ForgeConstants.PROVIDERS[currentProject.byok.prov]
                            val savedKey = currentProject.byok.saved[currentProject.byok.prov]

                            Text(
                                text = "Provider: ${provDef?.displayName ?: "AWS Bedrock"}",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = Slate400
                            )

                            if (!savedKey.isNullOrEmpty()) {
                                Text(
                                    text = "✓ Saved · key ending $savedKey",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.5.sp,
                                    color = Emerald400,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Turn on to use your own inference account.",
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                color = Slate400
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Model Button
                val curModel = ForgeConstants.MODELS.find { it.id == currentProject.model } ?: ForgeConstants.MODELS[1]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldTint)
                        .border(GlossyEmeraldStroke, RoundedCornerShape(12.dp))
                        .clickable { onOpenModal(ActiveModal.ModelSelector) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active model: ${curModel.displayName}",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = Emerald400
                    )
                    Text("›", fontSize = 16.sp, color = Emerald400, fontWeight = FontWeight.Bold)
                }
            }

            // Section 6: Usage
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "USAGE",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.08.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vault Disk Space Bar
                val maxGb = if (user.plan == "one") 100.0 else 5.0
                val vaultFraction = (user.vaultUsedGb / maxGb).coerceIn(0.0, 1.0).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("uVault disk space", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400)
                    Text("${"%.1f".format(user.vaultUsedGb)} / ${maxGb.toInt()} GB", fontFamily = InterFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald400)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Slate700)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(vaultFraction)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Emerald400)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tokens Bar
                val maxTokens = if (user.plan == "one") 2_000_000L else 100_000L
                val tokensLeft = (maxTokens - user.tokensUsed).coerceAtLeast(0L)
                val tokenFraction = (user.tokensUsed.toFloat() / maxTokens).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Forge Co-Pilot tokens", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400)
                    Text("$tokensLeft left", fontFamily = InterFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald400)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Slate700)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(tokenFraction)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (tokenFraction > 0.85f) Amber400 else Emerald400)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Sticky Footer: Plugins & Tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(GlossyBorderStroke, RoundedCornerShape(12.dp))
                        .clickable { onOpenPlugins() }
                        .testTag("drawer_plugins_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧩 Plugins",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Slate100
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(GlossyBorderStroke, RoundedCornerShape(12.dp))
                        .clickable { onOpenTools() }
                        .testTag("drawer_tools_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛠 Tools",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Slate100
                    )
                }
            }
        }
    }
}

@Composable
fun ToolRow(
    icon: String,
    title: String,
    subtitle: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(width = 0.5.dp, color = Slate800)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Slate800)
                .border(GlossyBorderStroke, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 16.sp, color = Slate100)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() }
        ) {
            Text(
                text = title,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Slate100
            )
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                fontSize = 11.5.sp,
                color = Slate400,
                maxLines = 1
            )
        }

        ToggleSwitch(
            checked = isOn,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CircleShape)
            .background(if (checked) Emerald400 else Slate700)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (checked) Slate950 else Slate100)
        )
    }
}
