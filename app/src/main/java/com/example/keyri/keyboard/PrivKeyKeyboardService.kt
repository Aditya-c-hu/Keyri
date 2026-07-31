package com.example.keyri.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.keyri.ai.PersonalizationTrainer
import com.example.keyri.ai.SimpleSuggestionEngine
import com.example.keyri.keyboard.ui.PrivKeyKeyboardScreen
import com.example.keyri.security.PrivacyGuard
import com.example.keyri.settings.KeyboardSettings
import com.example.keyri.settings.SettingsStore
import com.example.keyri.ui.theme.KeyboardThemes

/**
 * PrivKeyAI IME. Renders a Compose keyboard inside the InputMethodService window,
 * which requires the service to act as Lifecycle and SavedStateRegistry owner.
 * The owners must be set on the IME window's decor view: Compose resolves them
 * from the window root, so setting them only on the ComposeView crashes with
 * "ViewTreeLifecycleOwner not found".
 */
class PrivKeyKeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val suggestionEngine = SimpleSuggestionEngine()
    private lateinit var trainer: PersonalizationTrainer

    private var shiftState by mutableStateOf(ShiftState.Off)
    private var layoutMode by mutableStateOf(LayoutMode.Letters)
    private var suggestions by mutableStateOf(emptyList<String>())
    private var showAiPanel by mutableStateOf(false)
    private var showEmojiPanel by mutableStateOf(false)
    private var theme by mutableStateOf(KeyboardThemes.Graphite)
    private var settings by mutableStateOf(KeyboardSettings())
    private var recentEmojis by mutableStateOf<List<String>>(emptyList())
    private var suggestionsEnabled = true
    private var learningAllowed = false
    private var lastLearnedKey: String? = null
    // Suppresses auto-capitalization for one refresh after the user manually toggles
    // shift, so the keyboard never fights an explicit choice.
    private var suppressAutoCapOnce = false

    // Snapshot of the latest learnable text, so the final words of a message are
    // still learned when the field closes without a trailing space/enter
    // (e.g. tapping an app's send button).
    private var pendingContext = ""

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        trainer = PersonalizationTrainer(this)
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PrivKeyKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@PrivKeyKeyboardService)
            setContent {
                PrivKeyKeyboardScreen(
                    theme = this@PrivKeyKeyboardService.theme,
                    shiftState = shiftState,
                    layoutMode = this@PrivKeyKeyboardService.layoutMode,
                    suggestions = suggestions,
                    showAiPanel = showAiPanel,
                    showEmojiPanel = showEmojiPanel,
                    settings = settings,
                    recentEmojis = recentEmojis,
                    onAction = ::handleAction,
                    onSuggestionClick = ::commitSuggestion
                )
            }
        }
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        theme = KeyboardThemes.byId(prefs.getString(KEY_THEME, KeyboardThemes.Graphite.id))
        settings = SettingsStore.load(prefs)
        recentEmojis = SettingsStore.loadRecentEmojis(prefs)

        val inputType = editorInfo?.inputType ?: 0
        suggestionsEnabled = !PrivacyGuard.isPasswordField(inputType)
        learningAllowed = PrivacyGuard.shouldLearnFromCurrentInput(inputType)

        shiftState = ShiftState.Off
        layoutMode = LayoutMode.Letters
        showAiPanel = false
        showEmojiPanel = false
        lastLearnedKey = null
        pendingContext = ""
        suppressAutoCapOnce = false
        refreshSuggestions()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        if (::trainer.isInitialized) {
            // Learn the trailing words that never got a space/enter boundary
            // (message sent via the app's send button), then end the session.
            learnFromText(pendingContext)
            pendingContext = ""
            trainer.endSession()
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        refreshSuggestions()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onDestroy() {
        if (::trainer.isInitialized) trainer.endSession()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun handleAction(action: KeyAction) {
        val ic = currentInputConnection ?: return
        when (action) {
            is KeyAction.Text -> {
                val text = if (shiftState != ShiftState.Off) action.text.uppercase() else action.text
                ic.commitText(text, 1)
                if (shiftState == ShiftState.Shift) shiftState = ShiftState.Off
                if (action.text in WORD_SEPARATORS) maybeLearn(ic)
                refreshSuggestions()
            }

            is KeyAction.Emoji -> {
                ic.commitText(action.emoji, 1)
                rememberEmoji(action.emoji)
                refreshSuggestions()
            }

            is KeyAction.MoveCursor -> {
                // DPAD events move the cursor the same way hardware arrow keys would,
                // so they work in every editor. onUpdateSelection refreshes suggestions.
                val keyCode =
                    if (action.delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
                repeat(kotlin.math.abs(action.delta)) { sendDownUpKeyEvents(keyCode) }
            }

            KeyAction.Space -> {
                if (!(settings.doubleSpacePeriod && tryDoubleSpacePeriod(ic))) {
                    ic.commitText(" ", 1)
                }
                maybeLearn(ic)
                refreshSuggestions()
            }

            KeyAction.Backspace -> {
                val selected = ic.getSelectedText(0)
                if (selected.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
                refreshSuggestions()
            }

            KeyAction.Enter -> {
                maybeLearn(ic)
                if (!sendDefaultEditorAction(true)) {
                    ic.commitText("\n", 1)
                }
                refreshSuggestions()
            }

            KeyAction.Shift -> {
                shiftState = when (shiftState) {
                    ShiftState.Off -> ShiftState.Shift
                    ShiftState.Shift -> ShiftState.CapsLock
                    ShiftState.CapsLock -> ShiftState.Off
                }
                // Respect the manual choice over the next auto-capitalization pass.
                suppressAutoCapOnce = true
            }

            KeyAction.SwitchLayout -> layoutMode =
                if (layoutMode == LayoutMode.Letters) LayoutMode.Symbols else LayoutMode.Letters

            KeyAction.ToggleAiPanel -> {
                showAiPanel = !showAiPanel
                if (showAiPanel) showEmojiPanel = false
            }

            KeyAction.ToggleEmoji -> {
                showEmojiPanel = !showEmojiPanel
                if (showEmojiPanel) showAiPanel = false
            }
        }
    }

    private fun commitSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(MAX_CONTEXT, 0)?.toString().orEmpty()
        val partial = before.takeLastWhile { it.isLetterOrDigit() || it == '\'' }
        if (partial.isNotEmpty() && word.startsWith(partial, ignoreCase = true)) {
            ic.deleteSurroundingText(partial.length, 0)
        }
        ic.commitText("$word ", 1)
        maybeLearn(ic)
        refreshSuggestions()
    }

    /** Feeds the last completed word sequence to the trainer, once per boundary. */
    private fun maybeLearn(ic: InputConnection) {
        if (!learningAllowed) return
        learnFromText(ic.getTextBeforeCursor(MAX_CONTEXT, 0)?.toString().orEmpty())
    }

    private fun learnFromText(text: String) {
        val words = lastWords(text, 3)
        if (words.isEmpty()) return
        val key = words.joinToString(" ")
        if (key == lastLearnedKey) return
        lastLearnedKey = key
        trainer.learnFrom(words)
    }

    private fun lastWords(text: String, n: Int): List<String> =
        text.lowercase()
            .split(WORD_SPLIT_REGEX)
            .filter { it.isNotBlank() }
            .takeLast(n)

    private fun refreshSuggestions() {
        val before = currentInputConnection?.getTextBeforeCursor(MAX_CONTEXT, 0)?.toString().orEmpty()
        applyAutoCapitalization(before)
        if (!suggestionsEnabled) {
            suggestions = emptyList()
            return
        }
        if (learningAllowed) pendingContext = before
        suggestions = suggestionEngine.suggest(before, trainer.store)
    }

    /**
     * Sets shift at the start of the field and after sentence punctuation so the next
     * letter is capitalized, matching standard keyboards. Caps Lock and a just-made
     * manual shift choice are left untouched.
     */
    private fun applyAutoCapitalization(before: String) {
        if (suppressAutoCapOnce) {
            suppressAutoCapOnce = false
            return
        }
        if (!settings.autoCapitalize) return
        if (layoutMode != LayoutMode.Letters) return
        if (shiftState == ShiftState.CapsLock) return
        shiftState = if (shouldCapitalizeNext(before)) ShiftState.Shift else ShiftState.Off
    }

    private fun shouldCapitalizeNext(before: String): Boolean {
        val trimmed = before.trimEnd()
        if (trimmed.isEmpty()) return true
        // Capitalize only if sentence punctuation is followed by a space we just typed.
        if (before.endsWith(" ") || before.endsWith("\n")) {
            return trimmed.last() in SENTENCE_ENDINGS
        }
        return false
    }

    /** Turns a second consecutive space after a word into ". ". Returns true if applied. */
    private fun tryDoubleSpacePeriod(ic: InputConnection): Boolean {
        val before = ic.getTextBeforeCursor(2, 0)?.toString() ?: return false
        if (before.length == 2 && before[1] == ' ' && before[0].isLetterOrDigit()) {
            ic.deleteSurroundingText(1, 0)
            ic.commitText(". ", 1)
            return true
        }
        return false
    }

    private fun rememberEmoji(emoji: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        recentEmojis = SettingsStore.pushRecentEmoji(prefs, recentEmojis, emoji)
    }

    companion object {
        const val PREFS_NAME = "privkeyai_prefs"
        const val KEY_THEME = "keyboard_theme"
        const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val MAX_CONTEXT = 64
        private val WORD_SEPARATORS = setOf(",", ".", "!", "?", ";", ":")
        private val SENTENCE_ENDINGS = setOf('.', '!', '?')

        // \p{L} matches any Unicode letter so non-English words are kept.
        private val WORD_SPLIT_REGEX = Regex("[^\\p{L}']+")
    }
}
