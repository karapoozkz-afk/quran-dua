package app.bibliya.shared.search

import app.bibliya.shared.model.Passage
import app.bibliya.shared.model.Situation
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class SearchResults(
    val situations: List<Situation>,
    val passages: List<Passage>,
    /** Stems to look up in the full Bible text. */
    val bibleTerms: List<String>,
) {
    val isEmpty: Boolean get() = situations.isEmpty() && passages.isEmpty()
}

/**
 * Offline "What should I read?" search.
 *
 * Understands natural phrases ("что почитать когда тревожно", "my father died") without a
 * network: stop words are dropped, words are matched by shared stems against each situation's
 * keyword list in both languages, and passages inherit part of the score of their situations.
 */
class SmartSearch(
    situations: List<Situation>,
    passages: List<Passage>,
) {
    private class SituationDoc(
        val situation: Situation,
        val tokens: Set<String>,
        val phrases: List<String>,
    )

    private class PassageDoc(
        val item: Passage,
        val titleTokens: Set<String>,
        val bodyTokens: Set<String>,
    )

    private val situationDocs = situations.map { s ->
        val keywordPhrases = s.keywords.values.flatten().map(TextNormalizer::normalize)
        val tokens = buildSet {
            keywordPhrases.forEach { addAll(it.split(' ')) }
            s.title.values.forEach { addAll(TextNormalizer.tokens(it)) }
        }.filterNot { it in STOP_WORDS || it.length < 2 }.toSet()
        SituationDoc(s, tokens, keywordPhrases.filter { ' ' in it })
    }

    private val passageDocs = passages.map { p ->
        PassageDoc(
            item = p,
            titleTokens = p.title.values.flatMap(TextNormalizer::tokens).filterNot { it in STOP_WORDS }.toSet(),
            bodyTokens = buildList {
                p.text.values.forEach { verses -> verses.forEach { addAll(TextNormalizer.tokens(it[1])) } }
                p.note?.values?.forEach { addAll(TextNormalizer.tokens(it)) }
            }.filterNot { it in STOP_WORDS || it.length < 3 }.toSet(),
        )
    }

    fun search(query: String, maxPassages: Int = 30): SearchResults {
        val normalized = TextNormalizer.normalize(query)
        val queryTokens = normalized.split(' ')
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .distinct()
        if (queryTokens.isEmpty()) return SearchResults(emptyList(), emptyList(), emptyList())

        // Rarer words decide more: "перед" fits many situations, "дорога" fits one.
        val weights = queryTokens.associateWith { q ->
            val hits = situationDocs.count { doc -> doc.tokens.any { similarity(q, it) > 0 } }
            1.0 / sqrt(max(1, hits).toDouble())
        }

        val situationScores = situationDocs.associate { doc ->
            var score = 0.0
            for (q in queryTokens) {
                score += weights.getValue(q) * (doc.tokens.maxOfOrNull { similarity(q, it) } ?: 0.0)
            }
            for (phrase in doc.phrases) {
                if (" $normalized ".contains(" $phrase ")) score += 1.0
            }
            doc.situation.id to score
        }

        val rankedSituations = situationDocs
            .filter { situationScores.getValue(it.situation.id) >= MIN_SCORE }
            .sortedByDescending { situationScores.getValue(it.situation.id) }
            .map { it.situation }

        val rankedPassages = passageDocs.map { doc ->
            var score = 0.0
            for (q in queryTokens) {
                val title = doc.titleTokens.maxOfOrNull { similarity(q, it) } ?: 0.0
                val body = (doc.bodyTokens.maxOfOrNull { similarity(q, it) } ?: 0.0) * 0.6
                score += weights.getValue(q) * max(title, body)
            }
            val inherited = doc.item.sit.maxOfOrNull { situationScores[it] ?: 0.0 } ?: 0.0
            doc.item to score + inherited * 0.7
        }
            .filter { it.second >= MIN_SCORE }
            .sortedByDescending { it.second }
            .take(maxPassages)
            .map { it.first }

        val terms = queryTokens.map(TextNormalizer::stem).filter { it.length >= 3 }.distinct().take(6)
        return SearchResults(rankedSituations, rankedPassages, terms)
    }

    companion object {
        private const val MIN_SCORE = 0.5

        /** 1.0 for the same word, 0.8 for the same stem, 0 otherwise. */
        fun similarity(a: String, b: String): Double {
            if (a == b) return 1.0
            if (a.length < 4 || b.length < 4) return 0.0
            var common = 0
            val limit = min(a.length, b.length)
            while (common < limit && a[common] == b[common]) common++
            val needed = max(4, limit - 2)
            return if (common >= needed) 0.8 else 0.0
        }

        val STOP_WORDS = setOf(
            // ru
            "что", "чтобы", "читать", "почитать", "прочитать", "читаю", "какую", "какой", "какие", "какая",
            "каким", "когда", "если", "мне", "меня", "мой", "моя", "мои", "мое", "моего", "моей", "моих",
            "за", "от", "для", "при", "на", "во", "как", "со", "по", "об", "хочу", "нужно", "надо", "можно",
            "могу", "не", "нет", "бог", "бога", "богу", "господь", "господа", "библия", "библии", "стих",
            "стихи", "это", "очень", "сильно", "много", "у", "я", "в", "и", "а", "с", "к", "о", "ли", "бы",
            "же", "его", "ее", "её", "он", "она", "они", "мы", "вы", "ты", "уже", "тоже", "просто", "сейчас",
            "который", "которая", "место", "где", "про", "есть",
            // en
            "what", "to", "read", "verse", "verses", "bible", "which", "when", "if", "i", "me", "my", "for",
            "from", "at", "in", "on", "the", "a", "an", "and", "of", "is", "am", "are", "want", "should",
            "can", "do", "how", "god", "lord", "have", "has", "lot", "very", "im", "it", "with", "about",
            "after", "before", "someone", "something", "where", "says",
        ).map(TextNormalizer::normalize).toSet()
    }
}
