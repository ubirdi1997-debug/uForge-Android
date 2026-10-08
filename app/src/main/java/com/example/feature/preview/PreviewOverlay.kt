package com.example.feature.preview

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
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
fun PreviewOverlay(
    isOpen: Boolean,
    project: Project,
    onClose: () -> Unit,
    onSetMode: (String) -> Unit,
    onSetUrl: (String) -> Unit,
    onToggleShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        var inputUrl by remember(project.preview.url) { mutableStateOf(project.preview.url) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Back ←, URL input, Go button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .clickable { onClose() }
                        .testTag("preview_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                // Address Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val addressText = if (project.preview.mode == "html") {
                        "html · ${if (project.files.containsKey("index.html")) "index.html" else "no index.html"}"
                    } else inputUrl

                    BasicTextField(
                        value = addressText,
                        onValueChange = {
                            if (project.preview.mode == "url") {
                                inputUrl = it
                            }
                        },
                        readOnly = project.preview.mode == "html",
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate100
                        ),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_address_input")
                    )
                }

                // Go Button
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald400)
                        .clickable {
                            if (project.preview.mode == "url") {
                                onSetUrl(inputUrl)
                            }
                        }
                        .padding(horizontal = 16.dp)
                        .testTag("preview_go_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate950
                    )
                }
            }

            // Mode Selector Row: HTML | URL / IP + 👁 Share Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Segmented Selector
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // HTML option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (project.preview.mode == "html") Slate950 else Slate800)
                            .border(
                                if (project.preview.mode == "html") 1.dp else 0.dp,
                                if (project.preview.mode == "html") Emerald400 else Slate800,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { onSetMode("html") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HTML",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = if (project.preview.mode == "html") Emerald400 else Slate400
                        )
                    }

                    // URL / IP option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (project.preview.mode == "url") Slate950 else Slate800)
                            .border(
                                if (project.preview.mode == "url") 1.dp else 0.dp,
                                if (project.preview.mode == "url") Emerald400 else Slate800,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { onSetMode("url") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "URL / IP",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = if (project.preview.mode == "url") Emerald400 else Slate400
                        )
                    }
                }

                // 👁 Share Button
                val isShared = project.preview.isShared
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isShared) EmeraldTint else Slate800)
                        .border(1.dp, if (isShared) Emerald400 else Slate700, RoundedCornerShape(10.dp))
                        .clickable { onToggleShare() }
                        .padding(horizontal = 14.dp)
                        .testTag("preview_share_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👁 Share",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = if (isShared) Emerald400 else Slate100
                    )
                }
            }

            // Sandboxed Preview Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
            ) {
                val htmlContent = project.files["index.html"] ?: "<body style='background:#0F172A;color:#F1F5F9;font-family:system-ui;padding:24px'><h3>No index.html</h3><p>Create index.html to preview.</p></body>"

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                        }
                    },
                    update = { webView ->
                        if (project.preview.mode == "html") {
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        } else {
                            if (project.preview.url.startsWith("http")) {
                                webView.loadUrl(project.preview.url)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Note Text
            Text(
                text = if (project.preview.mode == "html") {
                    "Rendered from your current index.html. The agent can read it."
                } else {
                    "Some servers block embedding. The agent gets the address and status only."
                },
                fontFamily = InterFontFamily,
                fontSize = 11.5.sp,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
