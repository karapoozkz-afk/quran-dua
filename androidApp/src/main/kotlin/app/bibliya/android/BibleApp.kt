package app.bibliya.android

import android.app.Application
import app.bibliya.android.platform.AndroidPlatformActions
import app.bibliya.android.platform.AndroidSpeechController
import app.bibliya.android.platform.Reminder
import app.bibliya.android.ui.AppDeps
import app.bibliya.android.ui.PermissionRequester
import app.bibliya.shared.data.ContentRepository
import app.bibliya.shared.db.RoomBibleRepository
import app.bibliya.shared.db.RoomUserDataRepository
import app.bibliya.shared.db.androidDatabaseBuilder
import app.bibliya.shared.db.buildAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleApp : Application() {

    lateinit var deps: AppDeps
        private set

    private lateinit var speech: AndroidSpeechController

    override fun onCreate() {
        super.onCreate()
        val db = buildAppDatabase(androidDatabaseBuilder(this))
        speech = AndroidSpeechController(this)
        Reminder.createChannel(this)
        Reminder.schedule(this)
        val content = ContentRepository.fromJson(
            readAsset("content/books.json"),
            readAsset("content/situations.json"),
            readAsset("content/passages.json"),
            readAsset("content/votd.json"),
            readAsset("content/plan.json"),
        )
        deps = AppDeps(
            content = content,
            bible = RoomBibleRepository(db, content.books),
            user = RoomUserDataRepository(db),
            platform = AndroidPlatformActions(this),
            speech = speech,
            permissions = PermissionRequester(),
            readBibleAsset = { lang -> withContext(Dispatchers.IO) { readAsset("content/bible_$lang.json") } },
            versionName = BuildConfig.VERSION_NAME,
        )
    }

    override fun onTerminate() {
        speech.release()
        super.onTerminate()
    }

    private fun readAsset(name: String): String = assets.open(name).bufferedReader().use { it.readText() }
}
