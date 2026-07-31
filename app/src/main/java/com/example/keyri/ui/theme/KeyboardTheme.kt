package com.example.keyri.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Visual style of the keyboard itself, independent of the app's Material theme
 * so the IME can render without a MaterialTheme host.
 */
data class KeyboardTheme(
    val id: String,
    val displayName: String,
    val background: Color,
    val backgroundGlow: Color,
    val keyColor: Color,
    val keyPressedColor: Color,
    val specialKeyColor: Color,
    val accent: Color,
    val textColor: Color,
    val mutedTextColor: Color,
    val suggestionChipColor: Color,
    val isLight: Boolean = false
)

object KeyboardThemes {

    val Graphite = KeyboardTheme(
        id = "graphite",
        displayName = "Graphite",
        background = Color(0xFF101316),
        backgroundGlow = Color(0xFF8A9199),
        keyColor = Color(0xFF262B31),
        keyPressedColor = Color(0xFF3A4149),
        specialKeyColor = Color(0xFF1B1F24),
        accent = Color(0xFFC9D1D9),
        textColor = Color(0xFFF2F4F6),
        mutedTextColor = Color(0xFF8A9199),
        suggestionChipColor = Color(0xFF1C2127)
    )

    val AmoledDark = KeyboardTheme(
        id = "amoled_dark",
        displayName = "AMOLED Dark",
        background = Color(0xFF000000),
        backgroundGlow = Color(0xFF6F7479),
        keyColor = Color(0xFF141414),
        keyPressedColor = Color(0xFF2C2C2C),
        specialKeyColor = Color(0xFF0B0B0B),
        accent = Color(0xFF9AA0A6),
        textColor = Color(0xFFEDEDED),
        mutedTextColor = Color(0xFF707070),
        suggestionChipColor = Color(0xFF171717)
    )

    val CleanLight = KeyboardTheme(
        id = "clean_light",
        displayName = "Clean Light",
        background = Color(0xFFECEEF1),
        backgroundGlow = Color(0xFF94A3B8),
        keyColor = Color(0xFFFFFFFF),
        keyPressedColor = Color(0xFFD8DDE3),
        specialKeyColor = Color(0xFFDCE0E6),
        accent = Color(0xFF475569),
        textColor = Color(0xFF1F2933),
        mutedTextColor = Color(0xFF6B7280),
        suggestionChipColor = Color(0xFFDFE3E9),
        isLight = true
    )

    val all = listOf(Graphite, AmoledDark, CleanLight)

    fun byId(id: String?): KeyboardTheme = all.firstOrNull { it.id == id } ?: Graphite
}
