package com.example.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.terminal.TerminalEngine
import com.example.domain.model.Project
import com.example.ui.theme.Amber400
import com.example.ui.theme.AmberTint
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.GlossyBorderStroke
import com.example.ui.theme.GlossyCardGradient
import com.example.ui.theme.GlossyEmeraldStroke
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun TerminalEmulatorComponent(
    project: Project,
    logs: List<TerminalEngine.OutputLine>,
    input: String,
    suggestions: List<Pair<String, String>>,
    onInputChange: (String) -> Unit,
    onExecute: (String) -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var activeTab by remember { mutableStateOf("sh:main") }
    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }

    val quickCommands = listOf(
        "npm test",
        "gradle build",
        "gradle test",
        "npm run build",
        "git status",
        "git diff",
        "docker-compose restart",
        "tree",
        "ls -la",
        "forge deploy",
        "forge vault sync",
        "clear"
    )

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(GlossyCardGradient)
            .border(GlossyBorderStroke, RoundedCornerShape(16.dp))
    ) {
        // Mac-style HUD Header with Traffic light dots and multi-tab sessions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate950.copy(alpha = 0.9f))
                .border(0.5.dp, Slate700, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Traffic light dots
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(CrimsonRed.copy(alpha = 0.8f)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Amber400.copy(alpha = 0.8f)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Emerald400.copy(alpha = 0.8f)))

                    Spacer(modifier = Modifier.width(6.dp))

                    // Active Tab Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TerminalTab(
                            title = "bash: ${project.name}",
                            isActive = activeTab == "sh:main",
                            onClick = { activeTab = "sh:main" }
                        )
                        TerminalTab(
                            title = "build & test",
                            isActive = activeTab == "sh:build",
                            onClick = {
                                activeTab = "sh:build"
                                onExecute("npm test")
                            }
                        )
                    }
                }

                // Status & Actions (Copy, Clear, Close)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Copy output
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .clickable {
                                val fullLog = logs.joinToString("\n") { it.text }
                                clipboardManager.setText(AnnotatedString(fullLog))
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("📋 Copy", fontSize = 10.5.sp, color = Slate400, fontFamily = InterFontFamily)
                    }

                    // Clear
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .clickable { onExecute("clear") }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("Clear", fontSize = 10.5.sp, color = Slate400, fontFamily = InterFontFamily)
                    }

                    onClose?.let { closeAction ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .clickable { closeAction() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 11.sp, color = Slate100)
                        }
                    }
                }
            }
        }

        // Quick CLI Commands Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900.copy(alpha = 0.7f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Slate800)
                        .border(0.5.dp, Slate700, RoundedCornerShape(7.dp))
                        .clickable {
                            if (!commandHistory.contains(cmd)) commandHistory.add(cmd)
                            onExecute(cmd)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cmd,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = if (cmd.startsWith("npm") || cmd.startsWith("gradle")) Emerald300 else if (cmd.startsWith("git")) SkyBlue else Slate100
                    )
                }
            }
        }

        // Console Stream View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Slate950.copy(alpha = 0.95f))
                .padding(10.dp)
                .testTag("terminal_emulator_console")
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = "Forge Studio Interactive Shell (v1.4.0-arm64)\nType CLI commands (e.g. 'gradle build', 'npm test', 'tree', 'git status') or '# <prompt>'\n",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.5.sp,
                            color = Slate500
                        )
                    }
                }
                items(logs) { line ->
                    Text(
                        text = line.text,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = when (line.style) {
                            "cm" -> Slate100
                            "ok" -> Emerald400
                            "er" -> CrimsonRed
                            "warn" -> Amber400
                            "mt" -> Slate500
                            else -> Emerald400.copy(alpha = 0.9f)
                        }
                    )
                }
            }
        }

        // Autocomplete suggestions chips (Aura assist)
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { (cmd, auraTag) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                            .border(0.5.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                onInputChange(cmd)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(cmd, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = Slate100)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldTint)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(auraTag, fontSize = 9.sp, color = Emerald400, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Input Prompt & Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .border(0.5.dp, Slate700, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Prompt prefix
                Text(
                    text = "${project.name} $",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Emerald400
                )

                // Text field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .border(1.dp, Emerald400.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (input.isEmpty()) {
                        Text(
                            text = "command, or # ask Aura",
                            fontFamily = JetBrainsMonoFontFamily,
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    }

                    BasicTextField(
                        value = input,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.5.sp,
                            color = Slate100
                        ),
                        cursorBrush = SolidColor(Emerald400),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (input.isNotBlank()) {
                                if (!commandHistory.contains(input)) commandHistory.add(input)
                                historyIndex = -1
                                onExecute(input)
                            }
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("terminal_emulator_input")
                    )
                }

                // History UP button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .border(0.5.dp, Slate700, RoundedCornerShape(8.dp))
                        .clickable(enabled = commandHistory.isNotEmpty()) {
                            if (commandHistory.isNotEmpty()) {
                                if (historyIndex == -1) historyIndex = commandHistory.size - 1
                                else if (historyIndex > 0) historyIndex--
                                onInputChange(commandHistory[historyIndex])
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▲", fontSize = 11.sp, color = if (commandHistory.isNotEmpty()) Slate100 else Slate600)
                }

                // History DOWN button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .border(0.5.dp, Slate700, RoundedCornerShape(8.dp))
                        .clickable(enabled = historyIndex != -1) {
                            if (historyIndex != -1 && historyIndex < commandHistory.size - 1) {
                                historyIndex++
                                onInputChange(commandHistory[historyIndex])
                            } else {
                                historyIndex = -1
                                onInputChange("")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▼", fontSize = 11.sp, color = if (historyIndex != -1) Slate100 else Slate600)
                }

                // Tab Complete button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Emerald400)
                        .clickable {
                            val top = suggestions.firstOrNull()?.first
                            if (!top.isNullOrEmpty()) {
                                onInputChange(top)
                            } else if (input.isNotBlank()) {
                                if (!commandHistory.contains(input)) commandHistory.add(input)
                                onExecute(input)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⇥", color = Slate950, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TerminalTab(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Slate900 else Slate800)
            .border(0.5.dp, if (isActive) Emerald400 else Slate700, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Emerald300 else Slate400
        )
    }
}
