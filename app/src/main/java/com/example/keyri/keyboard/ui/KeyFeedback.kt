package com.example.keyri.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.ui.theme.KeyboardTheme
import kotlin.math.roundToInt

/** Per-key feedback toggles, provided once near the keyboard root. */
data class KeyFeedbackConfig(
    val haptics: Boolean = true,
    val sound: Boolean = false,
    val preview: Boolean = true
)

val LocalKeyFeedback = staticCompositionLocalOf { KeyFeedbackConfig() }

/**
 * Tracks which character key is currently held so a single bubble can be drawn above
 * it. Lives at the keyboard root (not a separate window) to avoid IME window-token
 * crashes that a [androidx.compose.ui.window.Popup] would risk.
 */
class KeyPreviewController {
    var label by mutableStateOf<String?>(null)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var size by mutableStateOf(IntSize.Zero)
        private set

    private var rootCoords: LayoutCoordinates? = null

    fun attachRoot(coordinates: LayoutCoordinates) {
        rootCoords = coordinates
    }

    fun show(text: String, keyCoordinates: LayoutCoordinates) {
        val root = rootCoords ?: return
        if (!keyCoordinates.isAttached) return
        offset = root.localPositionOf(keyCoordinates, Offset.Zero)
        size = keyCoordinates.size
        label = text
    }

    fun hide() {
        label = null
    }
}

val LocalKeyPreview = staticCompositionLocalOf<KeyPreviewController?> { null }

/** Floating glyph bubble shown above the held key, like Samsung/Gboard previews. */
@Composable
fun KeyPreviewBubble(controller: KeyPreviewController, theme: KeyboardTheme) {
    val label = controller.label ?: return
    val density = LocalDensity.current
    val bubbleHeight = 48.dp
    val gap = 6.dp
    val shape = RoundedCornerShape(10.dp)
    val bubbleHeightPx = with(density) { (bubbleHeight + gap).toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = controller.offset.x.roundToInt(),
                    y = (controller.offset.y - bubbleHeightPx).roundToInt()
                )
            }
            .width(with(density) { controller.size.width.toDp() })
            .height(bubbleHeight)
            .clip(shape)
            .background(theme.keyPressedColor)
            .border(1.dp, theme.accent.copy(alpha = 0.35f), shape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = theme.textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
