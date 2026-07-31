package com.example.keyri.settings

/**
 * User-tunable typing preferences. Every quality-of-life behaviour is optional and
 * defaults to a sensible, unobtrusive value so the keyboard works the same out of
 * the box. Persisted by [SettingsStore] in the shared keyboard preferences.
 */
data class KeyboardSettings(
    val soundOnKey: Boolean = false,
    val hapticsOnKey: Boolean = true,
    val keyPreview: Boolean = true,
    val autoCapitalize: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val numberRow: Boolean = false
)
