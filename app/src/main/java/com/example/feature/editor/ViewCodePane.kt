package com.example.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ProjectFileEntity
import com.example.data.terminal.TerminalEngine
import com.example.domain.model.Project
import com.example.feature.editor.components.HierarchicalFileExplorer
import com.example.feature.terminal.TerminalEmulatorComponent
import com.example.ui.theme.Amber400
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.EmeraldTint
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
fun ViewCodePane(
    project: Project,
    roomFiles: List<ProjectFileEntity>,
    terminalLogs: List<TerminalEngine.OutputLine>,
    terminalInput: String,
    terminalSuggestions: List<Pair<String, String>>,
    onOpenFile: (String) -> Unit,
    onFileContentChange: (filename: String, content: String) -> Unit,
    onAddFolder: (parentDir: String) -> Unit,
    onAddFile: (parentDir: String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (oldPath: String, newPath: String) -> Unit,
    onToggleFolderExpanded: (path: String, isExpanded: Boolean) -> Unit,
    onUploadFile: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onExecuteTerminal: (String) -> Unit,
    onOpenFileExplorerSidebar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var isExplorerExpanded by remember { mutableStateOf(true) }
    var isInlineTerminalOpen by remember { mutableStateOf(false) }

    val currentContent = project.files[project.openFile] ?: ""
    val undoStack = remember(project.openFile) { mutableStateListOf<String>() }
    val redoStack = remember(project.openFile) { mutableStateListOf<String>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Hierarchical File Explorer Section (backed by Room SQLite)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isExplorerExpanded && !isInlineTerminalOpen) {
                        Modifier.height(180.dp)
                    } else if (isExplorerExpanded && isInlineTerminalOpen) {
                        Modifier.height(130.dp)
                    } else {
                        Modifier.height(38.dp)
                    }
                )
        ) {
            if (isExplorerExpanded) {
                HierarchicalFileExplorer(
                    projectName = project.name,
                    files = if (roomFiles.isNotEmpty()) roomFiles else project.files.map { (p, c) ->
                        ProjectFileEntity(
                            id = "${project.id}:$p",
                            projectId = project.id,
                            path = p,
                            name = p.substringAfterLast('/'),
                            parentPath = if (p.contains('/')) p.substringBeforeLast('/') else "",
                            isDirectory = false,
                            content = c,
                            extension = p.substringAfterLast('.', "")
                        )
                    },
                    selectedFile = project.openFile,
                    onOpenFile = onOpenFile,
                    onAddFile = { onAddFile(it) },
                    onAddFolder = { onAddFolder(it) },
                    onDeleteFile = onDeleteFile,
                    onRenameFile = onRenameFile,
                    onToggleFolder = onToggleFolderExpanded,
                    onUploadClick = onUploadFile,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Collapsed Explorer bar with expand toggle
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(GlossyBorderStroke, RoundedCornerShape(12.dp))
                        .clickable { isExplorerExpanded = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📁", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXPLORER · ${project.files.size} files",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = Slate100
                        )
                    }
                    Text("▼ Expand", fontFamily = InterFontFamily, fontSize = 11.sp, color = Emerald400)
                }
            }
        }

        // Main Code Editor or Terminal View
        if (!isInlineTerminalOpen) {
            // Code Editor Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlossyCardGradient)
                    .border(GlossyBorderStroke, RoundedCornerShape(16.dp))
                    .testTag("code_editor_card")
            ) {
                // File Tab & Quick Actions Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950.copy(alpha = 0.95f))
                        .border(0.5.dp, Slate700, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File type dot & active file name
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Emerald400)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = project.openFile,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = Slate100,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Dedicated Sidebar Drawer Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldTint)
                                .border(0.5.dp, Emerald400, RoundedCornerShape(6.dp))
                                .clickable { onOpenFileExplorerSidebar() }
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                .testTag("open_file_sidebar_editor_button")
                        ) {
                            Text(
                                text = "📁 Sidebar",
                                fontSize = 10.sp,
                                color = Emerald300,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = InterFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Toggle Explorer button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .clickable { isExplorerExpanded = !isExplorerExpanded }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isExplorerExpanded) "▲ Hide Tree" else "▼ Show Tree",
                                fontSize = 10.sp,
                                color = Slate400,
                                fontFamily = InterFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Undo
                        Text(
                            text = "↶",
                            color = if (undoStack.isNotEmpty()) Slate100 else Slate500,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clickable(enabled = undoStack.isNotEmpty()) {
                                    val previous = undoStack.removeLastOrNull()
                                    if (previous != null) {
                                        redoStack.add(currentContent)
                                        onFileContentChange(project.openFile, previous)
                                    }
                                }
                                .padding(horizontal = 4.dp)
                        )

                        // Redo
                        Text(
                            text = "↷",
                            color = if (redoStack.isNotEmpty()) Slate100 else Slate500,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clickable(enabled = redoStack.isNotEmpty()) {
                                    val next = redoStack.removeLastOrNull()
                                    if (next != null) {
                                        undoStack.add(currentContent)
                                        onFileContentChange(project.openFile, next)
                                    }
                                }
                                .padding(horizontal = 4.dp)
                        )

                        // Copy
                        Text(
                            text = "📋",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(currentContent))
                                }
                                .padding(horizontal = 4.dp)
                        )

                        // Search Toggle
                        Text(
                            text = "🔍",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { isSearchOpen = !isSearchOpen }
                                .padding(horizontal = 4.dp)
                        )

                        // Open Embedded Terminal button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldTint)
                                .border(1.dp, Emerald400, RoundedCornerShape(6.dp))
                                .clickable { isInlineTerminalOpen = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⌨ Shell",
                                fontSize = 10.5.sp,
                                color = Emerald400,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }

                // Search & Replace Bar (when active)
                if (isSearchOpen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Slate100, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                        BasicTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            textStyle = TextStyle(color = Slate100, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald400)
                                .clickable {
                                    if (searchQuery.isNotEmpty()) {
                                        val replaced = currentContent.replace(searchQuery, replaceQuery)
                                        undoStack.add(currentContent)
                                        onFileContentChange(project.openFile, replaced)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("Replace", fontSize = 11.sp, color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Code Editor Body: Line numbers gutter + text editor with syntax highlighting
                val lines = currentContent.lines()
                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line Number Gutter with subtle background
                    Column(
                        modifier = Modifier
                            .width(36.dp)
                            .background(Slate950.copy(alpha = 0.6f))
                            .padding(top = 8.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { index ->
                            Text(
                                text = "${index + 1}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 20.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Editable Code Canvas with syntax highlighting representation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScroll)
                            .padding(top = 8.dp, end = 12.dp, bottom = 8.dp)
                    ) {
                        BasicTextField(
                            value = currentContent,
                            onValueChange = { next ->
                                if (undoStack.lastOrNull() != currentContent) {
                                    undoStack.add(currentContent)
                                }
                                onFileContentChange(project.openFile, next)
                            },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 20.sp,
                                color = Slate100
                            ),
                            cursorBrush = SolidColor(Emerald400),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("code_editor_text_field"),
                            visualTransformation = { text ->
                                val annotated = buildSyntaxHighlightedString(text.text)
                                androidx.compose.ui.text.input.TransformedText(
                                    annotated,
                                    androidx.compose.ui.text.input.OffsetMapping.Identity
                                )
                            }
                        )
                    }
                }
            }
        } else {
            // Inline Terminal Emulator View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TerminalEmulatorComponent(
                    project = project,
                    logs = terminalLogs,
                    input = terminalInput,
                    suggestions = terminalSuggestions,
                    onInputChange = onTerminalInputChange,
                    onExecute = onExecuteTerminal,
                    onClose = { isInlineTerminalOpen = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun buildSyntaxHighlightedString(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    val keywordRegex = Regex("\\b(import|export|async|function|await|return|const|let|from|if|else|class|interface|type)\\b")
    val stringRegex = Regex("(\"[^\"]*\"|'[^']*')")
    val commentRegex = Regex("(//.*|#.*)")
    val numberRegex = Regex("\\b\\d+\\b")

    keywordRegex.findAll(text).forEach { match ->
        builder.addStyle(
            SpanStyle(color = Emerald400, fontWeight = FontWeight.SemiBold),
            match.range.first,
            match.range.last + 1
        )
    }

    stringRegex.findAll(text).forEach { match ->
        builder.addStyle(
            SpanStyle(color = Amber400),
            match.range.first,
            match.range.last + 1
        )
    }

    commentRegex.findAll(text).forEach { match ->
        builder.addStyle(
            SpanStyle(color = Slate500),
            match.range.first,
            match.range.last + 1
        )
    }

    numberRegex.findAll(text).forEach { match ->
        builder.addStyle(
            SpanStyle(color = SkyBlue),
            match.range.first,
            match.range.last + 1
        )
    }

    return builder.toAnnotatedString()
}
