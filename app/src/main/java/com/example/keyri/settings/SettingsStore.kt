package com.example.keyri.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.keyri.keyboard.PrivKeyKeyboardService

/**
 * Reads and writes [KeyboardSettings] plus the recently-used emoji list against the
 * shared keyboard preferences file. Keeps all preference keys in one place so the
 * app UI and the IME service stay in sync.
 */
object SettingsStore {

    private const val KEY_SOUND = "qol_sound_on_key"
    private const val KEY_HAPTICS = "qol_haptics_on_key"
    private const val KEY_PREVIEW = "qol_key_preview"
    private const val KEY_AUTO_CAP = "qol_auto_capitalize"
    private const val KEY_DOUBLE_SPACE = "qol_double_space_period"
    private const val KEY_NUMBER_ROW = "qol_number_row"
    private const val KEY_RECENT_EMOJIS = "qol_recent_emojis"

    private const val RECENT_EMOJI_LIMIT = 24
    private const val RECENT_DELIMITER = "␟" // unit separator, never appears in emoji

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PrivKeyKeyboardService.PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): KeyboardSettings = load(prefs(context))

    fun load(prefs: SharedPreferences): KeyboardSettings = KeyboardSettings(
        soundOnKey = prefs.getBoolean(KEY_SOUND, false),
        hapticsOnKey = prefs.getBoolean(KEY_HAPTICS, true),
        keyPreview = prefs.getBoolean(KEY_PREVIEW, true),
        autoCapitalize = prefs.getBoolean(KEY_AUTO_CAP, true),
        doubleSpacePeriod = prefs.getBoolean(KEY_DOUBLE_SPACE, true),
        numberRow = prefs.getBoolean(KEY_NUMBER_ROW, false)
    )

    fun save(prefs: SharedPreferences, settings: KeyboardSettings) {
        prefs.edit()
            .putBoolean(KEY_SOUND, settings.soundOnKey)
            .putBoolean(KEY_HAPTICS, settings.hapticsOnKey)
            .putBoolean(KEY_PREVIEW, settings.keyPreview)
            .putBoolean(KEY_AUTO_CAP, settings.autoCapitalize)
            .putBoolean(KEY_DOUBLE_SPACE, settings.doubleSpacePeriod)
            .putBoolean(KEY_NUMBER_ROW, settings.numberRow)
            .apply()
    }

    fun loadRecentEmojis(prefs: SharedPreferences): List<String> =
        prefs.getString(KEY_RECENT_EMOJIS, "")
            ?.split(RECENT_DELIMITER)
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    /** Returns the updated recents list with [emoji] moved to the front, capped and persisted. */
    fun pushRecentEmoji(prefs: SharedPreferences, current: List<String>, emoji: String): List<String> {
        val updated = (listOf(emoji) + current.filter { it != emoji }).take(RECENT_EMOJI_LIMIT)
        prefs.edit().putString(KEY_RECENT_EMOJIS, updated.joinToString(RECENT_DELIMITER)).apply()
        return updated
    }
}
