package com.example.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.domain.model.ChatMessage
import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
import com.example.ui.theme.Amber400
import com.example.ui.theme.AmberTint
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
fun ChatPane(
    project: Project,
    isAgentBusy: Boolean,
    onSendMessage: (String) -> Unit,
    onOpenModelSelector: () -> Unit,
    onCycleAgent: () -> Unit,
    onApproveCommand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var inputMessage by remember { mutableStateOf("") }

    LaunchedEffect(project.messages.size) {
        if (project.messages.isNotEmpty()) {
            listState.animateScrollToItem(project.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Conversation History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(project.messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onApproveCommand = onApproveCommand
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Composer Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview Shared with Agent Tag
            if (project.preview.isShared) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👁 Preview shared with agent",
                        fontFamily = InterFontFamily,
                        fontSize = 11.5.sp,
                        color = Emerald400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Chips Row: Model & Agent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentModel = ForgeConstants.MODELS.find { it.id == project.model } ?: ForgeConstants.MODELS[1]

                // Model Selector Chip
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .clickable { onOpenModelSelector() }
                        .padding(horizontal = 12.dp)
                        .testTag("model_selector_chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◈ ${currentModel.displayName} ▾",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald400
                    )
                }

                // Agent Cycle Chip
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .clickable { onCycleAgent() }
                        .padding(horizontal = 12.dp)
                        .testTag("agent_chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⬡ ${project.agent} ▾",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate100
                    )
                }
            }

            // Input Row & Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputMessage.isEmpty()) {
                        Text(
                            text = "Ask the agent…",
                            color = Slate500,
                            fontSize = 14.sp,
                            fontFamily = InterFontFamily
                        )
                    }
                    BasicTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        textStyle = TextStyle(
                            color = Slate100,
                            fontSize = 14.sp,
                            fontFamily = InterFontFamily
                        ),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field")
                    )
                }

                // Emerald Send Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAgentBusy) Slate700 else Emerald400)
                        .clickable(enabled = !isAgentBusy && inputMessage.isNotBlank()) {
                            onSendMessage(inputMessage)
                            inputMessage = ""
                        }
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↑",
                        color = if (isAgentBusy) Slate400 else Slate950,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onApproveCommand: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
    ) {
        when (message) {
            is ChatMessage.User -> {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp))
                            .background(Emerald400)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = Slate950,
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            is ChatMessage.Assistant -> {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = Slate100,
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            is ChatMessage.ToolCall -> {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (message.status == "ok") Emerald400 else Amber400)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${message.icon} ${message.tool}",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = Slate100
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.argument,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                color = Slate400,
                                maxLines = 1
                            )
                        }
                        if (message.result.isNotEmpty()) {
                            Text(
                                text = message.result,
                                fontFamily = InterFontFamily,
                                fontSize = 11.sp,
                                color = if (message.status == "ok") Emerald400 else Slate400
                            )
                        }
                    }
                }
            }

            is ChatMessage.AskApproval -> {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberTint)
                        .border(1.dp, Amber400, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Amber400)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run command?",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.command,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = Emerald400
                        )
                    }

                    if (message.status == "wait") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald400)
                                    .clickable { onApproveCommand(true) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Allow",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Slate950
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate800)
                                    .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                    .clickable { onApproveCommand(false) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Skip",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Slate100
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (message.status == "ok") "✓ Allowed" else "✕ Skipped",
                            fontFamily = InterFontFamily,
                            fontSize = 11.5.sp,
                            color = if (message.status == "ok") Emerald400 else Slate400
                        )
                    }
                }
            }

            is ChatMessage.Terminal -> {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                ) {
                    // Mac-style mini header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Slate700))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Slate700))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Slate700))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Forge agent · terminal",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (message.isDone) "Done" else "Running",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = if (message.isDone) Emerald400 else Amber400
                        )
                    }

                    // Monospace terminal lines
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.lines.forEach { (text, style) ->
                            Text(
                                text = text,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = when (style) {
                                    "cm" -> Slate100
                                    "ok" -> Emerald400
                                    "er" -> Amber400
                                    else -> Emerald400.copy(alpha = 0.85f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
