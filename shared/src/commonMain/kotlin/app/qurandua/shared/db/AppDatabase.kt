package app.qurandua.shared.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        SurahEntity::class,
        SurahNameEntity::class,
        AyahEntity::class,
        AyahTranslationEntity::class,
        FavoriteEntity::class,
        BookmarkEntity::class,
        SettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun userDao(): UserDao

    companion object {
        const val FILE_NAME = "quran_dua.db"
    }
}

// Room's KSP processor generates the actual implementations for each platform.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Platform code supplies the builder (file location); configuration is shared. */
fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
