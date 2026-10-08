package app.qurandua.shared.search

/** Normalizes text so that search ignores case, diacritics and letter variants. */
object TextNormalizer {

    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        for (raw in text) {
            val c = raw.lowercaseChar()
            when {
                isArabicDiacritic(c) -> Unit
                // Uzbek o‘ g‘ and the Arabic ‘ayn are typed with any of these marks; drop them so "ko'p" meets "ko‘p".
                c in APOSTROPHES -> Unit
                c == 'ё' -> sb.append('е')
                c == 'ٱ' || c == 'أ' || c == 'إ' || c == 'آ' -> sb.append('ا')
                c == 'ى' -> sb.append('ي')
                c == 'ة' -> sb.append('ه')
                c == 'ؤ' -> sb.append('و')
                c == 'ئ' -> sb.append('ي')
                // Urdu and Persian keyboards type these letters for the Arabic ي ك ه.
                c == 'ی' || c == 'ې' -> sb.append('ي')
                c == 'ک' -> sb.append('ك')
                c == 'ہ' || c == 'ۂ' || c == 'ە' -> sb.append('ه')
                c in LATIN_FOLD -> sb.append(LATIN_FOLD.getValue(c))
                c.isLetterOrDigit() -> sb.append(c)
                else -> sb.append(' ')
            }
        }
        return sb.toString().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
    }

    fun tokens(text: String): List<String> =
        normalize(text).split(' ').filter { it.isNotEmpty() }

    /**
     * People often type Turkish, Indonesian or transliterated words without diacritics
     * ("borc" for "borç", "sabir" for "sabır"), so both sides drop them.
     */
    private val LATIN_FOLD: Map<Char, Char> = buildMap {
        "çc şs ğg ıi öo üu âa îi ûu āa īi ūu ṣs ḥh ṭt ḍd ẓz".split(' ').forEach { put(it[0], it[1]) }
    }

    private const val APOSTROPHES = "'‘’ʻʼ`´"

    /** Harakat, Quranic annotation marks, superscript alef and tatweel. */
    private fun isArabicDiacritic(c: Char): Boolean {
        val code = c.code
        return code in 0x064B..0x065F ||
            code == 0x0670 ||
            code in 0x06D6..0x06ED ||
            code == 0x0640
    }

    /**
     * A crude language-agnostic stem: short words stay whole, longer words are cut
     * so that "долгов" and "долги", "болезнь" and "болезни" meet.
     */
    fun stem(token: String): String = when {
        token.length <= 4 -> token
        token.length <= 6 -> token.substring(0, token.length - 1)
        else -> token.substring(0, 6)
    }
}
