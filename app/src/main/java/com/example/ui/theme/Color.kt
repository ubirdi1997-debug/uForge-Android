package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Slate palette from Forge Studio UX blueprint
val Slate950 = Color(0xFF020617) // Primary background
val Slate900 = Color(0xFF0F172A) // Card & panel background
val Slate800 = Color(0xFF1E293B) // Component surfaces, chips, bubbles
val Slate700 = Color(0xFF334155) // Borders, lines, inactive tracks
val Slate600 = Color(0xFF475569) // Muted highlights
val Slate500 = Color(0xFF64748B) // Gutter numbers, dim metadata
val Slate400 = Color(0xFF94A3B8) // Secondary text, subtitles
val Slate200 = Color(0xFFE2E8F0) // Bright text
val Slate100 = Color(0xFFF1F5F9) // Primary text

// Accents
val Emerald400 = Color(0xFF4ADE80) // Primary accent, user bubble, active indicators
val Emerald300 = Color(0xFF86EFAC) // Bright glowing emerald highlight
val EmeraldTint = Color(0x244ADE80) // Emerald highlight tint
val Amber400 = Color(0xFFFBBF24) // Warning / waiting status, strings
val AmberTint = Color(0x28FBBF24) // Amber status tint
val SkyBlue = Color(0xFF7DD3FC) // Functions / tokens / cyan
val PurpleAccent = Color(0xFFC084FC) // Types / styling accent
val CrimsonRed = Color(0xFFF87171) // Errors / failed states
val PureWhite = Color(0xFFFFFFFF)

// Glossy Glassmorphism Brushes & Strokes
val GlossySurfaceGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF1A263D).copy(alpha = 0.95f),
        Slate900.copy(alpha = 0.98f),
        Slate950
    )
)

val GlossyCardGradient = Brush.verticalGradient(
    listOf(
        Slate800.copy(alpha = 0.96f),
        Slate900.copy(alpha = 0.98f)
    )
)

val GlossyHeaderBrush = Brush.horizontalGradient(
    listOf(
        Slate900,
        Color(0xFF162238),
        Slate900
    )
)

val GlossyBorderStroke = BorderStroke(
    1.dp,
    Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.22f),
            Slate700,
            Color.White.copy(alpha = 0.05f)
        )
    )
)

val GlossyEmeraldStroke = BorderStroke(
    1.2.dp,
    Brush.verticalGradient(
        listOf(
            Emerald300,
            Emerald400,
            Emerald400.copy(alpha = 0.35f)
        )
    )
)

val GlossyEmeraldBrush = Brush.linearGradient(
    listOf(
        Emerald300,
        Emerald400,
        Color(0xFF22C55E)
    )
)

val GlassHighlightOverlay = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.14f),
        Color.White.copy(alpha = 0.03f),
        Color.Transparent
    )
)
