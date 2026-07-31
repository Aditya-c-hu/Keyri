package com.example.keyri.ai

import java.io.File

/**
 * Unigram/bigram/trigram frequency model with INT8-style quantized counts.
 * Language-agnostic: words are arbitrary Unicode, so non-English vocabulary
 * and word sequences are learned the same way as English.
 *
 * Quantization-aware training: counts live in the 8-bit range (0..255) during
 * training, and when a count would saturate, every count is halved
 * ("requantized") so relative frequencies survive without overflow.
 */
class QuantizedNgramStore {

    private val unigrams = HashMap<String, Int>()
    private val bigrams = HashMap<String, Int>()
    private val trigrams = HashMap<String, Int>()

    val unigramCount: Int get() = unigrams.size
    val totalEntries: Int get() = unigrams.size + bigrams.size + trigrams.size

    /** Approximate serialized size in bytes. */
    fun estimatedSizeBytes(): Int =
        listOf(unigrams, bigrams, trigrams).sumOf { map ->
            map.entries.sumOf { it.key.length + 6 }
        }

    fun observe(previous2: String?, previous: String?, word: String) {
        bump(unigrams, word)
        if (previous != null) {
            bump(bigrams, "$previous $word")
            if (previous2 != null) bump(trigrams, "$previous2 $previous $word")
        }
    }

    private fun bump(map: HashMap<String, Int>, key: String) {
        var next = (map[key] ?: 0) + 1
        if (next > QUANT_MAX) {
            requantize()
            next = (map[key] ?: 0) + 1
        }
        map[key] = next
    }

    /** Halve every count so values stay in 8-bit range while keeping relative order. */
    private fun requantize() {
        listOf(unigrams, bigrams, trigrams).forEach { map ->
            val iterator = map.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val halved = entry.value / 2
                if (halved == 0) iterator.remove() else entry.setValue(halved)
            }
        }
    }

    /** Predicts next words from the longest matching learned sequence (trigram, then bigram). */
    fun predictNext(context: List<String>, topK: Int): List<String> {
        val result = LinkedHashSet<String>()
        if (context.size >= 2) {
            val prefix = "${context[context.size - 2]} ${context.last()} "
            trigrams.entries.asSequence()
                .filter { it.key.startsWith(prefix) }
                .sortedByDescending { it.value }
                .take(topK)
                .forEach { result += it.key.removePrefix(prefix) }
        }
        if (context.isNotEmpty()) {
            val prefix = "${context.last()} "
            bigrams.entries.asSequence()
                .filter { it.key.startsWith(prefix) }
                .sortedByDescending { it.value }
                .take(topK)
                .forEach { result += it.key.removePrefix(prefix) }
        }
        return result.take(topK).toList()
    }

    fun completions(partial: String, topK: Int): List<String> =
        unigrams.entries
            .filter { it.key.startsWith(partial) && it.key != partial }
            .sortedByDescending { it.value }
            .take(topK)
            .map { it.key }

    /**
     * Magnitude pruning: drops entries with counts below [minCount], then caps
     * each table at [maxEntries], keeping the highest-frequency entries.
     */
    fun prune(minCount: Int = 2, maxEntries: Int = MAX_ENTRIES): PruneResult {
        val before = totalEntries
        listOf(unigrams, bigrams, trigrams).forEach { map ->
            map.entries.removeAll { it.value < minCount }
            if (map.size > maxEntries) {
                val keepKeys = map.entries
                    .sortedByDescending { it.value }
                    .take(maxEntries)
                    .mapTo(HashSet()) { it.key }
                map.keys.retainAll(keepKeys)
            }
        }
        return PruneResult(entriesBefore = before, entriesAfter = totalEntries)
    }

    fun save(file: File) {
        file.bufferedWriter().use { out ->
            unigrams.forEach { (word, count) -> out.write("u\t$word\t$count\n") }
            bigrams.forEach { (pair, count) -> out.write("b\t$pair\t$count\n") }
            trigrams.forEach { (triple, count) -> out.write("t\t$triple\t$count\n") }
        }
    }

    fun load(file: File) {
        if (!file.exists()) return
        unigrams.clear()
        bigrams.clear()
        trigrams.clear()
        file.forEachLine { line ->
            val parts = line.split('\t')
            if (parts.size == 3) {
                val count = parts[2].toIntOrNull()?.coerceIn(1, QUANT_MAX) ?: return@forEachLine
                when (parts[0]) {
                    "u" -> unigrams[parts[1]] = count
                    "b" -> bigrams[parts[1]] = count
                    "t" -> trigrams[parts[1]] = count
                }
            }
        }
    }

    data class PruneResult(val entriesBefore: Int, val entriesAfter: Int)

    companion object {
        const val QUANT_MAX = 255
        const val MAX_ENTRIES = 2000
    }
}
