package app.qurandua.android.platform

import android.content.Context
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import app.qurandua.android.ui.JournalController
import app.qurandua.shared.journal.JournalData
import app.qurandua.shared.journal.JournalEntry
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** Folder and prefs file of the journal; res/xml backup rules keep both out of every backup. */
private const val JOURNAL_DIR = "journal"
private const val JOURNAL_FILE = "entries.bin"
private const val LOCK_PREFS = "journal_lock"
private const val KEY_SALT = "salt"
private const val KEY_HASH = "hash"
private const val KEY_ALIAS = "journal_key"
private const val IV_BYTES = 12
private const val PBKDF2_ROUNDS = 20_000
/** After this many wrong PINs in a row, each further try waits [LOCKOUT_MS]. */
private const val FREE_TRIES = 5
private const val LOCKOUT_MS = 30_000L

private val journalJson = Json { ignoreUnknownKeys = true }

/**
 * The repentance journal: notes stay on this phone only. They are written as one file encrypted
 * with AES-GCM under a key that lives in the Android Keystore and never leaves the device, so even
 * a copy of the file is unreadable elsewhere. No network, no account, no analytics.
 */
class AndroidJournalController(context: Context) : JournalController {
    private val app = context.applicationContext
    private val file = AtomicFile(File(File(app.filesDir, JOURNAL_DIR).apply { mkdirs() }, JOURNAL_FILE))
    private val prefs = app.getSharedPreferences(LOCK_PREFS, Context.MODE_PRIVATE)
    /** Writes go one by one, off the main thread. */
    private val writer = Executors.newSingleThreadExecutor()

    /** Read on first use (when the journal is opened), not at app start. */
    private val _entries by lazy { MutableStateFlow(load()) }
    override val entries: StateFlow<List<JournalEntry>> get() = _entries.asStateFlow()

    private val _hasPin = MutableStateFlow(prefs.contains(KEY_HASH))
    override val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    private var wrongTries = 0
    private var lockedUntil = 0L

    override fun save(entry: JournalEntry) {
        val list = _entries.value.filterNot { it.id == entry.id } + entry
        publish(list.sortedByDescending { it.createdAt })
    }

    override fun delete(id: Long) = publish(_entries.value.filterNot { it.id == id })

    override fun deleteAll() {
        publish(emptyList())
        clearPin()
    }

    override fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_SALT, salt.b64())
            .putString(KEY_HASH, hash(pin, salt).b64())
            .apply()
        _hasPin.value = true
    }

    override fun clearPin() {
        prefs.edit().remove(KEY_SALT).remove(KEY_HASH).apply()
        _hasPin.value = false
        wrongTries = 0
        lockedUntil = 0
    }

    override fun waitMillis(): Long = (lockedUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0)

    override fun checkPin(pin: String): Boolean {
        if (waitMillis() > 0) return false
        val salt = prefs.getString(KEY_SALT, null)?.unb64() ?: return true
        val expected = prefs.getString(KEY_HASH, null)?.unb64() ?: return true
        val ok = MessageDigest.isEqual(hash(pin, salt), expected)
        if (ok) {
            wrongTries = 0
        } else if (++wrongTries >= FREE_TRIES) {
            lockedUntil = SystemClock.elapsedRealtime() + LOCKOUT_MS
        }
        return ok
    }

    private fun publish(list: List<JournalEntry>) {
        _entries.value = list
        val bytes = journalJson.encodeToString(JournalData.serializer(), JournalData(list)).toByteArray()
        writer.execute { write(bytes) }
    }

    private fun load(): List<JournalEntry> {
        if (!file.baseFile.exists()) return emptyList()
        return runCatching {
            val stored = file.readFully()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, stored, 0, IV_BYTES))
            val plain = cipher.doFinal(stored, IV_BYTES, stored.size - IV_BYTES)
            journalJson.decodeFromString(JournalData.serializer(), String(plain)).entries.sortedByDescending { it.createdAt }
        }.getOrElse {
            // Never overwrite notes we failed to read (a Keystore hiccup): set the file aside instead.
            file.baseFile.renameTo(File(file.baseFile.parentFile, "$JOURNAL_FILE.unreadable-${System.currentTimeMillis()}"))
            emptyList()
        }
    }

    private fun write(plain: ByteArray) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val out = cipher.iv + cipher.doFinal(plain)
        val stream = file.startWrite()
        try {
            stream.write(out)
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
        }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    /** PBKDF2WithHmacSHA1 is the variant every Android version since 24 has. */
    private fun hash(pin: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ROUNDS, 256))
            .encoded

    private fun ByteArray.b64() = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.unb64() = Base64.decode(this, Base64.NO_WRAP)
}
