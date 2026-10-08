package com.example.feature.plugins

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ForgeConstants
import com.example.domain.model.Project
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun PluginsPage(
    isOpen: Boolean,
    project: Project,
    onClose: () -> Unit,
    onTogglePlugin: (String) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        var tab by remember { mutableStateOf("disc") } // "disc" or "ins"
        var query by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Back, Title
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
                Text(
                    text = "Plugins",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Slate100
                )
            }

            // Search Bar
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
                if (query.isEmpty()) {
                    Text("Search plugins", color = Slate400, fontSize = 14.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = TextStyle(color = Slate100, fontSize = 14.sp, fontFamily = InterFontFamily),
                    cursorBrush = SolidColor(Emerald400),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tabs: Discover vs Installed (n)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (tab == "disc") Slate950 else Slate800)
                        .border(if (tab == "disc") 1.dp else 0.dp, if (tab == "disc") Emerald400 else Slate800, RoundedCornerShape(9.dp))
                        .clickable { tab = "disc" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Discover", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = if (tab == "disc") Emerald400 else Slate400)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (tab == "ins") Slate950 else Slate800)
                        .border(if (tab == "ins") 1.dp else 0.dp, if (tab == "ins") Emerald400 else Slate800, RoundedCornerShape(9.dp))
                        .clickable { tab = "ins" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Installed (${project.plugins.size})", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = if (tab == "ins") Emerald400 else Slate400)
                }
            }

            // Plugins List
            val filtered = ForgeConstants.PLUGINS.filter { (id, name, desc) ->
                val matchesTab = if (tab == "ins") project.plugins.contains(id) else !project.plugins.contains(id)
                val matchesQuery = query.isEmpty() || name.contains(query, ignoreCase = true) || desc.contains(query, ignoreCase = true)
                matchesTab && matchesQuery
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Text("Nothing here yet.", fontFamily = InterFontFamily, fontSize = 13.sp, color = Slate400, modifier = Modifier.padding(16.dp))
                    }
                }
                items(filtered) { (id, name, desc) ->
                    val isInstalled = project.plugins.contains(id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Slate100)
                            Text(desc, fontFamily = InterFontFamily, fontSize = 11.5.sp, color = Slate400)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isInstalled) Slate800 else EmeraldTint)
                                .border(1.dp, if (isInstalled) Slate700 else Emerald400, RoundedCornerShape(10.dp))
                                .clickable {
                                    onTogglePlugin(id)
                                    onToast(if (isInstalled) "Removed $name" else "Installed $name")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isInstalled) "Remove" else "Install",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (isInstalled) Slate100 else Emerald400
                            )
                        }
                    }
                }
            }

            Text("New plugins are added by admins and show up here.", fontFamily = InterFontFamily, fontSize = 11.5.sp, color = Slate400)
        }
    }
}
