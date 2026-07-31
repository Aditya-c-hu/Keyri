package com.example.keyri.keyboard.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.ui.theme.KeyboardTheme

@Composable
fun SuggestionBar(
    suggestions: List<String>,
    theme: KeyboardTheme,
    aiPanelActive: Boolean,
    onSuggestionClick: (String) -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (aiPanelActive) theme.accent.copy(alpha = 0.25f) else theme.suggestionChipColor)
                .border(1.dp, theme.accent.copy(alpha = if (aiPanelActive) 0.9f else 0.25f), CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onAiClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text("✨", fontSize = 15.sp)
        }

        Spacer(Modifier.width(8.dp))

        if (suggestions.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("PrivKeyAI", color = theme.mutedTextColor, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        } else {
            suggestions.take(3).forEachIndexed { index, word ->
                if (index > 0) {
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(theme.mutedTextColor.copy(alpha = 0.25f))
                    )
                }
                val highlighted = index == suggestions.size / 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 3.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (highlighted) theme.suggestionChipColor else Color.Transparent)
                        .clickable { onSuggestionClick(word) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = word,
                        color = if (highlighted) theme.accent else theme.textColor,
                        fontSize = 14.sp,
                        fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
