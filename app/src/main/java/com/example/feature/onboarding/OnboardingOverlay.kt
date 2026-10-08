package com.example.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GitHubConfig
import com.example.domain.model.Project
import com.example.domain.model.ProjectTools
import com.example.domain.model.SshConfig
import com.example.domain.model.VaultConfig
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun OnboardingOverlay(
    isOpen: Boolean,
    onComplete: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var step by remember { mutableStateOf(0) }
    var projectName by remember { mutableStateOf("my-project") }
    var host by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("root") }
    var vaultFolder by remember { mutableStateOf("/projects/my-project") }

    var sshConnected by remember { mutableStateOf(false) }
    var ghConnected by remember { mutableStateOf(false) }
    var vaultConnected by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .statusBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual Node Hub: Central F + 3 Satellites
            Box(
                modifier = Modifier
                    .size(240.dp, 160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Connection Lines Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val hubCenter = Offset(size.width * 0.5f, size.height * 0.45f)
                    val satSsh = Offset(size.width * 0.18f, size.height * 0.24f)
                    val satGh = Offset(size.width * 0.82f, size.height * 0.24f)
                    val satVault = Offset(size.width * 0.5f, size.height * 0.90f)

                    drawLine(
                        color = if (sshConnected || step == 1) Emerald400 else Slate700,
                        start = hubCenter,
                        end = satSsh,
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = if (ghConnected || step == 2) Emerald400 else Slate700,
                        start = hubCenter,
                        end = satGh,
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = if (vaultConnected || step == 3) Emerald400 else Slate700,
                        start = hubCenter,
                        end = satVault,
                        strokeWidth = 3f
                    )
                }

                // Central Hub
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Emerald400),
                    contentAlignment = Alignment.Center
                ) {
                    Text("F", color = Slate950, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                // Sat 1: SSH
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 8.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sshConnected) EmeraldTint else Slate800)
                        .border(1.dp, if (sshConnected) Emerald400 else Slate700, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▣", color = if (sshConnected) Emerald400 else Slate400, fontSize = 16.sp)
                }

                // Sat 2: GitHub
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 8.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (ghConnected) EmeraldTint else Slate800)
                        .border(1.dp, if (ghConnected) Emerald400 else Slate700, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⎇", color = if (ghConnected) Emerald400 else Slate400, fontSize = 16.sp)
                }

                // Sat 3: uVault
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (vaultConnected) EmeraldTint else Slate800)
                        .border(1.dp, if (vaultConnected) Emerald400 else Slate700, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◈", color = if (vaultConnected) Emerald400 else Slate400, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Dots (0..4)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..4).forEach { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == step) 22.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .background(if (idx == step) Emerald400 else if (idx < step) EmeraldTint else Slate700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Content
            val titles = listOf(
                "Create your first project",
                "Connect a server",
                "Connect GitHub",
                "Sync to uVault",
                "You are set!"
            )

            Text(
                text = titles[step],
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Slate100,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                0 -> {
                    Text(
                        text = "Just a name. We set up the rest with matching defaults.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = projectName,
                            onValueChange = {
                                projectName = it
                                vaultFolder = "/projects/$it"
                            },
                            textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate100),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_project_name_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald400)
                            .clickable { step = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create project", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
                    }
                }

                1 -> {
                    Text(
                        text = "Add a server for $projectName. Add keys or password in the tools deck.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (host.isEmpty()) Text("178.62.14.90", color = Slate700, fontSize = 14.sp)
                        BasicTextField(
                            value = host,
                            onValueChange = { host = it },
                            textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.5.sp, color = Slate100),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald400)
                            .clickable {
                                sshConnected = true
                                step = 2
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Connect server", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800)
                            .clickable { step = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Skip", fontFamily = InterFontFamily, fontSize = 13.sp, color = Slate100)
                    }
                }

                2 -> {
                    Text(
                        text = "Creates the repo mara-dev/$projectName and turns on auto-commit.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald400)
                            .clickable {
                                ghConnected = true
                                step = 3
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Connect GitHub", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800)
                            .clickable { step = 3 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Skip", fontFamily = InterFontFamily, fontSize = 13.sp, color = Slate100)
                    }
                }

                3 -> {
                    Text(
                        text = "Folder in your uVault, named after the project.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = vaultFolder,
                            onValueChange = { vaultFolder = it },
                            textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.5.sp, color = Slate100),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald400)
                            .clickable {
                                vaultConnected = true
                                step = 4
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create folder and sync", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate950)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate800)
                            .clickable { step = 4 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Skip", fontFamily = InterFontFamily, fontSize = 13.sp, color = Slate100)
                    }
                }

                4 -> {
                    Text(
                        text = "$projectName is ready${if (sshConnected) " · server" else ""}${if (ghConnected) " · GitHub" else ""}${if (vaultConnected) " · uVault $vaultFolder" else ""}.",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = Slate100,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Emerald400)
                            .clickable {
                                val cleanName = projectName.trim().lowercase().replace(Regex("[^a-z0-9-]+"), "-")
                                val newProj = Project(
                                    id = cleanName,
                                    name = cleanName,
                                    files = mapOf(
                                        "index.html" to "<!DOCTYPE html><html><body style='background:#0F172A;color:#F1F5F9;font-family:system-ui;padding:24px'><h1>$cleanName</h1><p>Ready to build with Forge agent.</p></body></html>",
                                        "README.md" to "# $cleanName"
                                    ),
                                    openFile = "index.html",
                                    tools = ProjectTools(
                                        ssh = SshConfig(on = sshConnected, user = user, host = host),
                                        gh = GitHubConfig(on = ghConnected, repo = "mara-dev/$cleanName", auto = true),
                                        vault = VaultConfig(on = vaultConnected, folder = vaultFolder)
                                    )
                                )
                                onComplete(newProj)
                            }
                            .testTag("onboarding_open_workspace_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Open workspace", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate950)
                    }
                }
            }
        }
    }
}
