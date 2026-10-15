package app.bibliya.shared.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "verse",
    primaryKeys = ["lang", "book", "chapter", "verse"],
    indices = [Index("lang", "bookOrder", "chapter", "verse")],
)
data class VerseEntity(
    val lang: String,
    val book: String,
    /** Position of the book in the translation, so search results come in Bible order. */
    val bookOrder: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    /** Lower-cased text without punctuation, for case-insensitive search of Cyrillic. */
    val textNorm: String,
)

@Entity(tableName = "favorite")
data class FavoriteEntity(
    @androidx.room.PrimaryKey val passageId: String,
    val createdAt: Long,
)

@Entity(tableName = "bookmark", primaryKeys = ["lang", "book", "chapter", "verse"])
data class BookmarkEntity(
    val lang: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val createdAt: Long,
)

/** A bookmark with its verse text, for the "Saved" tab. */
data class BookmarkRow(
    val lang: String,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String?,
)

@Entity(tableName = "plan_day")
data class PlanDayEntity(
    @androidx.room.PrimaryKey val day: Int,
    val doneAt: Long,
)

@Entity(tableName = "setting")
data class SettingEntity(
    @androidx.room.PrimaryKey val key: String,
    val value: String,
)
