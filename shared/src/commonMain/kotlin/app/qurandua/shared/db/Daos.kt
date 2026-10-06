package app.qurandua.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuranDao {

    @Query("SELECT COUNT(*) FROM ayah")
    abstract suspend fun ayahCount(): Int

    @Query("SELECT COUNT(*) FROM ayah_translation")
    abstract suspend fun translationCount(): Int

    @Query(
        """
        SELECT s.number, s.nameArabic, s.transliteration, s.revelation, s.ayahCount, n.name
        FROM surah s LEFT JOIN surah_name n ON n.number = s.number AND n.lang = :lang
        ORDER BY s.number
        """
    )
    abstract fun surahs(lang: String): Flow<List<SurahRow>>

    @Query(
        """
        SELECT s.number, s.nameArabic, s.transliteration, s.revelation, s.ayahCount, n.name
        FROM surah s LEFT JOIN surah_name n ON n.number = s.number AND n.lang = :lang
        WHERE s.number = :number
        """
    )
    abstract suspend fun surah(number: Int, lang: String): SurahRow?

    @Query(
        """
        SELECT a.surah, a.ayah, a.arabic, a.transliteration, t.text AS translation
        FROM ayah a LEFT JOIN ayah_translation t
            ON t.surah = a.surah AND t.ayah = a.ayah AND t.lang = :lang
        WHERE a.surah = :surah
        ORDER BY a.ayah
        """
    )
    abstract suspend fun ayahs(surah: Int, lang: String): List<AyahRow>

    @Query(
        """
        SELECT a.surah, a.ayah, a.arabic, a.transliteration, t.text AS translation
        FROM ayah a JOIN ayah_translation t
            ON t.surah = a.surah AND t.ayah = a.ayah AND t.lang = :lang
        WHERE t.textNorm LIKE '%' || :term || '%' OR a.arabicPlain LIKE '%' || :term || '%'
        ORDER BY a.surah, a.ayah
        LIMIT :limit
        """
    )
    abstract suspend fun searchTerm(term: String, lang: String, limit: Int): List<AyahRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSurahs(items: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSurahNames(items: List<SurahNameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAyahs(items: List<AyahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTranslations(items: List<AyahTranslationEntity>)

    @Transaction
    open suspend fun importAll(
        surahs: List<SurahEntity>,
        names: List<SurahNameEntity>,
        ayahs: List<AyahEntity>,
        translations: List<AyahTranslationEntity>,
    ) {
        insertSurahs(surahs)
        insertSurahNames(names)
        insertAyahs(ayahs)
        insertTranslations(translations)
    }
}

@Dao
interface UserDao {

    @Query("SELECT itemId FROM favorite ORDER BY createdAt DESC")
    fun favoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE itemId = :itemId)")
    suspend fun isFavorite(itemId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE itemId = :itemId")
    suspend fun deleteFavorite(itemId: String)

    @Query("SELECT * FROM bookmark ORDER BY createdAt DESC")
    fun bookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmark WHERE surah = :surah AND ayah = :ayah)")
    suspend fun isBookmarked(surah: Int, ayah: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmark WHERE surah = :surah AND ayah = :ayah")
    suspend fun deleteBookmark(surah: Int, ayah: Int)

    @Query("SELECT * FROM setting")
    fun settings(): Flow<List<SettingEntity>>

    @Query("SELECT * FROM setting")
    suspend fun settingsOnce(): List<SettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSettings(items: List<SettingEntity>)
}
