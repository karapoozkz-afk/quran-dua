package app.bibliya.android.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import app.bibliya.android.ui.PlatformActions
import app.bibliya.android.ui.SpeechController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AndroidPlatformActions(private val context: Context) : PlatformActions {

    override fun share(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun copy(text: String, confirmation: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
        // Android 13+ shows its own copy confirmation.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, confirmation, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    override fun email(address: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

/**
 * Reads a chapter aloud with the phone's own text-to-speech voice. No recordings are bundled or
 * downloaded, so the app needs no internet; the voice is whatever the phone has installed.
 */
class AndroidSpeechController(context: Context) : SpeechController {
    private val appContext = context.applicationContext
    private val _speaking = MutableStateFlow<String?>(null)
    override val speaking: StateFlow<String?> = _speaking.asStateFlow()
    private val _verse = MutableStateFlow<Int?>(null)
    override val verse: StateFlow<Int?> = _verse.asStateFlow()
    private val _missingVoice = MutableStateFlow(false)
    override val missingVoice: StateFlow<Boolean> = _missingVoice.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Triple<String, String, List<Pair<Int, String>>>? = null

    override fun speak(key: String, lang: String, verses: List<Pair<Int, String>>) {
        stop()
        _speaking.value = key
        _missingVoice.value = false
        val engine = tts
        if (engine == null) {
            pending = Triple(key, lang, verses)
            tts = TextToSpeech(appContext) { status -> onInit(status) }
        } else if (ready) {
            queue(engine, key, lang, verses)
        } else {
            pending = Triple(key, lang, verses)
        }
    }

    private fun onInit(status: Int) {
        val engine = tts ?: return
        ready = status == TextToSpeech.SUCCESS
        if (!ready) {
            _missingVoice.value = true
            return
        }
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _verse.value = utteranceId?.removeSuffix(LAST)?.substringAfterLast('#')?.toIntOrNull()
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId?.endsWith(LAST) == true) finish()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish()
        })
        pending?.let { (key, lang, verses) -> queue(engine, key, lang, verses) }
        pending = null
    }

    private fun queue(engine: TextToSpeech, key: String, lang: String, verses: List<Pair<Int, String>>) {
        val result = engine.setLanguage(Locale(lang))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _missingVoice.value = true
            return
        }
        verses.forEachIndexed { i, (number, text) ->
            val id = if (i == verses.lastIndex) "$key#$number$LAST" else "$key#$number"
            engine.speak(text, TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    private fun finish() {
        _speaking.value = null
        _verse.value = null
    }

    override fun stop() {
        tts?.stop()
        pending = null
        finish()
    }

    override fun openVoiceSettings() {
        val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(intent) }
            .onFailure { appContext.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun release() {
        tts?.shutdown()
        tts = null
        ready = false
    }

    private companion object {
        /** Marks the last verse's utterance; the "#n" before it is the verse number. */
        const val LAST = "#end"
    }
}
