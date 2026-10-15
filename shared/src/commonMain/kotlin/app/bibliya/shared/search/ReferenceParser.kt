package app.bibliya.shared.search

import app.bibliya.shared.model.Book
import app.bibliya.shared.model.VerseRef

/**
 * Understands typed references: "Ин 3:16", "иоанна 3 16", "1 Кор 13", "Пс 22", "John 3:16", "1jn 4".
 * Returns null when the text does not look like a reference to a known book.
 */
class ReferenceParser(books: List<Book>) {

    /** Normalized spelling (no spaces, no dots) to book id. */
    private val names: Map<String, String> = buildMap {
        for (b in books) {
            val spellings = b.names.values + b.short.values + b.aliases + b.id
            for (s in spellings) put(key(s), b.id)
        }
    }

    fun parse(query: String): VerseRef? {
        val m = PATTERN.matchEntire(query.trim().lowercase()) ?: return null
        val prefix = m.groupValues[1]
        val word = m.groupValues[2]
        val book = names[key(prefix + word)] ?: names[key(word)].takeIf { prefix.isEmpty() } ?: return null
        val chapter = m.groupValues[3].toIntOrNull() ?: return null
        val verse = m.groupValues[4].toIntOrNull() ?: 1
        val verseEnd = m.groupValues[5].toIntOrNull()?.coerceAtLeast(verse) ?: verse
        if (chapter < 1) return null
        return VerseRef(book, chapter, verse.coerceAtLeast(1), verseEnd)
    }

    private fun key(s: String) = s.lowercase().replace('ё', 'е').filter { it.isLetterOrDigit() }

    private companion object {
        /** Optional book number, the book word, chapter, optional verse and verse range. */
        val PATTERN = Regex("""^([1-4]?)\s*([\p{L}][\p{L}.\s]*?)\.?\s*(\d{1,3})(?:\s*[:,.\s]\s*(\d{1,3})(?:\s*[-–—]\s*(\d{1,3}))?)?$""")
    }
}
