package com.example.feature.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ProjectFileEntity
import com.example.domain.model.FileTreeNode
import com.example.ui.theme.Amber400
import com.example.ui.theme.AmberTint
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
import com.example.ui.theme.GlassHighlightOverlay
import com.example.ui.theme.GlossyBorderStroke
import com.example.ui.theme.GlossyCardGradient
import com.example.ui.theme.GlossyEmeraldStroke
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun HierarchicalFileExplorer(
    projectName: String,
    files: List<ProjectFileEntity>,
    selectedFile: String,
    onOpenFile: (String) -> Unit,
    onAddFile: (parentDir: String) -> Unit,
    onAddFolder: (parentDir: String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (oldPath: String, newPath: String) -> Unit,
    onToggleFolder: (path: String, isExpanded: Boolean) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var contextMenuNode by remember { mutableStateOf<FileTreeNode?>(null) }
    var renameTargetNode by remember { mutableStateOf<FileTreeNode?>(null) }
    var newRenameName by remember { mutableStateOf("") }

    // Build hierarchical tree
    val rootNodes = remember(files, searchQuery) {
        val tree = FileTreeNode.buildTree(files)
        if (searchQuery.isBlank()) {
            tree
        } else {
            fun filterTree(nodes: List<FileTreeNode>): List<FileTreeNode> {
                return nodes.mapNotNull { node ->
                    val matches = node.name.contains(searchQuery, ignoreCase = true) || node.path.contains(searchQuery, ignoreCase = true)
                    val filteredChildren = filterTree(node.children)
                    if (matches || filteredChildren.isNotEmpty()) {
                        node.copy(children = filteredChildren, isExpanded = true)
                    } else null
                }
            }
            filterTree(tree)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(GlossyCardGradient)
            .border(GlossyBorderStroke, RoundedCornerShape(16.dp))
    ) {
        // Glossy Header Bar with specular highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate950.copy(alpha = 0.85f))
                .border(0.5.dp, Slate700, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Emerald400)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXPLORER",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.sp,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${files.count { !it.isDirectory }} files",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = Slate500
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Search toggle
                    HeaderIconButton(
                        text = if (isSearchActive) "✕" else "🔍",
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }
                    )
                    // New file button
                    HeaderIconButton(
                        text = "＋📄",
                        onClick = { onAddFile("") }
                    )
                    // New folder button
                    HeaderIconButton(
                        text = "＋📁",
                        onClick = { onAddFolder("") }
                    )
                    // Upload button
                    HeaderIconButton(
                        text = "⬆",
                        onClick = onUploadClick
                    )
                }
            }
        }

        // Search bar (collapsible)
        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text("Search project files...", color = Slate500, fontSize = 12.sp, fontFamily = InterFontFamily)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Slate100, fontSize = 12.sp, fontFamily = JetBrainsMonoFontFamily),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier.fillMaxWidth().testTag("explorer_search_input")
                    )
                }
            }
        }

        // Breadcrumbs indicator bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$projectName / ${selectedFile.replace("/", " / ")}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.5.sp,
                color = Emerald400,
                maxLines = 1
            )
        }

        // Tree Nodes List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (rootNodes.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No files match \"$searchQuery\"" else "No files in project",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            fun renderNode(node: FileTreeNode) {
                item(key = node.path) {
                    FileTreeItemRow(
                        node = node,
                        isSelected = selectedFile == node.path,
                        onSelect = {
                            if (node.isDirectory) {
                                onToggleFolder(node.path, !node.isExpanded)
                            } else {
                                onOpenFile(node.path)
                            }
                        },
                        onToggleExpand = { onToggleFolder(node.path, !node.isExpanded) },
                        onContextMenu = { contextMenuNode = node }
                    )
                }

                if (node.isDirectory && node.isExpanded) {
                    node.children.forEach { child ->
                        renderNode(child)
                    }
                }
            }

            rootNodes.forEach { root ->
                renderNode(root)
            }
        }

        // Rename Dialog inline sheet
        renameTargetNode?.let { node ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .border(1.dp, Emerald400, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Rename ${node.name}", fontFamily = InterFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasicTextField(
                            value = newRenameName,
                            onValueChange = { newRenameName = it },
                            textStyle = TextStyle(color = Slate100, fontSize = 12.sp, fontFamily = JetBrainsMonoFontFamily),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .border(1.dp, Slate700, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald400)
                                .clickable {
                                    if (newRenameName.isNotBlank()) {
                                        val parent = if (node.path.contains('/')) node.path.substringBeforeLast('/') + "/" else ""
                                        onRenameFile(node.path, "$parent$newRenameName")
                                        renameTargetNode = null
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate700)
                                .clickable { renameTargetNode = null }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp, color = Slate100)
                        }
                    }
                }
            }
        }

        // Context Action Bottom Bar for selected node
        contextMenuNode?.let { node ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(0.5.dp, Slate700)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = node.name,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = Slate100,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (node.isDirectory) {
                        ActionChip(text = "+File", onClick = {
                            onAddFile(node.path)
                            contextMenuNode = null
                        })
                        ActionChip(text = "+Dir", onClick = {
                            onAddFolder(node.path)
                            contextMenuNode = null
                        })
                    }
                    ActionChip(text = "Rename", onClick = {
                        renameTargetNode = node
                        newRenameName = node.name
                        contextMenuNode = null
                    })
                    ActionChip(text = "Delete", isDanger = true, onClick = {
                        onDeleteFile(node.path)
                        contextMenuNode = null
                    })
                    ActionChip(text = "✕", onClick = { contextMenuNode = null })
                }
            }
        }
    }
}

