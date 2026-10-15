package app.qurandua.android.platform

import android.content.Context
import android.os.SystemClock
import app.qurandua.android.BuildConfig
import app.qurandua.android.ui.WallController
import app.qurandua.shared.journal.JournalKind
import app.qurandua.shared.journal.WallFirestore
import app.qurandua.shared.journal.WallPost
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Prefs: the anonymous sign-in and which posts this phone already answered or reported. Kept out of backups. */
private const val WALL_PREFS = "wall"
private const val KEY_REFRESH = "refresh_token"
private const val KEY_UID = "uid"

/**
 * The shared repentance wall on Firestore, through its REST API (no Firebase SDK, no Google Play
 * services). The phone signs in anonymously: Firebase knows a random id, never a name, phone number
 * or email, and the id is not written on posts. Config comes from the build (wallProjectId,
 * wallApiKey); without it the wall says it opens soon.
 */
class AndroidWallController(context: Context) : WallController {
    private val prefs = context.applicationContext.getSharedPreferences(WALL_PREFS, Context.MODE_PRIVATE)
    private val projectId = BuildConfig.WALL_PROJECT_ID
    private val apiKey = BuildConfig.WALL_API_KEY
    private val root = WallFirestore.root(projectId)
    private val base = "https://firestore.googleapis.com/v1/$root"
    private val json = Json { ignoreUnknownKeys = true }
    private val authLock = Mutex()
    private var idToken: String? = null
    private var tokenExpires = 0L

    override val configured: Boolean = projectId.isNotBlank() && apiKey.isNotBlank()

    override suspend fun posts(lang: String): List<WallPost> =
        WallFirestore.parsePosts(post("$base:runQuery", WallFirestore.postsQuery(lang)))

    override suspend fun send(text: String, kind: JournalKind, lang: String) {
        post("$base:commit", WallFirestore.newPost(root, WallFirestore.newId(), text, kind, lang))
    }

    override suspend fun dua(postId: String, index: Int) {
        try {
            post("$base:commit", WallFirestore.dua(root, postId, uid(), index))
        } catch (e: HttpError) {
            // The marker doc exists: this phone already answered (precondition or rules refuse). Remember it and move on.
            if (e.code !in setOf(400, 403, 409)) throw e
        }
        prefs.edit().putInt("dua_$postId", index).apply()
    }

    override suspend fun report(postId: String) {
        try {
            post("$base:commit", WallFirestore.report(root, postId, uid()))
        } catch (e: HttpError) {
            if (e.code !in setOf(400, 403, 409)) throw e
        }
        prefs.edit().putBoolean("report_$postId", true).apply()
    }

    override fun myDua(postId: String): Int? = prefs.getInt("dua_$postId", -1).takeIf { it >= 0 }

    override fun reported(postId: String): Boolean = prefs.getBoolean("report_$postId", false)

    private suspend fun uid(): String {
        token()
        return prefs.getString(KEY_UID, null) ?: throw IOException("not signed in")
    }

    /** A Firebase ID token, signing up anonymously once and refreshing it when it runs out. */
    private suspend fun token(): String = authLock.withLock {
        idToken?.takeIf { SystemClock.elapsedRealtime() < tokenExpires }?.let { return@withLock it }
        val refresh = prefs.getString(KEY_REFRESH, null)
        val reply = if (refresh == null) {
            json.parseToJsonElement(
                http("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey", """{"returnSecureToken":true}""", null),
            ).jsonObject.let { o ->
                Triple(o.str("idToken"), o.str("refreshToken"), o.str("localId")) to o.str("expiresIn")
            }
        } else {
            val form = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refresh, "UTF-8")
            json.parseToJsonElement(
                http("https://securetoken.googleapis.com/v1/token?key=$apiKey", form, null, "application/x-www-form-urlencoded"),
            ).jsonObject.let { o ->
                Triple(o.str("id_token"), o.str("refresh_token"), o.str("user_id")) to o.str("expires_in")
            }
        }
        val (tokens, expiresIn) = reply
        prefs.edit().putString(KEY_REFRESH, tokens.second).putString(KEY_UID, tokens.third).apply()
        idToken = tokens.first
        // Refresh a minute early.
        tokenExpires = SystemClock.elapsedRealtime() + ((expiresIn.toLongOrNull() ?: 3600) - 60) * 1000
        tokens.first
    }

    private suspend fun post(url: String, body: String): String {
        val first = runCatching { http(url, body, token()) }
        val error = first.exceptionOrNull()
        if (error is HttpError && error.code == 401) {
            // The token was revoked or the account reset: sign in again once.
            authLock.withLock { idToken = null; prefs.edit().remove(KEY_REFRESH).remove(KEY_UID).apply() }
            return http(url, body, token())
        }
        return first.getOrThrow()
    }

    private suspend fun http(url: String, body: String, bearer: String?, type: String = "application/json"): String =
        withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "$type; charset=utf-8")
                if (bearer != null) conn.setRequestProperty("Authorization", "Bearer $bearer")
                conn.outputStream.use { it.write(body.toByteArray()) }
                val code = conn.responseCode
                if (code !in 200..299) throw HttpError(code)
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
        }

    private fun kotlinx.serialization.json.JsonObject.str(name: String): String =
        this[name]?.jsonPrimitive?.content ?: throw IOException("missing $name")

    private class HttpError(val code: Int) : IOException("HTTP $code")
}
