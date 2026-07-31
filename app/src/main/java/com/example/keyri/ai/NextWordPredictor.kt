package com.example.keyri.ai

/** Contract for any next-word prediction backend (dictionary now, LiteRT/TFLite later). */
interface NextWordPredictor {
    val isReady: Boolean
    fun predictNextWords(context: List<String>, topK: Int = 3): List<String>
    fun warmUp() {}
    fun release() {}
}

/**
 * Placeholder for the on-device LiteRT/TFLite model runner.
 * TODO: load the .tflite asset, create the interpreter, run inference,
 *       and map output logits to words via [Tokenizer].
 */
class TfLitePredictorStub : NextWordPredictor {

    override val isReady: Boolean = false

    override fun predictNextWords(context: List<String>, topK: Int): List<String> {
        // TODO: real inference. Falls back to nothing so callers use SimpleSuggestionEngine.
        return emptyList()
    }
}