@Composable
fun FileTreeItemRow(
    node: FileTreeNode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onContextMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (node.isExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "chevron_rot"
    )

    val itemBackground = if (isSelected) EmeraldTint else Color.Transparent
    val itemBorder = if (isSelected) GlossyEmeraldStroke else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(start = (node.depth * 14).dp)
            .clip(RoundedCornerShape(8.dp))
            .background(itemBackground)
            .then(if (itemBorder != null) Modifier.border(itemBorder, RoundedCornerShape(8.dp)) else Modifier)
            .clickable { onSelect() }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand/Collapse Chevron for directories
        if (node.isDirectory) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onToggleExpand() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 9.sp,
                    color = if (isSelected) Emerald400 else Slate400,
                    modifier = Modifier.rotate(rotation)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }

        // File/Folder Type Icon with distinctive coloring
        val (icon, iconColor) = getFileIconAndColor(node)
        Text(
            text = icon,
            fontSize = 12.sp,
            color = iconColor,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(6.dp))

        // File/Folder Name
        Text(
            text = node.name,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected || node.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Emerald300 else if (node.isDirectory) Slate100 else Slate400,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Dirty modified indicator dot
        if (node.isDirty) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Amber400)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Context menu trigger ⋯
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onContextMenu() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⋯",
                fontSize = 12.sp,
                color = Slate500,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getFileIconAndColor(node: FileTreeNode): Pair<String, Color> {
    if (node.isDirectory) {
        return "📁" to SkyBlue
    }
    val ext = node.extension.lowercase()
    return when {
        ext == "ts" || ext == "tsx" || ext == "js" || ext == "jsx" -> "◇" to Emerald400
        ext == "html" || ext == "htm" -> "◈" to Amber400
        ext == "css" || ext == "scss" -> "✦" to PurpleAccent
        ext == "json" || ext == "yaml" || ext == "yml" || ext == "toml" -> "{}" to Amber400
        ext == "sh" || ext == "bash" -> "\$_" to Emerald300
        ext == "md" || ext == "txt" -> "¶" to SkyBlue
        ext == "sql" -> "⌸" to PurpleAccent
        ext == "py" -> "🐍" to SkyBlue
        ext == "kt" || ext == "kts" -> "🔷" to PurpleAccent
        else -> "▫" to Slate400
    }
}

@Composable
private fun HeaderIconButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 11.sp, color = Slate100)
    }
}

@Composable
private fun ActionChip(
    text: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDanger) CrimsonRed.copy(alpha = 0.2f) else Slate800)
            .border(1.dp, if (isDanger) CrimsonRed else Slate700, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDanger) CrimsonRed else Slate100
        )
    }
}
