package app.qurandua.shared.search

import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.allValues
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class SearchResults(
    val situations: List<Situation>,
    val duas: List<DuaItem>,
    /** Stems to look up in Quran translations and Arabic text. */
    val quranTerms: List<String>,
) {
    val isEmpty: Boolean get() = situations.isEmpty() && duas.isEmpty()
}

/**
 * Offline "What should I read?" search.
 *
 * Understands natural phrases ("что читать за умершего отца", "I have a lot of debt")
 * without a network: stop words are dropped, words are matched by shared stems
 * against each situation's keyword list in every language, and duas inherit part of
 * the score of the situations they belong to. It runs on device in a few milliseconds
 * over the bundled content, so it also works on iOS through the shared module.
 */
class SmartSearch(
    situations: List<Situation>,
    duas: List<DuaItem>,
) {
    private class SituationDoc(
        val situation: Situation,
        val tokens: Set<String>,
        val phrases: List<String>,
    )

    private class DuaDoc(
        val item: DuaItem,
        val titleTokens: Set<String>,
        val bodyTokens: Set<String>,
    )

    private val situationDocs = situations.map { s ->
        val keywordPhrases = s.keywords.allValues().map(TextNormalizer::normalize)
        val tokens = buildSet {
            keywordPhrases.forEach { addAll(it.split(' ')) }
            s.title.values.forEach { addAll(TextNormalizer.tokens(it)) }
        }.filterNot { it in STOP_WORDS || it.length < 2 }.toSet()
        SituationDoc(s, tokens, keywordPhrases.filter { ' ' in it })
    }

    private val duaDocs = duas.map { d ->
        DuaDoc(
            item = d,
            titleTokens = d.title.values.flatMap(TextNormalizer::tokens).filterNot { it in STOP_WORDS }.toSet(),
            bodyTokens = buildList {
                d.translation.values.forEach { addAll(TextNormalizer.tokens(it)) }
                d.note?.values?.forEach { addAll(TextNormalizer.tokens(it)) }
                d.arabic?.let { addAll(TextNormalizer.tokens(it)) }
            }.filterNot { it in STOP_WORDS || it.length < 3 }.toSet(),
        )
    }

    fun search(query: String, maxDuas: Int = 30): SearchResults {
        val normalized = TextNormalizer.normalize(query)
        val queryTokens = normalized.split(' ')
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .distinct()
        if (queryTokens.isEmpty()) return SearchResults(emptyList(), emptyList(), emptyList())

        // Rarer words decide more: "перед" fits many situations, "дорогой" fits one.
        val weights = queryTokens.associateWith { q ->
            val hits = situationDocs.count { doc -> doc.tokens.any { similarity(q, it) > 0 } }
            1.0 / sqrt(max(1, hits).toDouble())
        }

        val situationScores = situationDocs.associate { doc ->
            var score = 0.0
            for (q in queryTokens) {
                score += weights.getValue(q) * doc.tokens.maxOfOrNull { similarity(q, it) }.orZero()
            }
            for (phrase in doc.phrases) {
                if (containsPhrase(normalized, phrase)) score += 1.0
            }
            doc.situation.id to score
        }

        val rankedSituations = situationDocs
            .filter { situationScores.getValue(it.situation.id) >= MIN_SCORE }
            .sortedByDescending { situationScores.getValue(it.situation.id) }
            .map { it.situation }

        val rankedDuas = duaDocs.map { doc ->
            var score = 0.0
            for (q in queryTokens) {
                val title = doc.titleTokens.maxOfOrNull { similarity(q, it) }.orZero()
                val body = doc.bodyTokens.maxOfOrNull { similarity(q, it) }.orZero() * 0.6
                score += weights.getValue(q) * max(title, body)
            }
            val inherited = doc.item.situations.maxOfOrNull { situationScores[it] ?: 0.0 }.orZero()
            doc.item to score + inherited * 0.7
        }
            .filter { it.second >= MIN_SCORE }
            .sortedByDescending { it.second }
            .take(maxDuas)
            .map { it.first }

        return SearchResults(rankedSituations, rankedDuas, quranTerms(queryTokens))
    }

    private fun quranTerms(queryTokens: List<String>): List<String> {
        val terms = LinkedHashSet<String>()
        for (q in queryTokens) {
            terms += TextNormalizer.stem(q)
            for (group in QURAN_CONCEPTS) {
                if (group.triggers.any { similarity(q, it) > 0 }) terms += group.terms
            }
        }
        return terms.filter { it.length >= 3 }.take(8)
    }

    private fun containsPhrase(query: String, phrase: String): Boolean =
        " $query ".contains(" $phrase ")

    private fun Double?.orZero(): Double = this ?: 0.0

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
            "что", "чтобы", "читать", "читают", "прочитать", "почитать", "читаю", "какую", "какой", "какие",
            "какая", "каким", "дуа", "дуъа", "дога", "когда", "если", "мне", "меня", "мой", "моя", "мои",
            "мое", "моего", "моей", "моих", "за", "от", "для", "при", "на", "во", "как", "со", "по", "об",
            "хочу", "нужно", "надо", "можно", "могу", "не", "нет", "аллах", "аллаха", "это", "очень",
            "сильно", "много", "у", "я", "в", "и", "а", "с", "к", "о", "ли", "бы", "же", "его", "ее",
            "её", "он", "она", "они", "мы", "вы", "ты", "уже", "тоже", "просто", "сейчас", "который", "которая",
            // kk
            "не", "оқу", "оқимын", "оқыған", "дұға", "дұғасы", "дұғалар", "үшін", "менің", "мен", "маған",
            "бар", "керек", "қандай", "қашан", "егер", "және", "бұл", "өте", "қатты", "көп", "алла",
            "алдында", "алдындағы", "кейін", "кейінгі",
            // id
            "apa", "yang", "dan", "di", "ke", "dari", "untuk", "buat", "bagi", "pada", "dengan", "saya", "aku",
            "kami", "kita", "ku", "nya", "doa", "baca", "bacaan", "membaca", "dibaca", "ketika", "saat", "waktu",
            "kalau", "jika", "agar", "supaya", "ini", "itu", "ada", "sedang", "mau", "ingin", "bagaimana", "cara",
            "tolong", "ya", "dong", "sih",
            // tr
            "ne", "neler", "nasıl", "için", "ile", "ve", "veya", "bir", "bu", "şu", "da", "de", "ki", "mi", "mı",
            "mu", "mü", "ben", "benim", "bana", "beni", "sen", "senin", "zaman", "olunca", "iken", "okunur",
            "okunacak", "okumak", "oku", "duası", "duaları", "gibi", "çok", "daha", "en", "var", "yok",
            // ur (Urdu script and Roman Urdu)
            "کیا", "کے", "کی", "کا", "میں", "سے", "لیے", "لئے", "کو", "پر", "اور", "ہے", "ہیں", "ہو", "تھا", "یہ",
            "وہ", "مجھے", "میرا", "میری", "میرے", "جب", "کب", "کیسے", "کون", "کوئی", "بھی", "دعا", "دعائیں",
            "پڑھنا", "پڑھیں", "kya", "ki", "ke", "ka", "mein", "se", "liye", "ko", "aur", "hai", "parhna", "parhen",
            // es
            "qué", "que", "leer", "duá", "súplica", "suplica", "para", "por", "mis", "cuando", "cuándo", "el",
            "la", "los", "las", "un", "una", "del", "al", "con", "se", "es", "estoy", "tengo", "cómo", "como",
            "hay", "decir", "rezar",
            // en
            "what", "to", "read", "recite", "say", "which", "dua", "duas", "supplication", "when", "if", "i",
            "me", "my", "for", "from", "at", "in", "on", "the", "a", "an", "and", "of", "is", "am", "are",
            "want", "should", "can", "do", "how", "allah", "have", "has", "lot", "lots", "very", "im", "it",
            "with", "about", "after", "before", "someone", "something",
        ).map(TextNormalizer::normalize).toSet() // same folding as the query tokens (Urdu ی ک ہ, Turkish ç ş ı)

        private class Concept(val triggers: List<String>, val terms: List<String>)

        /** Expands everyday words to the vocabulary used in Quran translations. */
        private val QURAN_CONCEPTS = listOf(
            Concept(listOf("деньги", "денег", "бедность", "богатство", "money", "poverty", "wealth", "rich", "poor"),
                listOf("богатств", "имуществ", "удел", "бедн", "wealth", "provision", "poor")),
            Concept(listOf("смерть", "умер", "умерла", "умерший", "death", "died", "dead"),
                listOf("смерт", "умира", "умерш", "death", "dies")),
            Concept(listOf("терпение", "терпеть", "patience", "patient"),
                listOf("терпе", "patien")),
            Concept(listOf("родители", "мама", "мать", "отец", "папа", "parents", "mother", "father"),
                listOf("родител", "parents")),
            Concept(listOf("страх", "боюсь", "страшно", "fear", "afraid", "scared"),
                listOf("страх", "боязн", "fear")),
            Concept(listOf("болезнь", "болею", "заболел", "illness", "sick", "ill"),
                listOf("болезн", "исцел", "больн", "ill", "heal")),
            Concept(listOf("прощение", "грехи", "грех", "простить", "forgive", "forgiveness", "sin", "sins"),
                listOf("прощ", "грех", "forgiv", "sins")),
            Concept(listOf("печаль", "грусть", "тревога", "sad", "sadness", "grief", "anxiety"),
                listOf("печал", "скорб", "grief", "sorrow")),
            Concept(listOf("долг", "долги", "debt", "debts", "loan"),
                listOf("долг", "debt")),
        )
    }
}
