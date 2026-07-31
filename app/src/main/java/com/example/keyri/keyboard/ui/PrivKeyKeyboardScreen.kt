package com.example.keyri.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.example.keyri.keyboard.KeyAction
import com.example.keyri.keyboard.KeyboardLayouts
import com.example.keyri.keyboard.LayoutMode
import com.example.keyri.keyboard.ShiftState
import com.example.keyri.settings.KeyboardSettings
import com.example.keyri.ui.theme.KeyboardTheme

@Composable
fun PrivKeyKeyboardScreen(
    theme: KeyboardTheme,
    shiftState: ShiftState,
    layoutMode: LayoutMode,
    suggestions: List<String>,
    showAiPanel: Boolean,
    showEmojiPanel: Boolean,
    settings: KeyboardSettings,
    recentEmojis: List<String>,
    onAction: (KeyAction) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    val backgroundTop = theme.backgroundGlow.copy(alpha = 0.08f).compositeOver(theme.background)
    val feedback = remember(settings) {
        KeyFeedbackConfig(
            haptics = settings.hapticsOnKey,
            sound = settings.soundOnKey,
            preview = settings.keyPreview
        )
    }
    val previewController = remember { KeyPreviewController() }

    // Lift the keyboard clear of the navigation area. Compose insets can report 0
    // inside an IME window, so fall back to the view's root insets and finally to
    // the system navigation_bar_height resource (covers 3-button navigation).
    val view = LocalView.current
    val density = LocalDensity.current
    val composeNav = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val systemNav = remember(view) {
        val fromInsets = view.rootWindowInsets?.let {
            WindowInsetsCompat.toWindowInsetsCompat(it)
                .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        } ?: 0
        val resId = view.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val fromResource = if (resId > 0) view.resources.getDimensionPixelSize(resId) else 0
        with(density) { maxOf(fromInsets, fromResource).toDp() }
    }
    // Extra margin past the nav inset keeps the bottom key row clear of the
    // back button even when the reported inset is smaller than the touch target.
    val bottomPadding = maxOf(composeNav, systemNav, 16.dp) + 14.dp

    CompositionLocalProvider(
        LocalKeyFeedback provides feedback,
        LocalKeyPreview provides previewController
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { previewController.attachRoot(it) }
        ) {
            KeyboardBody(
                theme = theme,
                shiftState = shiftState,
                layoutMode = layoutMode,
                suggestions = suggestions,
                showAiPanel = showAiPanel,
                showEmojiPanel = showEmojiPanel,
                settings = settings,
                recentEmojis = recentEmojis,
                backgroundTop = backgroundTop,
                bottomPadding = bottomPadding,
                onAction = onAction,
                onSuggestionClick = onSuggestionClick
            )
            KeyPreviewBubble(previewController, theme)
        }
    }
}

@Composable
private fun KeyboardBody(
    theme: KeyboardTheme,
    shiftState: ShiftState,
    layoutMode: LayoutMode,
    suggestions: List<String>,
    showAiPanel: Boolean,
    showEmojiPanel: Boolean,
    settings: KeyboardSettings,
    recentEmojis: List<String>,
    backgroundTop: Color,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onAction: (KeyAction) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Liquid glass backdrop: base gradient plus two soft light blooms
                // that the translucent keys appear to float over.
                drawRect(Brush.verticalGradient(listOf(backgroundTop, theme.background)))
                val leftCenter = Offset(size.width * 0.18f, -size.height * 0.1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.backgroundGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = leftCenter,
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = leftCenter
                )
                val rightCenter = Offset(size.width * 0.85f, size.height * 0.95f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.accent.copy(alpha = 0.09f), Color.Transparent),
                        center = rightCenter,
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = rightCenter
                )
            }
            .padding(top = 8.dp, bottom = bottomPadding)
    ) {
        SuggestionBar(
            suggestions = suggestions,
            theme = theme,
            aiPanelActive = showAiPanel,
            onSuggestionClick = onSuggestionClick,
            onAiClick = { onAction(KeyAction.ToggleAiPanel) }
        )
        Spacer(Modifier.height(8.dp))
        when {
            showEmojiPanel -> EmojiPanel(theme, recentEmojis, onAction)
            showAiPanel -> AiToolsPanel(theme)
            else -> KeyRows(theme, shiftState, layoutMode, settings.numberRow, onAction)
        }
    }
}

@Composable
private fun KeyRows(
    theme: KeyboardTheme,
    shiftState: ShiftState,
    layoutMode: LayoutMode,
    showNumberRow: Boolean,
    onAction: (KeyAction) -> Unit
) {
    val letters = layoutMode == LayoutMode.Letters
    val shiftActive = shiftState != ShiftState.Off
    val onText: (String) -> Unit = { onAction(KeyAction.Text(it)) }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (showNumberRow && letters) {
            KeyboardRow(
                keys = NumberRowKeys,
                theme = theme,
                onKey = onText
            )
        }
        KeyboardRow(
            keys = if (letters) KeyboardLayouts.letterRow1 else KeyboardLayouts.symbolRow1,
            theme = theme,
            uppercase = letters && shiftActive,
            onKey = onText
        )
        KeyboardRow(
            keys = if (letters) KeyboardLayouts.letterRow2 else KeyboardLayouts.symbolRow2,
            theme = theme,
            uppercase = letters && shiftActive,
            sideSpacers = letters,
            onKey = onText
        )
        KeyboardRow(
            keys = if (letters) KeyboardLayouts.letterRow3 else KeyboardLayouts.symbolRow3,
            theme = theme,
            uppercase = letters && shiftActive,
            onKey = onText,
            startContent = if (letters) {
                {
                    KeyboardKey(
                        label = "",
                        theme = theme,
                        modifier = Modifier.weight(1.4f),
                        isSpecial = true,
                        icon = KeyIconType.Shift,
                        iconFilled = shiftState != ShiftState.Off,
                        capsBar = shiftState == ShiftState.CapsLock,
                        labelColor = if (shiftState != ShiftState.Off) theme.accent else null
                    ) { onAction(KeyAction.Shift) }
                }
            } else null,
            endContent = {
                KeyboardKey(
                    label = "",
                    theme = theme,
                    modifier = Modifier.weight(1.4f),
                    isSpecial = true,
                    icon = KeyIconType.Backspace,
                    repeatable = true
                ) { onAction(KeyAction.Backspace) }
            }
        )
        BottomKeyboardRow(layoutMode, theme, onAction)
    }
}

private val NumberRowKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
