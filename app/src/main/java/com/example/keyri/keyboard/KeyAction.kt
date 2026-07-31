package com.example.keyri.keyboard

/** Everything a key press can ask the IME service to do. */
sealed interface KeyAction {
    data class Text(val text: String) : KeyAction
    data class Emoji(val emoji: String) : KeyAction
    /** Move the text cursor left (negative) or right (positive) by [delta] characters. */
    data class MoveCursor(val delta: Int) : KeyAction
    data object Space : KeyAction
    data object Backspace : KeyAction
    data object Enter : KeyAction
    data object Shift : KeyAction
    data object SwitchLayout : KeyAction
    data object ToggleAiPanel : KeyAction
    data object ToggleEmoji : KeyAction
}

enum class ShiftState { Off, Shift, CapsLock }

enum class LayoutMode { Letters, Symbols }
