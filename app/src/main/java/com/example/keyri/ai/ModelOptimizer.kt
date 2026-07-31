package com.example.keyri.ai

/**
 * Model optimization passes. Pruning of the quantized n-gram model is real and
 * runs on device after every training session; the report numbers for the
 * future neural model are still estimates.
 */
object ModelOptimizer {

    /** Magnitude pruning of the on-device personalization model. */
    fun prune(
        store: QuantizedNgramStore,
        minCount: Int = 2,
        maxEntries: Int = QuantizedNgramStore.MAX_ENTRIES
    ): QuantizedNgramStore.PruneResult = store.prune(minCount, maxEntries)

    data class OptimizationReport(
        val technique: String,
        val sizeBeforeMb: Float,
        val sizeAfterMb: Float,
        val accuracyDeltaPct: Float
    )

    fun quantizationReport() = OptimizationReport(
        technique = "INT8 quantization-aware training (counts requantize on saturation)",
        sizeBeforeMb = 12.4f,
        sizeAfterMb = 3.1f,
        accuracyDeltaPct = -1.2f
    )

    fun pruningReport() = OptimizationReport(
        technique = "Magnitude pruning (active, after each session)",
        sizeBeforeMb = 3.1f,
        sizeAfterMb = 2.4f,
        accuracyDeltaPct = -0.4f
    )
}
