package com.example.keyri.ai

/** Converts raw text into model-ready tokens. */
interface Tokenizer {
    fun tokenize(text: String): List<String>
    fun vocabularySize(): Int
}

/**
 * Whitespace/word tokenizer placeholder.
 * TODO: replace with the SentencePiece/BPE tokenizer shipped alongside the TFLite model assets.
 */
class SimpleWordTokenizer : Tokenizer {

    override fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(Regex("[^a-z0-9']+"))
            .filter { it.isNotBlank() }

    override fun vocabularySize(): Int = 0 // TODO: real vocab size once model assets land
}
