package app.bibliya.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import app.bibliya.shared.data.BibleRepository
import app.bibliya.shared.data.ContentRepository
import app.bibliya.shared.data.UserDataRepository
import app.bibliya.shared.i18n.RuStrings
import app.bibliya.shared.i18n.Strings
import app.bibliya.shared.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Things only the platform can do. Keeps screens free of Android APIs. */
interface PlatformActions {
    fun share(text: String)
    fun copy(text: String, confirmation: String)
    fun openUrl(url: String)
    fun email(address: String, subject: String)
}

/** Reads text aloud with the phone's voice. [speaking] holds the key of what is being read. */
interface SpeechController {
    val speaking: StateFlow<String?>
    /** The verse being read right now. */
    val verse: StateFlow<Int?>
    /** True when the phone has no voice for the language. */
    val missingVoice: StateFlow<Boolean>
    fun speak(key: String, lang: String, verses: List<Pair<Int, String>>)
    fun stop()
    fun openVoiceSettings()
}

/** Lets screens ask for a runtime permission; MainActivity plugs in the real launcher. */
class PermissionRequester {
    var launcher: ((permission: String, onResult: (Boolean) -> Unit) -> Unit)? = null
    var isGranted: ((permission: String) -> Boolean)? = null

    fun request(permission: String, onResult: (Boolean) -> Unit) {
        launcher?.invoke(permission, onResult) ?: onResult(false)
    }
}

class AppDeps(
    val content: ContentRepository,
    val bible: BibleRepository,
    val user: UserDataRepository,
    val platform: PlatformActions,
    val speech: SpeechController,
    val permissions: PermissionRequester,
    /** Reads bundled bible_<lang>.json for the first-launch import. */
    val readBibleAsset: suspend (String) -> String,
    val versionName: String,
    /** Set when the app was opened from the verse-of-the-day notification. */
    val openVerseRequested: MutableStateFlow<Boolean> = MutableStateFlow(false),
)

val LocalDeps = staticCompositionLocalOf<AppDeps> { error("AppDeps not provided") }
val LocalStrings = staticCompositionLocalOf<Strings> { RuStrings }
val LocalSettings = staticCompositionLocalOf { AppSettings() }
/** The interface language actually shown (the chosen one, else the phone's). */
val LocalUiLanguage = staticCompositionLocalOf { "ru" }
/** The Bible translation shown in the reader and in passages ("ru" Synodal, "en" KJV). */
val LocalTranslation = staticCompositionLocalOf { "ru" }
