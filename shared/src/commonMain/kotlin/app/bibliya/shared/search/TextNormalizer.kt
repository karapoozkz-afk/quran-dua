package app.bibliya.shared.search

/** Normalizes text so that search ignores case, "ё", punctuation and typographic apostrophes. */
object TextNormalizer {

    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        for (raw in text) {
            val c = raw.lowercaseChar()
            when {
                c in APOSTROPHES -> Unit
                c == 'ё' -> sb.append('е')
                c.isLetterOrDigit() -> sb.append(c)
                else -> sb.append(' ')
            }
        }
        return sb.toString().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
    }

    fun tokens(text: String): List<String> =
        normalize(text).split(' ').filter { it.isNotEmpty() }

    private const val APOSTROPHES = "'‘’ʻʼ`´"

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
