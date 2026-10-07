package app.qurandua.android

import android.app.Application
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import app.qurandua.android.platform.AndroidPlatformActions
import app.qurandua.android.platform.Adhan
import app.qurandua.android.platform.AndroidPrayerController
import app.qurandua.android.platform.ExoAudioController
import app.qurandua.android.platform.RecitationStore
import app.qurandua.android.platform.PrayerAlarms
import app.qurandua.android.ui.AppDeps
import app.qurandua.android.ui.PermissionRequester
import app.qurandua.shared.data.ContentRepository
import app.qurandua.shared.db.AppDatabase
import app.qurandua.shared.db.RoomQuranRepository
import app.qurandua.shared.db.RoomUserDataRepository
import app.qurandua.shared.db.androidDatabaseBuilder
import app.qurandua.shared.db.buildAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranDuaApp : Application() {

    lateinit var deps: AppDeps
        private set

    private lateinit var audio: ExoAudioController

    override fun onCreate() {
        super.onCreate()
        val db = buildAppDatabase(androidDatabaseBuilder(this))
        audio = ExoAudioController(this)
        PrayerAlarms.createChannel(this)
        Adhan.createChannel(this)
        PrayerAlarms.schedule(this)
        deps = AppDeps(
            content = ContentRepository.fromJson(
                readAsset("content/situations.json"),
                readAsset("content/duas.json"),
                readAsset("content/charities.json"),
            ),
            quran = RoomQuranRepository(db),
            user = RoomUserDataRepository(db),
            platform = AndroidPlatformActions(this),
            audio = audio,
            recitations = RecitationStore,
            prayer = AndroidPrayerController(this),
            permissions = PermissionRequester(),
            arabicFont = FontFamily(Font(R.font.amiri_quran)),
            readQuranAsset = { withContext(Dispatchers.IO) { readAsset("content/quran.json") } },
        )
    }

    override fun onTerminate() {
        audio.release()
        super.onTerminate()
    }

    private fun readAsset(name: String): String = assets.open(name).bufferedReader().use { it.readText() }
}
