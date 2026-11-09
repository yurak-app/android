package com.yurak.agent.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// "Yurak" Clean Pristine White & Crimson Theme
val YurakCrimson = Color(0xFFE11D48)       // Primary Heart Crimson
val YurakCrimsonLight = Color(0xFFFFE4E6)  // Very soft crimson tint for pills
val YurakSlate = Color(0xFF0F172A)         // Deep dark slate for primary text
val YurakMuted = Color(0xFF64748B)         // Slate 500 for secondary text
val YurakBackground = Color(0xFFF8FAFC)    // Soft clean background
val YurakCardBg = Color(0xFFFFFFFF)        // Pure crisp white card background
val YurakBorder = Color(0xFFE2E8F0)        // Subtle card border

// Accent / Category colors
val YurakSky = Color(0xFF0284C7)           // Sky blue
val YurakAmber = Color(0xFFF59E0B)         // Amber
val YurakEmerald = Color(0xFF10B981)       // Emerald
val YurakPurple = Color(0xFF7C3AED)        // Purple

// Always use clean crisp Light Theme to ensure pristine readability & 100% contrast
private val CleanLightColors = lightColorScheme(
    primary = YurakCrimson,
    onPrimary = Color.White,
    primaryContainer = YurakCrimsonLight,
    onPrimaryContainer = Color(0xFF9F1239),
    secondary = YurakSlate,
    onSecondary = Color.White,
    background = YurakBackground,
    surface = YurakCardBg,
    onSurface = YurakSlate,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = YurakMuted,
)

@Composable
fun YurakTheme(content: @Composable () -> Unit) {
    // Pure white & crisp contrast on all devices regardless of system dark mode
    MaterialTheme(colorScheme = CleanLightColors, content = content)
}
