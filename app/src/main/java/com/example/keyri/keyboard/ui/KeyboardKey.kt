package com.example.keyri.keyboard.ui

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.ui.theme.KeyboardTheme
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun KeyboardKey(
    label: String,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isAccent: Boolean = false,
    fontSize: TextUnit = 22.sp,
    labelColor: Color? = null,
    icon: KeyIconType? = null,
    iconFilled: Boolean = false,
    capsBar: Boolean = false,
    repeatable: Boolean = false,
    previewable: Boolean = false,
    onCursorMove: ((Int) -> Unit)? = null,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnCursorMove by rememberUpdatedState(onCursorMove)
    val feedback = LocalKeyFeedback.current
    val preview = LocalKeyPreview.current
    val audioManager = remember(view) {
        view.context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val showsPreview = previewable && icon == null && label.isNotEmpty()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 40),
        label = "keyScale"
    )
    // Highlight snaps on instantly with the press and fades out after release,
    // so each typed key visibly flashes.
    val highlight by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (pressed) snap() else tween(durationMillis = 240),
        label = "keyHighlight"
    )
    val restColor = when {
        isAccent -> theme.accent
        isSpecial -> theme.specialKeyColor
        else -> theme.keyColor
    }
    val pressColor = theme.accent.copy(alpha = 0.30f).compositeOver(theme.keyPressedColor)
    val baseColor = lerp(restColor, pressColor, highlight)

    // Liquid glass: vertical sheen over the key color plus a light-catching top edge.
    // The edge glows in the accent color while the key is highlighted.
    val sheen = Color.White.copy(alpha = if (theme.isLight) 0.55f else 0.08f)
    val glassFill = Brush.verticalGradient(
        listOf(sheen.compositeOver(baseColor), baseColor)
    )
    val edgeTop = lerp(
        Color.White.copy(alpha = if (theme.isLight) 0.9f else if (isAccent) 0.45f else 0.16f),
        theme.accent,
        highlight * 0.85f
    )
    val edgeBottom = lerp(
        Color.White.copy(alpha = 0f),
        theme.accent.copy(alpha = 0.55f),
        highlight
    )
    val edgeHighlight = Brush.verticalGradient(listOf(edgeTop, edgeBottom))
    val shape = RoundedCornerShape(9.dp)

    Box(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(glassFill)
            .border(1.dp, edgeHighlight, shape)
            .onGloballyPositioned { coordinates = it }
            .pointerInput(repeatable, onCursorMove != null) {
                if (onCursorMove != null) {
                    // Spacebar mode: hold and slide horizontally to move the cursor;
                    // a plain release without sliding commits the normal key action.
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        pressed = true
                        if (feedback.haptics) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        if (feedback.sound) {
                            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                        }
                        var cursorMode = false
                        var accumulated = 0f
                        var lastX = down.position.x
                        val enterThreshold = 16.dp.toPx()
                        val stepPx = 13.dp.toPx()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                change.consume()
                                break
                            }
                            val x = change.position.x
                            if (!cursorMode && abs(x - down.position.x) > enterThreshold) {
                                cursorMode = true
                                lastX = x
                                if (feedback.haptics) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                            }
                            if (cursorMode) {
                                accumulated += x - lastX
                                lastX = x
                                while (accumulated >= stepPx) {
                                    currentOnCursorMove?.invoke(1)
                                    accumulated -= stepPx
                                }
                                while (accumulated <= -stepPx) {
                                    currentOnCursorMove?.invoke(-1)
                                    accumulated += stepPx
                                }
                                change.consume()
                            }
                        }
                        if (!cursorMode) currentOnClick()
                        pressed = false
                    }
                } else {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            if (feedback.haptics) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            if (feedback.sound) {
                                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                            }
                            if (showsPreview && feedback.preview) {
                                coordinates?.let { preview?.show(label, it) }
                            }
                            currentOnClick()
                            var repeatJob: Job? = null
                            if (repeatable) {
                                repeatJob = scope.launch {
                                    delay(420)
                                    while (isActive) {
                                        currentOnClick()
                                        if (feedback.haptics) {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        }
                                        delay(50)
                                    }
                                }
                            }
                            tryAwaitRelease()
                            repeatJob?.cancel()
                            if (showsPreview) preview?.hide()
                            pressed = false
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val contentColor = labelColor ?: if (isAccent) theme.background else theme.textColor
        if (icon != null) {
            KeyGlyph(icon, contentColor, filled = iconFilled, capsBar = capsBar)
        } else {
            Text(
                text = label,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
