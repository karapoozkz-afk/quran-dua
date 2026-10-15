package app.bibliya.shared.db

import app.bibliya.shared.content.ContentParser
import app.bibliya.shared.data.BibleRepository
import app.bibliya.shared.data.UserDataRepository
import app.bibliya.shared.model.AppSettings
import app.bibliya.shared.model.Book
import app.bibliya.shared.model.Bookmark
import app.bibliya.shared.model.ReadingPosition
import app.bibliya.shared.model.ThemeMode
import app.bibliya.shared.model.Verse
import app.bibliya.shared.search.TextNormalizer
import app.bibliya.shared.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

class RoomBibleRepository(db: AppDatabase, private val books: List<Book>) : BibleRepository {
    private val dao = db.bibleDao()
    private val userDao = db.userDao()
    private val importLock = Mutex()

    override suspend fun ensureImported(lang: String, readAsset: suspend (String) -> String) = importLock.withLock {
        val marker = "imported_$lang"
        // Re-import when an app update ships a corrected text (CONTENT_VERSION goes up).
        if (userDao.setting(marker) == CONTENT_VERSION && dao.verseCount(lang) > 0) return@withLock
        val asset = ContentParser.bible(readAsset(lang))
        val order = books.associate { it.id to (if (lang == "en") it.orderEn ?: it.order else it.order) }
        val verses = ArrayList<VerseEntity>(40_000)
        for ((book, chapters) in asset.books) {
            val bookOrder = order[book] ?: continue
            chapters.forEachIndexed { c, texts ->
                texts.forEachIndexed { v, text ->
                    // An empty verse is printed joined to the one before it (Synodal Psalm 114:9).
                    if (text.isNotBlank()) {
                        verses += VerseEntity(lang, book, bookOrder, c + 1, v + 1, text, TextNormalizer.normalize(text))
                    }
                }
            }
        }
        dao.replaceLang(lang, verses)
        userDao.putSettings(listOf(SettingEntity(marker, CONTENT_VERSION)))
    }

    override suspend fun chapter(lang: String, book: String, chapter: Int): List<Verse> =
        dao.chapter(lang, book, chapter).map { it.toModel() }

    override suspend fun verse(lang: String, book: String, chapter: Int, verse: Int): Verse? =
        dao.verse(lang, book, chapter, verse)?.toModel()

    override suspend fun search(lang: String, terms: List<String>, limit: Int): List<Verse> {
        val normalized = terms.map(TextNormalizer::normalize).filter { it.isNotBlank() }.distinct()
        if (normalized.isEmpty()) return emptyList()
        // The longest term is usually the rarest: let the database narrow by it, then check the rest here.
        val first = normalized.maxBy { it.length }
        val rest = normalized - first
        return dao.search(lang, first, SCAN_LIMIT)
            .filter { row -> rest.all { row.textNorm.contains(it) } }
            .take(limit)
            .map { it.toModel() }
    }

    private fun VerseEntity.toModel() = Verse(lang, book, chapter, verse, text)

    private companion object {
        /** Bump when the bundled Bible text changes, so phones re-import it. */
        const val CONTENT_VERSION = "1"
        const val SCAN_LIMIT = 3000
    }
}

class RoomUserDataRepository(db: AppDatabase) : UserDataRepository {
    private val dao = db.userDao()

