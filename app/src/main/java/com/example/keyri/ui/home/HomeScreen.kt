package com.example.keyri.ui.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyri.ai.ModelStats
import com.example.keyri.keyboard.PrivKeyKeyboardService
import com.example.keyri.settings.KeyboardSettings
import com.example.keyri.settings.SettingsStore
import com.example.keyri.ui.theme.AccentSlate
import com.example.keyri.ui.theme.KeyboardTheme
import com.example.keyri.ui.theme.KeyboardThemes
import com.example.keyri.ui.theme.TextBright
import kotlin.math.roundToInt

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PrivKeyKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)
    }
    var selectedThemeId by remember {
        mutableStateOf(
            prefs.getString(PrivKeyKeyboardService.KEY_THEME, KeyboardThemes.Graphite.id)
                ?: KeyboardThemes.Graphite.id
        )
    }
    var privacyMode by remember {
        mutableStateOf(prefs.getBoolean(PrivKeyKeyboardService.KEY_PRIVACY_MODE, true))
    }
    var typingSettings by remember { mutableStateOf(SettingsStore.load(prefs)) }
    val modelStats = remember { ModelStats.load(context) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            HeroSection()
            SetupButtons()
            SetupGuideCard()
            FeatureGrid()
            DashboardCard(modelStats)
            TypingPreferencesCard(typingSettings) { updated ->
                typingSettings = updated
                SettingsStore.save(prefs, updated)
            }
            ThemeSelector(selectedThemeId) { id ->
                selectedThemeId = id
                prefs.edit().putString(PrivKeyKeyboardService.KEY_THEME, id).apply()
            }
            PrivacyCard(privacyMode) { enabled ->
                privacyMode = enabled
                prefs.edit().putBoolean(PrivKeyKeyboardService.KEY_PRIVACY_MODE, enabled).apply()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroSection() {
    Column {
        Text(
            text = "PrivKeyAI",
            style = TextStyle(
                brush = Brush.linearGradient(listOf(TextBright, AccentSlate)),
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Private. Fast. Personalized.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetupButtons() {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Enable Keyboard", fontSize = 13.sp)
        }
        OutlinedButton(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Choose PrivKeyAI", fontSize = 13.sp)
        }
    }
}

@Composable
private fun SetupGuideCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Setup guide",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            listOf(
                "Tap Enable Keyboard and turn on PrivKeyAI Keyboard",
                "Tap Choose PrivKeyAI and select it as the active keyboard",
                "Open any app and start typing",
                "Tap ✨ on the keyboard to preview AI tools"
            ).forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard("✨", "AI Suggestions", "Next-word prediction, fully on device", Modifier.weight(1f))
            FeatureCard("🔒", "Privacy Mode", "Never learns from passwords or secure fields", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard("⚡", "Model Optimization", "Quantization-aware training with auto-pruning", Modifier.weight(1f))
            FeatureCard("🎨", "Themes", "Graphite, AMOLED and light keyboard styles", Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeatureCard(emoji: String, title: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardCard(stats: ModelStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Model dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (stats.isQuantized) "INT8" else "FP32",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            StatRow("Model", "INT8 n-gram (${stats.modelVersion})")
            StatRow("Size", formatModelSize(stats.modelSizeMb))
            StatRow("Latency", "${stats.latencyMs} ms")
            StatRow("Top-3 Accuracy", "${(stats.top3Accuracy * 100).toInt()}% (est.)")
            StatRow("Pruning", if (stats.isPruned) "Auto (on-device)" else "After first session")
            StatRow("Personal words learned", "${stats.personalWordsLearned}", divider = false)
        }
    }
}

private fun formatModelSize(sizeMb: Float): String =
    if (sizeMb >= 1f) "%.1f MB".format(sizeMb)
    else "${(sizeMb * 1024).roundToInt().coerceAtLeast(1)} KB"

@Composable
private fun StatRow(label: String, value: String, divider: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun ThemeSelector(selectedId: String, onSelect: (String) -> Unit) {
    Column {
        Text(
            "Keyboard theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KeyboardThemes.all.forEach { theme ->
                ThemePreviewCard(
                    theme = theme,
                    selected = theme.id == selectedId,
                    modifier = Modifier.weight(1f)
                ) { onSelect(theme.id) }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: KeyboardTheme,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(theme.background),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.keyColor)
                            .border(0.5.dp, theme.accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(theme.displayName, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun TypingPreferencesCard(
    settings: KeyboardSettings,
    onChange: (KeyboardSettings) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Typing preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "All optional. Tune how the keyboard feels and behaves.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                "Key preview popup",
                "Show a glyph bubble above the key you press",
                settings.keyPreview
            ) { onChange(settings.copy(keyPreview = it)) }
            ToggleRow(
                "Number row",
                "Add a dedicated row of digits above the letters",
                settings.numberRow
            ) { onChange(settings.copy(numberRow = it)) }
            ToggleRow(
                "Auto-capitalization",
                "Capitalize the first letter of each sentence",
                settings.autoCapitalize
            ) { onChange(settings.copy(autoCapitalize = it)) }
            ToggleRow(
                "Double-space full stop",
                "Tap space twice to insert a period",
                settings.doubleSpacePeriod
            ) { onChange(settings.copy(doubleSpacePeriod = it)) }
            ToggleRow(
                "Haptic feedback",
                "Vibrate softly on each key press",
                settings.hapticsOnKey
            ) { onChange(settings.copy(hapticsOnKey = it)) }
            ToggleRow(
                "Key press sound",
                "Play the system keypress click",
                settings.soundOnKey,
                divider = false
            ) { onChange(settings.copy(soundOnKey = it)) }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    divider: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onToggle)
    }
    if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun PrivacyCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Privacy Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Everything stays on device. Passwords and secure fields are never learned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}
