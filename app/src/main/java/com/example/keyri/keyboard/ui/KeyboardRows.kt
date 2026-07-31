package com.example.keyri.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.keyboard.KeyAction
import com.example.keyri.keyboard.LayoutMode
import com.example.keyri.ui.theme.KeyboardTheme

@Composable
fun KeyboardRow(
    keys: List<String>,
    theme: KeyboardTheme,
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    uppercase: Boolean = false,
    sideSpacers: Boolean = false,
    previewable: Boolean = true,
    startContent: (@Composable RowScope.() -> Unit)? = null,
    endContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        startContent?.invoke(this)
        if (sideSpacers) Spacer(Modifier.weight(0.5f))
        keys.forEach { key ->
            KeyboardKey(
                label = if (uppercase) key.uppercase() else key,
                theme = theme,
                modifier = Modifier.weight(1f),
                previewable = previewable
            ) { onKey(key) }
        }
        if (sideSpacers) Spacer(Modifier.weight(0.5f))
        endContent?.invoke(this)
    }
}

@Composable
fun BottomKeyboardRow(
    layoutMode: LayoutMode,
    theme: KeyboardTheme,
    onAction: (KeyAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyboardKey(
            label = if (layoutMode == LayoutMode.Letters) "?123" else "ABC",
            theme = theme,
            modifier = Modifier.weight(1.3f),
            isSpecial = true,
            fontSize = 14.sp
        ) { onAction(KeyAction.SwitchLayout) }

        if (layoutMode == LayoutMode.Letters) {
            KeyboardKey(
                label = "",
                theme = theme,
                modifier = Modifier.weight(1f),
                isSpecial = true,
                icon = KeyIconType.Emoji
            ) { onAction(KeyAction.ToggleEmoji) }
        } else {
            KeyboardKey(",", theme, Modifier.weight(1f)) { onAction(KeyAction.Text(",")) }
        }

        KeyboardKey(
            label = "PrivKeyAI",
            theme = theme,
            modifier = Modifier.weight(3.4f),
            fontSize = 12.sp,
            labelColor = theme.mutedTextColor,
            onCursorMove = { delta -> onAction(KeyAction.MoveCursor(delta)) }
        ) { onAction(KeyAction.Space) }

        KeyboardKey(".", theme, Modifier.weight(1f)) { onAction(KeyAction.Text(".")) }

        KeyboardKey(
            label = "",
            theme = theme,
            modifier = Modifier.weight(1.4f),
            isAccent = true,
            icon = KeyIconType.Enter
        ) { onAction(KeyAction.Enter) }
    }
}
