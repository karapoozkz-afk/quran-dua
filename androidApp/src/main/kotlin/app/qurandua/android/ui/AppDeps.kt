package app.qurandua.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import app.qurandua.shared.data.ContentRepository
import app.qurandua.shared.data.QuranRepository
import app.qurandua.shared.data.UserDataRepository
import app.qurandua.shared.i18n.EnStrings
import app.qurandua.shared.i18n.Strings
import app.qurandua.shared.model.AppSettings
import app.qurandua.shared.prayer.AdhanSound
import app.qurandua.shared.prayer.Place
import app.qurandua.shared.prayer.PrayerSettings
import kotlinx.coroutines.flow.StateFlow

/** Things only the platform can do. Keeps screens free of Android APIs. */
interface PlatformActions {
    fun share(text: String)
    fun copy(text: String)
    fun openUrl(url: String)
}

/** Streams recitation audio. [playing] holds the key of what is playing now. */
interface AudioController {
    val playing: StateFlow<String?>
    /** Index of the track now playing within the list passed to [play]. */
    val index: StateFlow<Int>
    /** Whether the phone has an Arabic text-to-speech voice; null while that is still unknown. */
    val arabicVoice: StateFlow<Boolean?>
    fun play(key: String, urls: List<String>)
    /** Reads Arabic text with the phone's own voice, for duas that have no recording. */
    fun speak(key: String, arabic: String)
    fun openVoiceSettings()
    fun stop()
}

/** A download in progress: [surah] is the one being fetched now; counts are in ayahs. */
data class DownloadProgress(val surah: Int, val all: Boolean, val done: Int, val total: Int, val failed: Int)

/** Recitations saved on the phone for good, so nothing is downloaded twice. */
interface RecitationLibrary {
    val savedBytes: StateFlow<Long>
    val savedSurahs: StateFlow<Set<Int>>
    val download: StateFlow<DownloadProgress?>
    /** Recounts what is saved; pass the ayah count of every surah once it is known. */
    fun refresh(ayahCounts: List<Int>)
    fun downloadSurah(surah: Int, ayahCount: Int)
    fun downloadAll(ayahCounts: List<Int>)
    fun cancel()
    fun deleteAll()
}

/** Prayer-time settings, alarms and location. Implemented with Android services in platform/. */
interface PrayerController {
    val settings: StateFlow<PrayerSettings>
    fun update(transform: (PrayerSettings) -> PrayerSettings)
    /** Language of notification texts; follows the app's UI language. */
    fun setLanguage(language: String)
    /** The device's approximate location, or null when unavailable or not permitted. */
    suspend fun currentLocation(): Place?
    /** True while an adhan plays, from a prayer alarm or from [preview]. */
    val adhanPlaying: StateFlow<Boolean>
    fun preview(sound: AdhanSound)
    fun stopAdhan()
    /** Android 14+ lets the user withhold full-screen notifications; false when withheld. */
    fun canUseFullScreen(): Boolean
    fun openFullScreenSettings()
    /** Android 12+ lets the user withhold exact alarms; without them reminders can be late. */
    fun canScheduleExact(): Boolean
    fun openExactAlarmSettings()
}

/** Lets screens ask for a runtime permission; MainActivity plugs in the real launcher. */
class PermissionRequester {
    var launcher: ((permission: String, onResult: (Boolean) -> Unit) -> Unit)? = null

    fun request(permission: String, onResult: (Boolean) -> Unit) {
        launcher?.invoke(permission, onResult) ?: onResult(false)
    }
}

class AppDeps(
    val content: ContentRepository,
    val quran: QuranRepository,
    val user: UserDataRepository,
    val platform: PlatformActions,
    val audio: AudioController,
    val recitations: RecitationLibrary,
    val prayer: PrayerController,
    val permissions: PermissionRequester,
    val arabicFont: FontFamily,
    /** Reads the bundled Quran asset for the first-launch import. */
    val readQuranAsset: suspend () -> String,
)

val LocalDeps = staticCompositionLocalOf<AppDeps> { error("AppDeps not provided") }
val LocalStrings = staticCompositionLocalOf<Strings> { EnStrings }
val LocalSettings = staticCompositionLocalOf { AppSettings() }
/** Language translations are shown in (ru, en; others fall back to en). */
val LocalContentLang = staticCompositionLocalOf { "en" }

/** Recitation by Mishary Rashid Alafasy, one file per ayah. */
fun ayahAudioUrl(surah: Int, ayah: Int): String {
    fun pad(n: Int) = n.toString().padStart(3, '0')
    return "https://everyayah.com/data/Alafasy_128kbps/${pad(surah)}${pad(ayah)}.mp3"
}
