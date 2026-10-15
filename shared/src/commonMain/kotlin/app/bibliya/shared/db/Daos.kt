package app.bibliya.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BibleDao {

    @Query("SELECT COUNT(*) FROM verse WHERE lang = :lang")
    abstract suspend fun verseCount(lang: String): Int

    @Query("SELECT * FROM verse WHERE lang = :lang AND book = :book AND chapter = :chapter ORDER BY verse")
    abstract suspend fun chapter(lang: String, book: String, chapter: Int): List<VerseEntity>

    @Query("SELECT * FROM verse WHERE lang = :lang AND book = :book AND chapter = :chapter AND verse = :verse")
    abstract suspend fun verse(lang: String, book: String, chapter: Int, verse: Int): VerseEntity?

    @Query(
        """
        SELECT * FROM verse
        WHERE lang = :lang AND textNorm LIKE '%' || :term || '%'
        ORDER BY bookOrder, chapter, verse
        LIMIT :limit
        """
    )
    abstract suspend fun search(lang: String, term: String, limit: Int): List<VerseEntity>

    @Query("DELETE FROM verse WHERE lang = :lang")
    abstract suspend fun deleteLang(lang: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVerses(items: List<VerseEntity>)

    @Transaction
    open suspend fun replaceLang(lang: String, verses: List<VerseEntity>) {
        deleteLang(lang)
        verses.chunked(2000).forEach { insertVerses(it) }
    }
}

@Dao
interface UserDao {

    @Query("SELECT passageId FROM favorite ORDER BY createdAt DESC")
    fun favoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE passageId = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE passageId = :id")
    suspend fun deleteFavorite(id: String)

    @Query(
        """
        SELECT b.lang, b.book, b.chapter, b.verse, v.text
        FROM bookmark b LEFT JOIN verse v
            ON v.lang = b.lang AND v.book = b.book AND v.chapter = b.chapter AND v.verse = b.verse
        ORDER BY b.createdAt DESC
        """
    )
    fun bookmarks(): Flow<List<BookmarkRow>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmark WHERE lang = :lang AND book = :book AND chapter = :chapter AND verse = :verse)")
    suspend fun isBookmarked(lang: String, book: String, chapter: Int, verse: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmark WHERE lang = :lang AND book = :book AND chapter = :chapter AND verse = :verse")
    suspend fun deleteBookmark(lang: String, book: String, chapter: Int, verse: Int)

    @Query("SELECT day FROM plan_day")
    fun planDays(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanDay(item: PlanDayEntity)

    @Query("DELETE FROM plan_day WHERE day = :day")
    suspend fun deletePlanDay(day: Int)

    @Query("DELETE FROM plan_day")
    suspend fun clearPlan()

    @Query("SELECT * FROM setting")
    fun settings(): Flow<List<SettingEntity>>

    @Query("SELECT * FROM setting")
    suspend fun settingsOnce(): List<SettingEntity>

    @Query("SELECT value FROM setting WHERE `key` = :key")
    suspend fun setting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSettings(items: List<SettingEntity>)

    @Query("DELETE FROM setting WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}
