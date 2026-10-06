package app.qurandua.shared.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val transliteration: String,
    val revelation: String,
    val ayahCount: Int,
)

/** Surah names per language, so new languages are rows, not columns. */
@Entity(tableName = "surah_name", primaryKeys = ["number", "lang"])
data class SurahNameEntity(
    val number: Int,
    val lang: String,
    val name: String,
)

@Entity(tableName = "ayah", primaryKeys = ["surah", "ayah"])
data class AyahEntity(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    /** Arabic without diacritics, for search. */
    val arabicPlain: String,
    val transliteration: String,
)

@Entity(
    tableName = "ayah_translation",
    primaryKeys = ["surah", "ayah", "lang"],
    indices = [Index("lang")],
)
data class AyahTranslationEntity(
    val surah: Int,
    val ayah: Int,
    val lang: String,
    val text: String,
    /** Lower-cased text for case-insensitive search of non-Latin scripts. */
    val textNorm: String,
)

/** Result row of the reader and search queries. */
data class AyahRow(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val transliteration: String,
    @ColumnInfo(name = "translation") val translation: String?,
)

data class SurahRow(
    val number: Int,
    val nameArabic: String,
    val transliteration: String,
    val revelation: String,
    val ayahCount: Int,
    @ColumnInfo(name = "name") val name: String?,
)

@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val itemId: String,
    val createdAt: Long,
)

@Entity(tableName = "bookmark", primaryKeys = ["surah", "ayah"])
data class BookmarkEntity(
    val surah: Int,
    val ayah: Int,
    val createdAt: Long,
)

@Entity(tableName = "setting")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)
