package com.example.keyri.ai

import android.content.Context
import java.io.File

/**
 * On-device, quantization-aware personalization trainer.
 *
 * Learns word frequencies and word pairs from typing into an 8-bit quantized
 * [QuantizedNgramStore]. After each training session (leaving a text field),
 * the model automatically prunes itself via [ModelOptimizer] and persists to
 * app-private storage. Raw text is never stored — only word frequencies.
 *
 * TODO: neural QAT (fake-quant fine-tuning of the LiteRT model) once the
 *       TFLite next-word model ships; this n-gram path stays as fallback.
 */
class PersonalizationTrainer(context: Context) {

    val store = QuantizedNgramStore()

    private val modelFile = File(context.filesDir, MODEL_FILE)
    private val prefs = context.getSharedPreferences(MODEL_PREFS, Context.MODE_PRIVATE)
    private var updatesThisSession = 0

    init {
        runCatching { store.load(modelFile) }
    }

    /**
     * Learns from the last completed words (newest word last). Any Unicode script
     * counts, so non-English vocabulary and sequences are learned too.
     */
    fun learnFrom(words: List<String>) {
        val word = words.lastOrNull() ?: return
        if (!isLearnable(word)) return
        val previous = words.getOrNull(words.size - 2)?.takeIf { isLearnable(it) }
        val previous2 = if (previous != null) {
            words.getOrNull(words.size - 3)?.takeIf { isLearnable(it) }
        } else null
        store.observe(previous2, previous, word)
        updatesThisSession++
    }

    /** Ends a training session: auto-prunes the quantized model, then persists it. */
    fun endSession() {
        if (updatesThisSession == 0) return
        // minCount = 1 keeps once-typed words (e.g. romanized non-English vocabulary);
        // pruning still enforces the capacity cap and requantization drops dead entries.
        val result = ModelOptimizer.prune(store, minCount = 1)
        runCatching { store.save(modelFile) }
        prefs.edit()
            .putBoolean(KEY_PRUNED, true)
            .putInt(KEY_LAST_PRUNE_REMOVED, result.entriesBefore - result.entriesAfter)
            .apply()
        updatesThisSession = 0
    }

    fun personalWordsLearned(): Int = store.unigramCount

    /** Letters-only words; never digits, so OTPs and numbers are never learned. */
    private fun isLearnable(word: String): Boolean =
        word.length in 2..24 && word.all { it.isLetter() || it == '\'' }

    companion object {
        const val MODEL_FILE = "personal_ngram_model.tsv"
        const val MODEL_PREFS = "privkeyai_model"
        const val KEY_PRUNED = "model_pruned"
        const val KEY_LAST_PRUNE_REMOVED = "last_prune_removed"
    }
}
