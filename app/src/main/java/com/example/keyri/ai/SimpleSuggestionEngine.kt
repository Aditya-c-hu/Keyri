package com.example.keyri.ai

/**
 * Local, frequency/phrase-based suggestion engine.
 * Stateless with respect to user data: context is passed in per call and never stored.
 * Will be replaced by [NextWordPredictor] once the LiteRT model lands.
 */
class SimpleSuggestionEngine {

    private val phraseSuggestions = mapOf(
        "i am going to" to listOf("college", "class", "home"),
        "am going to" to listOf("college", "class", "home"),
        "going to" to listOf("college", "class", "home"),
        "submit the" to listOf("assignment", "report", "project"),
        "call you" to listOf("later", "tomorrow", "tonight"),
        "send me" to listOf("notes", "file", "details"),
        "send the" to listOf("file", "report", "notes"),
        "see you" to listOf("tomorrow", "soon", "later"),
        "let me" to listOf("know", "check", "see"),
        "how are" to listOf("you", "things", "we"),
        "i will" to listOf("be", "send", "do"),
        "thank" to listOf("you", "god", "everyone"),
        "good" to listOf("morning", "night", "evening"),
        "due" to listOf("tomorrow", "today", "soon")
    )

    private val fallbackWords = listOf(
        "the", "you", "to", "and", "assignment", "project", "college", "tomorrow", "today"
    )

    private val dictionary = (fallbackWords + listOf(
        "thanks", "please", "morning", "night", "evening", "later", "tonight",
        "class", "home", "report", "notes", "file", "details", "meeting",
        "submit", "send", "call", "good", "great", "okay", "yes", "sure",
        "exam", "deadline", "team", "update", "work", "done", "soon"
    )).distinct()

    /**
     * Returns up to 3 suggestions for the raw text before the cursor.
     * Ranking: personalized model, then phrase map, then built-in lexicon.
     */
    fun suggest(contextText: String, personal: QuantizedNgramStore? = null): List<String> {
        val tokens = tokenize(contextText)
        val partial = contextText.takeLastWhile { it.isLetter() || it == '\'' }.lowercase()

        val result = linkedSetOf<String>()
        if (partial.isNotEmpty()) {
            personal?.let { result += it.completions(partial, 3) }
            result += phraseMatches(tokens)
            result += EnglishLexicon.completions(partial, 4)
            result += dictionary.filter { it.startsWith(partial) && it != partial }
        } else {
            if (tokens.isNotEmpty()) {
                personal?.let { result += it.predictNext(tokens, 3) }
                result += phraseMatches(tokens)
                result += EnglishLexicon.nextWords(tokens.getOrNull(tokens.size - 2), tokens.last())
            } else {
                result += EnglishLexicon.starters
            }
        }
        result += EnglishLexicon.fallback
        return result.take(MAX_SUGGESTIONS).toList()
    }

    private fun phraseMatches(tokens: List<String>): List<String> {
        for (n in MAX_PHRASE_WORDS downTo 1) {
            if (tokens.size < n) continue
            val key = tokens.takeLast(n).joinToString(" ")
            phraseSuggestions[key]?.let { return it }
        }
        return emptyList()
    }

    // \p{L} keeps any Unicode letter, so non-English words survive tokenization.
    private fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(Regex("[^\\p{L}']+"))
            .filter { it.isNotBlank() }
            .takeLast(MAX_PHRASE_WORDS + 2)

    private companion object {
        const val MAX_SUGGESTIONS = 3
        const val MAX_PHRASE_WORDS = 4
    }
}
