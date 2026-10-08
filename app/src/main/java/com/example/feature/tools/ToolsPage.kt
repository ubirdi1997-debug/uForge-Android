package com.example.feature.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
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
fun ToolsPage(
    isOpen: Boolean,
    project: Project,
    onClose: () -> Unit,
    onToggleTool: (String, Boolean) -> Unit,
    onSetAllTools: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val enabledCount = ForgeConstants.TOOL14.count { project.ptools[it.id] != false }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Back, Title, All/None chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tools",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Slate100
                    )
                    Text(
                        text = "$enabledCount of 14 built-in tools enabled",
                        fontFamily = InterFontFamily,
                        fontSize = 11.5.sp,
                        color = Slate400
                    )
                }

                // All shortcut
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .clickable { onSetAllTools(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("All", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate100)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // None shortcut
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .clickable { onSetAllTools(false) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("None", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate100)
                }
            }

            // Tools List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ForgeConstants.TOOL14) { tool ->
                    val isEnabled = project.ptools[tool.id] != false

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .clickable { onToggleTool(tool.id, !isEnabled) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Custom Checkbox
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isEnabled) Emerald400 else Slate800)
                                .border(1.5.dp, if (isEnabled) Emerald400 else Slate700, RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isEnabled) {
                                Text("✓", color = Slate950, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${tool.icon} ${tool.name}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Slate100
                            )
                            Text(
                                text = tool.description,
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                color = Slate400
                            )
                            Text(
                                text = tool.exampleQuery,
                                fontFamily = InterFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.5.sp,
                                color = Emerald400
                            )
                        }
                    }
                }
            }
        }
    }
}
