package com.example.feature.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.GlossySurfaceGradient
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PurpleAccent
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
fun FileExplorerSidebar(
    isOpen: Boolean,
    projectName: String,
    files: List<ProjectFileEntity>,
    selectedFile: String,
    onClose: () -> Unit,
    onSelectFile: (String) -> Unit,
    onAddFile: (parentDir: String) -> Unit,
    onAddFolder: (parentDir: String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (oldPath: String, newPath: String) -> Unit,
    onToggleFolder: (path: String, isExpanded: Boolean) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("All") }
    var expandedFolders by remember { mutableStateOf<Set<String>>(emptySet()) }
    var renameTargetNode by remember { mutableStateOf<FileTreeNode?>(null) }
    var newRenameName by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onClose() }
                .testTag("file_explorer_sidebar_overlay")
        ) {
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(enabled = false) {} // Consume clicks inside sidebar
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .background(GlossySurfaceGradient)
                        .border(
                            GlossyBorderStroke,
                            RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                        )
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .testTag("file_explorer_sidebar_panel")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- Sidebar Header ---
                        SidebarHeader(
                            projectName = projectName,
                            totalFiles = files.count { !it.isDirectory },
                            onClose = onClose,
                            onAddFile = { onAddFile("") },
                            onAddFolder = { onAddFolder("") },
                            onUploadClick = onUploadClick
                        )

                        // --- Search & Filter Bar ---
                        SearchAndFilterSection(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategory = selectedFilterCategory,
                            onCategorySelected = { selectedFilterCategory = it }
                        )

                        // --- Recent Files Quick-Access Bar ---
                        val directFiles = files.filter { !it.isDirectory }
                        if (directFiles.isNotEmpty() && searchQuery.isBlank()) {
                            RecentFilesBar(
                                files = directFiles.take(5),
                                selectedFile = selectedFile,
                                onSelect = {
                                    onSelectFile(it)
                                    onClose()
                                }
                            )
                        }

                        // --- Hierarchical Tree Nodes ---
                        val rawTree = remember(files) {
                            FileTreeNode.buildTree(files)
                        }

                        // Filter by query and category
                        val filteredTree = remember(rawTree, searchQuery, selectedFilterCategory) {
                            fun matchesCategory(ext: String): Boolean {
                                val e = ext.lowercase()
                                return when (selectedFilterCategory) {
                                    "Source" -> e in listOf("ts", "tsx", "js", "jsx", "kt", "kts", "py", "rs", "go")
                                    "Markup" -> e in listOf("html", "htm", "css", "scss", "svg")
                                    "Config" -> e in listOf("json", "toml", "yaml", "yml", "env", "xml")
                                    "Docs" -> e in listOf("md", "txt", "doc")
                                    else -> true
                                }
                            }

                            fun filterNode(node: FileTreeNode): FileTreeNode? {
                                if (node.isDirectory) {
                                    val filteredChildren = node.children.mapNotNull { filterNode(it) }
                                    return if (filteredChildren.isNotEmpty() || (searchQuery.isNotBlank() && node.name.contains(searchQuery, ignoreCase = true))) {
                                        node.copy(children = filteredChildren, isExpanded = true)
                                    } else null
                                } else {
                                    val matchesQuery = searchQuery.isBlank() || node.name.contains(searchQuery, ignoreCase = true) || node.path.contains(searchQuery, ignoreCase = true)
                                    val matchesCat = matchesCategory(node.extension)
                                    return if (matchesQuery && matchesCat) node else null
                                }
                            }

                            if (searchQuery.isBlank() && selectedFilterCategory == "All") {
                                rawTree
                            } else {
                                rawTree.mapNotNull { filterNode(it) }
                            }
                        }

                        val visibleNodes = remember(filteredTree, expandedFolders) {
                            FileTreeNode.flattenVisibleNodes(filteredTree, expandedFolders)
                        }

                        if (visibleNodes.isEmpty()) {
                            EmptySearchResult(
                                query = searchQuery,
                                onClear = {
                                    searchQuery = ""
                                    selectedFilterCategory = "All"
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(visibleNodes, key = { it.path }) { node ->
                                    FileSidebarNodeItem(
                                        node = node,
                                        isSelected = node.path == selectedFile,
                                        onSelect = {
                                            if (node.isDirectory) {
                                                val nextExpanded = !node.isExpanded
                                                onToggleFolder(node.path, nextExpanded)
                                                expandedFolders = if (nextExpanded) {
                                                    expandedFolders + node.path
                                                } else {
                                                    expandedFolders - node.path
                                                }
                                            } else {
                                                onSelectFile(node.path)
                                                onClose()
                                            }
                                        },
                                        onAddFileInDir = { onAddFile(node.path) },
                                        onAddFolderInDir = { onAddFolder(node.path) },
                                        onRename = {
                                            renameTargetNode = node
                                            newRenameName = node.name
                                        },
                                        onDelete = { onDeleteFile(node.path) }
                                    )
                                }
                            }
                        }

                        // --- Active File Status Footer ---
                        SidebarFooter(
                            selectedFile = selectedFile,
                            totalFiles = files.count { !it.isDirectory },
                            onQuickNewFile = { onAddFile("") }
                        )
                    }

                    // --- Inline Rename Dialog Overlay ---
                    if (renameTargetNode != null) {
                        RenameDialog(
                            targetNode = renameTargetNode!!,
                            newName = newRenameName,
                            onNameChange = { newRenameName = it },
                            onConfirm = {
                                val node = renameTargetNode!!
                                val parent = if (node.path.contains('/')) node.path.substringBeforeLast('/') else ""
                                val newPath = if (parent.isNotEmpty()) "$parent/$newRenameName" else newRenameName
                                onRenameFile(node.path, newPath)
                                renameTargetNode = null
                            },
                            onDismiss = { renameTargetNode = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader(
    projectName: String,
    totalFiles: Int,
    onClose: () -> Unit,
    onAddFile: () -> Unit,
    onAddFolder: () -> Unit,
    onUploadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate950)
            .border(0.5.dp, Slate700, RoundedCornerShape(topEnd = 20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldTint)
                .border(0.5.dp, Emerald400, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📁", fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PROJECT FILES",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.05.sp,
                color = Slate100
            )
            Text(
                text = "$projectName · $totalFiles files",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = Emerald400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action: New file
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate800)
                .border(0.5.dp, Slate700, RoundedCornerShape(6.dp))
                .clickable { onAddFile() },
            contentAlignment = Alignment.Center
        ) {
            Text("+📄", fontSize = 11.sp, color = Slate100)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action: New folder
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate800)
                .border(0.5.dp, Slate700, RoundedCornerShape(6.dp))
                .clickable { onAddFolder() },
            contentAlignment = Alignment.Center
        ) {
            Text("+📁", fontSize = 11.sp, color = Slate100)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Close sidebar button
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate800)
                .border(0.5.dp, Slate700, RoundedCornerShape(6.dp))
                .clickable { onClose() }
                .testTag("close_file_explorer_sidebar"),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchAndFilterSection(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Search Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Slate900)
                .border(0.5.dp, if (query.isNotEmpty()) Emerald400 else Slate700, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔍", fontSize = 11.sp, color = Slate500)
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("file_sidebar_search_input"),
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = Slate100
                    ),
                    cursorBrush = SolidColor(Emerald400),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Filter files by name...",
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                color = Slate500
                            )
                        }
                        innerTextField()
                    }
                )
                if (query.isNotEmpty()) {
                    Text(
                        text = "✕",
                        fontSize = 11.sp,
                        color = Slate400,
                        modifier = Modifier
                            .clickable { onQueryChange("") }
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category filter chips
        val categories = listOf("All", "Source", "Markup", "Config", "Docs")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Emerald400 else Slate800)
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cat,
                        fontFamily = InterFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Slate950 else Slate400
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentFilesBar(
    files: List<ProjectFileEntity>,
    selectedFile: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "RECENT",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            letterSpacing = 0.08.sp,
            color = Slate500
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            files.forEach { file ->
                val isSelected = file.path == selectedFile
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) EmeraldTint else Slate800)
                        .border(
                            0.5.dp,
                            if (isSelected) Emerald400 else Slate700,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(file.path) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (icon, color) = getSidebarFileIcon(file.name, file.extension)
                        Text(icon, fontSize = 10.sp, color = color)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = file.name,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Emerald300 else Slate100
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileSidebarNodeItem(
    node: FileTreeNode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onAddFileInDir: () -> Unit,
    onAddFolderInDir: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val indentPadding = (node.depth * 14).dp
    val chevronRotation by animateFloatAsState(
        targetValue = if (node.isExpanded) 90f else 0f,
        label = "chevron_rot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EmeraldTint else if (node.isDirectory) Slate900.copy(alpha = 0.35f) else Color.Transparent
            )
            .border(
                if (isSelected) GlossyEmeraldStroke else BorderStroke(0.5.dp, Color.Transparent),
                RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(start = indentPadding + 6.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
            .testTag("file_node_${node.path.replace('/', '_')}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                // Folder Expand / Collapse indicator
                Text(
                    text = "▸",
                    fontSize = 11.sp,
                    color = Slate400,
                    modifier = Modifier.rotate(chevronRotation)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (node.isExpanded) "📂" else "📁",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = node.name,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate100,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Child count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate800)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${node.children.size}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Slate500
                    )
                }
            } else {
                // File row
                Spacer(modifier = Modifier.width(14.dp))
                val (icon, color) = getSidebarFileIcon(node.name, node.extension)
                Text(
                    text = icon,
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = node.name,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Emerald300 else Slate100,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (node.isDirty) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Amber400)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Emerald400)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "OPEN",
                            fontFamily = InterFontFamily,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate950
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            // Context menu button (three dots)
            Box {
                Text(
                    text = "⋮",
                    fontSize = 13.sp,
                    color = Slate500,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { showMenu = true }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Slate900).border(0.5.dp, Slate700)
                ) {
                    if (node.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("+ New File", fontSize = 12.sp, color = Slate100) },
                            onClick = {
                                showMenu = false
                                onAddFileInDir()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+ New Folder", fontSize = 12.sp, color = Slate100) },
                            onClick = {
                                showMenu = false
                                onAddFolderInDir()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Rename", fontSize = 12.sp, color = Slate100) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", fontSize = 12.sp, color = CrimsonRed) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarFooter(
    selectedFile: String,
    totalFiles: Int,
    onQuickNewFile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate950)
            .border(0.5.dp, Slate700, RoundedCornerShape(bottomEnd = 20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ACTIVE FILE",
                    fontFamily = InterFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
                Text(
                    text = selectedFile,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.5.sp,
                    color = Emerald400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Emerald400)
                    .clickable { onQuickNewFile() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ New File",
                    fontFamily = InterFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate950
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResult(query: String, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No files match \"$query\"",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = Slate400
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Slate800)
                .clickable { onClear() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Clear filter",
                fontFamily = InterFontFamily,
                fontSize = 11.sp,
                color = Emerald400
            )
        }
    }
}

@Composable
private fun RenameDialog(
    targetNode: FileTreeNode,
    newName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Slate900)
                .border(GlossyBorderStroke, RoundedCornerShape(14.dp))
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Rename ${if (targetNode.isDirectory) "Folder" else "File"}",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate100
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .border(1.dp, Emerald400, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = newName,
                        onValueChange = onNameChange,
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = Slate100
                        ),
                        cursorBrush = SolidColor(Emerald400),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .clickable { onDismiss() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Cancel", fontSize = 11.5.sp, color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Emerald400)
                            .clickable { onConfirm() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Save", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate950)
                    }
                }
            }
        }
    }
}

private fun getSidebarFileIcon(name: String, ext: String): Pair<String, Color> {
    val lower = ext.lowercase()
    val lowerName = name.lowercase()
    if (lowerName == ".env" || lowerName == ".env.example") return "🔑" to Amber400
    if (lowerName == ".gitignore" || lowerName == "dockerfile") return "⚙" to Slate400

    return when {
        lower == "ts" || lower == "tsx" || lower == "js" || lower == "jsx" -> "◇" to Emerald400
        lower == "html" || lower == "htm" -> "◈" to Amber400
        lower == "css" || lower == "scss" -> "✦" to PurpleAccent
        lower == "json" || lower == "yaml" || lower == "yml" || lower == "toml" -> "{}" to Amber400
        lower == "sh" || lower == "bash" -> "\$_" to Emerald300
        lower == "md" || lower == "txt" -> "¶" to SkyBlue
        lower == "sql" -> "⌸" to PurpleAccent
        lower == "py" -> "🐍" to SkyBlue
        lower == "kt" || lower == "kts" -> "🔷" to PurpleAccent
        else -> "▫" to Slate400
    }
}
