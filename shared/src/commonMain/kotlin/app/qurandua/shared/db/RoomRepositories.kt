package app.qurandua.shared.db

import app.qurandua.shared.content.ContentParser
import app.qurandua.shared.content.SurahAsset
import app.qurandua.shared.data.QURAN_TRANSLATION_LANGUAGES
import app.qurandua.shared.data.QuranRepository
import app.qurandua.shared.data.UserDataRepository
import app.qurandua.shared.model.AppSettings
import app.qurandua.shared.model.Ayah
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.SurahInfo
import app.qurandua.shared.model.ThemeMode
import app.qurandua.shared.search.TextNormalizer
import app.qurandua.shared.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomQuranRepository(private val db: AppDatabase) : QuranRepository {
    private val dao = db.quranDao()
    private val importLock = Mutex()

    override suspend fun ensureImported(readAsset: suspend () -> String) = importLock.withLock {
        // Re-import when an app update ships a new translation language.
        if (dao.ayahCount() == TOTAL_AYAHS && dao.translationCount() == TOTAL_AYAHS * importedLanguages) return@withLock
        val asset = ContentParser.quran(readAsset())
        val surahs = ArrayList<SurahEntity>(114)
        val names = ArrayList<SurahNameEntity>(114 * 2)
        val ayahs = ArrayList<AyahEntity>(TOTAL_AYAHS)
        val translations = ArrayList<AyahTranslationEntity>(TOTAL_AYAHS * asset.langs.size)
        for (s in asset.surahs) {
            surahs += SurahEntity(s.n, s.ar, s.tr, s.type, s.ayahs.size)
            for ((lang, name) in s.names) names += SurahNameEntity(s.n, lang, name)
            s.ayahs.forEachIndexed { index, row ->
                val number = index + 1
                val arabic = row[SurahAsset.ARABIC]
                ayahs += AyahEntity(s.n, number, arabic, TextNormalizer.normalize(arabic), row[SurahAsset.TRANSLIT])
                for ((lang, column) in asset.translationColumns) {
                    val text = row[column]
                    translations += AyahTranslationEntity(s.n, number, lang, text, TextNormalizer.normalize(text))
                }
            }
        }
        dao.importAll(surahs, names, ayahs, translations)
    }

    override fun surahs(lang: String): Flow<List<SurahInfo>> =
        dao.surahs(lang).map { rows -> rows.map { it.toModel() } }

    override suspend fun surah(number: Int, lang: String): SurahInfo? = dao.surah(number, lang)?.toModel()

    override suspend fun ayahs(surah: Int, lang: String): List<Ayah> = dao.ayahs(surah, lang).map { it.toModel() }

    override suspend fun search(terms: List<String>, lang: String, limit: Int): List<Ayah> {
        if (terms.isEmpty()) return emptyList()
        // Ayahs that match more of the terms come first.
        val hits = LinkedHashMap<AyahKey, Pair<AyahRow, Int>>()
        for (term in terms) {
            for (row in dao.searchTerm(TextNormalizer.normalize(term), lang, limit * 2)) {
                val key = AyahKey(row.surah, row.ayah)
                val previous = hits[key]
                hits[key] = row to ((previous?.second ?: 0) + 1)
            }
        }
        return hits.values
            .sortedWith(compareByDescending<Pair<AyahRow, Int>> { it.second }.thenBy { it.first.surah }.thenBy { it.first.ayah })
            .take(limit)
            .map { it.first.toModel() }
    }

    private fun SurahRow.toModel() =
        SurahInfo(number, nameArabic, transliteration, name ?: transliteration, revelation, ayahCount)

    private fun AyahRow.toModel() = Ayah(surah, ayah, arabic, transliteration, translation)

    companion object {
        const val TOTAL_AYAHS = 6236
    }

    /** Must equal the number of translation languages bundled in quran.json. */
    private val importedLanguages = QURAN_TRANSLATION_LANGUAGES.size
}

class RoomUserDataRepository(db: AppDatabase) : UserDataRepository {
    private val dao = db.userDao()

    override val settings: Flow<AppSettings> = dao.settings().map { rows -> rows.toSettings() }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(dao.settingsOnce().toSettings())
        dao.putSettings(
            listOf(
                SettingEntity(KEY_LANGUAGE, updated.language),
                SettingEntity(KEY_TRANSLIT, updated.showTransliteration.toString()),
                SettingEntity(KEY_TRANSLATION, updated.showTranslation.toString()),
                SettingEntity(KEY_FONT, updated.arabicFontSize.toString()),
                SettingEntity(KEY_THEME, updated.theme.name),
                SettingEntity(KEY_TRANSLIT_SCRIPT, updated.translitScript),
            )
        )
    }

    override val favoriteIds: Flow<Set<String>> = dao.favoriteIds().map { it.toSet() }

    override suspend fun toggleFavorite(itemId: String) {
        if (dao.isFavorite(itemId)) dao.deleteFavorite(itemId)
        else dao.insertFavorite(FavoriteEntity(itemId, currentTimeMillis()))
    }

    override val bookmarks: Flow<List<AyahKey>> =
        dao.bookmarks().map { rows -> rows.map { AyahKey(it.surah, it.ayah) } }

    override suspend fun toggleBookmark(key: AyahKey) {
        if (dao.isBookmarked(key.surah, key.ayah)) dao.deleteBookmark(key.surah, key.ayah)
        else dao.insertBookmark(BookmarkEntity(key.surah, key.ayah, currentTimeMillis()))
    }

    override val lastRead: Flow<AyahKey?> = dao.settings().map { rows ->
        val value = rows.firstOrNull { it.key == KEY_LAST_READ }?.value ?: return@map null
        val (surah, ayah) = value.split(':').mapNotNull { it.toIntOrNull() }.takeIf { it.size == 2 } ?: return@map null
        AyahKey(surah, ayah)
    }

    override suspend fun setLastRead(key: AyahKey) {
        dao.putSettings(listOf(SettingEntity(KEY_LAST_READ, "${key.surah}:${key.ayah}")))
    }

    private fun List<SettingEntity>.toSettings(): AppSettings {
        val map = associate { it.key to it.value }
        val defaults = AppSettings()
        return AppSettings(
            language = map[KEY_LANGUAGE] ?: defaults.language,
            showTransliteration = map[KEY_TRANSLIT]?.toBooleanStrictOrNull() ?: defaults.showTransliteration,
            showTranslation = map[KEY_TRANSLATION]?.toBooleanStrictOrNull() ?: defaults.showTranslation,
            arabicFontSize = map[KEY_FONT]?.toIntOrNull() ?: defaults.arabicFontSize,
            theme = map[KEY_THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: defaults.theme,
            translitScript = map[KEY_TRANSLIT_SCRIPT] ?: defaults.translitScript,
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_TRANSLIT = "show_translit"
        const val KEY_TRANSLATION = "show_translation"
        const val KEY_FONT = "arabic_font_size"
        const val KEY_THEME = "theme"
        const val KEY_TRANSLIT_SCRIPT = "translit_script"
        const val KEY_LAST_READ = "last_read"
    }
}
