package app.qurandua.shared.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val file = appContext.getDatabasePath(AppDatabase.FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(context = appContext, name = file.absolutePath)
}
