package app.qurandua.android.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import app.qurandua.android.ui.AudioController
import app.qurandua.android.ui.PlatformActions
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

    override fun copy(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
        // Android 13+ shows its own copy confirmation.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, android.R.string.copy, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

/**
 * Plays ayah recitations with ExoPlayer. Audio is not bundled, so the app stays small;
 * what was played once is kept in [RecitationStore] for good and replays offline.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class ExoAudioController(context: Context) : AudioController {
    init {
        RecitationStore.init(context)
    }

    // Everything played goes through the permanent store, so it is fetched at most once.
    private val dataSource = RecitationStore.dataSourceFactory()
    private val player = ExoPlayer.Builder(context.applicationContext)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
        .build()
    private val appContext = context.applicationContext
    private val _playing = MutableStateFlow<String?>(null)
    override val playing: StateFlow<String?> = _playing.asStateFlow()
    private val _index = MutableStateFlow(0)
    override val index: StateFlow<Int> = _index.asStateFlow()
    private val _arabicVoice = MutableStateFlow<Boolean?>(null)
    override val arabicVoice: StateFlow<Boolean?> = _arabicVoice.asStateFlow()
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: Pair<String, String>? = null
    /** The dua being read by the phone's voice; while set, the player's idle events must not clear [playing]. */
    private var spokenKey: String? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if ((state == Player.STATE_ENDED || state == Player.STATE_IDLE) && spokenKey == null) _playing.value = null
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _index.value = player.currentMediaItemIndex
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (spokenKey == null) _playing.value = null
            }
        })
    }

    override fun play(key: String, urls: List<String>) {
        tts?.stop()
        spokenKey = null
        _index.value = 0
        player.setMediaItems(urls.map(MediaItem::fromUri))
        player.prepare()
        player.play()
        _playing.value = key
    }

    /** Started lazily: most people never open a dua without a recording. */
    private fun ensureTts() {
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            val engine = tts ?: return@TextToSpeech
            val result = if (status == TextToSpeech.SUCCESS) engine.setLanguage(Locale("ar")) else TextToSpeech.LANG_NOT_SUPPORTED
            val ok = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            _arabicVoice.value = ok
            ttsReady = ok
            if (ok) {
                engine.setSpeechRate(0.8f)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) = finishSpeech(utteranceId)
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = finishSpeech(utteranceId)
                })
                pendingSpeech?.let { (key, text) -> engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, key) }
            }
            // Without an Arabic voice the card stays "playing" so it can explain why and offer the settings.
            pendingSpeech = null
        }
    }

    private fun finishSpeech(key: String?) {
        if (spokenKey == key) spokenKey = null
        _playing.compareAndSet(key, null)
    }

    override fun speak(key: String, arabic: String) {
        spokenKey = key
        player.stop()
        player.clearMediaItems()
        _index.value = 0
        _playing.value = key
        if (_arabicVoice.value == false) return
        ensureTts()
        val engine = tts
        if (ttsReady && engine != null) engine.speak(arabic, TextToSpeech.QUEUE_FLUSH, null, key)
        else pendingSpeech = key to arabic
    }

    override fun openVoiceSettings() {
        val intent = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(intent) }
            .onFailure { appContext.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    override fun stop() {
        tts?.stop()
        pendingSpeech = null
        spokenKey = null
        player.stop()
        player.clearMediaItems()
        _playing.value = null
    }

    fun release() {
        tts?.shutdown()
        tts = null
        player.release()
        _playing.value = null
    }
}