    override val settings: Flow<AppSettings> = dao.settings().map { rows -> rows.toSettings() }

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(dao.settingsOnce().toSettings())
        dao.putSettings(
            listOf(
                SettingEntity(KEY_LANGUAGE, updated.language),
                SettingEntity(KEY_TRANSLATION, updated.translation),
                SettingEntity(KEY_FONT, updated.fontSize.toString()),
                SettingEntity(KEY_THEME, updated.theme.name),
                SettingEntity(KEY_DEUTEROCANON, updated.showDeuterocanon.toString()),
                SettingEntity(KEY_REMINDER, updated.dailyReminder.toString()),
                SettingEntity(KEY_REMINDER_MINUTE, updated.reminderMinute.toString()),
                SettingEntity(KEY_ASKED_NOTIFICATIONS, updated.askedNotifications.toString()),
                SettingEntity(KEY_TRADITION, updated.tradition.name),
            )
        )
    }

    override val favoritePassages: Flow<Set<String>> = dao.favoriteIds().map { it.toSet() }

    override suspend fun toggleFavorite(passageId: String) {
        if (dao.isFavorite(passageId)) dao.deleteFavorite(passageId)
        else dao.insertFavorite(FavoriteEntity(passageId, currentTimeMillis()))
    }

    override val bookmarks: Flow<List<Bookmark>> =
        dao.bookmarks().map { rows -> rows.map { Bookmark(it.lang, it.book, it.chapter, it.verse, it.text) } }

    override suspend fun toggleBookmark(lang: String, book: String, chapter: Int, verse: Int) {
        if (dao.isBookmarked(lang, book, chapter, verse)) dao.deleteBookmark(lang, book, chapter, verse)
        else dao.insertBookmark(BookmarkEntity(lang, book, chapter, verse, currentTimeMillis()))
    }

    override val lastRead: Flow<ReadingPosition?> = dao.settings().map { rows ->
        val value = rows.firstOrNull { it.key == KEY_LAST_READ }?.value ?: return@map null
        val parts = value.split('|')
        if (parts.size != 4) return@map null
        val chapter = parts[2].toIntOrNull() ?: return@map null
        val verse = parts[3].toIntOrNull() ?: return@map null
        ReadingPosition(parts[0], parts[1], chapter, verse)
    }

    override suspend fun setLastRead(position: ReadingPosition) {
        dao.putSettings(listOf(SettingEntity(KEY_LAST_READ, with(position) { "$lang|$book|$chapter|$verse" })))
    }

    override val planDone: Flow<Set<Int>> = dao.planDays().map { it.toSet() }

    override suspend fun setPlanDay(day: Int, done: Boolean) {
        if (done) dao.insertPlanDay(PlanDayEntity(day, currentTimeMillis())) else dao.deletePlanDay(day)
    }

    override val planStart: Flow<LocalDate?> = dao.settings().map { rows ->
        rows.firstOrNull { it.key == KEY_PLAN_START }?.value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }

    override suspend fun setPlanStart(date: LocalDate?) {
        if (date == null) {
            dao.deleteSetting(KEY_PLAN_START)
            dao.clearPlan()
        } else {
            dao.putSettings(listOf(SettingEntity(KEY_PLAN_START, date.toString())))
        }
    }

    private fun List<SettingEntity>.toSettings(): AppSettings {
        val map = associate { it.key to it.value }
        val defaults = AppSettings()
        return AppSettings(
            language = map[KEY_LANGUAGE] ?: defaults.language,
            translation = map[KEY_TRANSLATION] ?: defaults.translation,
            fontSize = map[KEY_FONT]?.toIntOrNull() ?: defaults.fontSize,
            theme = map[KEY_THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: defaults.theme,
            showDeuterocanon = map[KEY_DEUTEROCANON]?.toBooleanStrictOrNull() ?: defaults.showDeuterocanon,
            dailyReminder = map[KEY_REMINDER]?.toBooleanStrictOrNull() ?: defaults.dailyReminder,
            reminderMinute = map[KEY_REMINDER_MINUTE]?.toIntOrNull() ?: defaults.reminderMinute,
            askedNotifications = map[KEY_ASKED_NOTIFICATIONS]?.toBooleanStrictOrNull() ?: defaults.askedNotifications,
            tradition = map[KEY_TRADITION]?.let { name -> Tradition.entries.firstOrNull { it.name == name } } ?: defaults.tradition,
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_TRANSLATION = "translation"
        const val KEY_FONT = "font_size"
        const val KEY_THEME = "theme"
        const val KEY_DEUTEROCANON = "show_deuterocanon"
        const val KEY_REMINDER = "daily_reminder"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_ASKED_NOTIFICATIONS = "asked_notifications"
        const val KEY_TRADITION = "tradition"
        const val KEY_LAST_READ = "last_read"
        const val KEY_PLAN_START = "plan_start"
    }
}
