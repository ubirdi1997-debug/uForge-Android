package com.example.feature.workspace.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.workspace.WorkspaceMode
import com.example.ui.theme.Emerald400
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate950

@Composable
fun TopWorkspaceBar(
    projectName: String,
    isGitHubSynced: Boolean,
    mode: WorkspaceMode,
    onMenuClick: () -> Unit,
    onFilesClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Menu Button (44x44, rounded 12dp, slate 800)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .clickable { onMenuClick() }
                .testTag("menu_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☰",
                color = Slate100,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Dedicated Files Explorer Sidebar Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800)
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .clickable { onFilesClick() }
                .testTag("files_sidebar_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📁",
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Project Title & Branch Status
        Column(
            modifier = Modifier
                .weight(1f)
                .testTag("project_header")
        ) {
            Text(
                text = projectName,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Slate100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "⎇ main · ${if (isGitHubSynced) "synced" else "local"}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.5.sp,
                color = Emerald400,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Mode action button: ◉ Preview in Chat mode, ⌨ Terminal in Code mode
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Emerald400)
                .clickable { onActionClick() }
                .padding(horizontal = 16.dp)
                .testTag("top_action_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mode == WorkspaceMode.Chat) "◉ Preview" else "⌨ Terminal",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Slate950
            )
        }
    }
}
