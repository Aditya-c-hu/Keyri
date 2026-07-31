package com.example.keyri.keyboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.keyboard.KeyAction
import com.example.keyri.ui.theme.KeyboardTheme

/** Emoji picker shown in place of the key rows, organized by category. */
@Composable
fun EmojiPanel(
    theme: KeyboardTheme,
    recentEmojis: List<String>,
    onAction: (KeyAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Matches the height of the four 46dp key rows so toggling the panel
            // doesn't resize the keyboard.
            .height(206.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            if (recentEmojis.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmojiSectionHeader("Recent", theme)
                }
                items(recentEmojis) { emoji ->
                    EmojiCell(emoji) { onAction(KeyAction.Emoji(emoji)) }
                }
            }
            EmojiCategories.forEach { category ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmojiSectionHeader(category.title, theme)
                }
                items(category.emojis) { emoji ->
                    EmojiCell(emoji) { onAction(KeyAction.Emoji(emoji)) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardKey(
                label = "ABC",
                theme = theme,
                modifier = Modifier.weight(1.3f),
                isSpecial = true,
                fontSize = 14.sp
            ) { onAction(KeyAction.ToggleEmoji) }

            KeyboardKey(
                label = "",
                theme = theme,
                modifier = Modifier.weight(3f)
            ) { onAction(KeyAction.Space) }

            KeyboardKey(
                label = "",
                theme = theme,
                modifier = Modifier.weight(1.3f),
                isSpecial = true,
                icon = KeyIconType.Backspace,
                repeatable = true
            ) { onAction(KeyAction.Backspace) }
        }
    }
}

@Composable
private fun EmojiSectionHeader(title: String, theme: KeyboardTheme) {
    Text(
        text = title,
        color = theme.mutedTextColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp)
    )
}

@Composable
private fun EmojiCell(emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 22.sp)
    }
}
