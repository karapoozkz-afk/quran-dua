package app.qurandua.shared

import app.qurandua.shared.data.ContentRepository
import app.qurandua.shared.data.contentLanguageFor
import app.qurandua.shared.db.RoomQuranRepository
import app.qurandua.shared.db.RoomUserDataRepository
import app.qurandua.shared.db.buildAppDatabase
import app.qurandua.shared.db.iosDatabaseBuilder
import app.qurandua.shared.i18n.Strings
import app.qurandua.shared.i18n.stringsFor
import app.qurandua.shared.model.Ayah
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.SurahInfo
import app.qurandua.shared.model.pick
import app.qurandua.shared.search.SearchResults
import app.qurandua.shared.zakat.ZakatCalculator
import app.qurandua.shared.zakat.ZakatInput
import app.qurandua.shared.zakat.ZakatResult
import app.qurandua.shared.zakat.ZakatRules
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * The single entry point Swift talks to. Everything the Android app does with
 * content, search, the Quran database, favourites and zakat goes through here,
 * so the SwiftUI app only draws screens.
 *
 * Swift sees `suspend` functions as `async throws` (Kotlin 2.x, Swift export
 * via the Objective-C header), e.g. `try await sdk.ayahs(surah: 1, lang: "ru")`.
 */
class QuranDuaSdk(private val bundle: NSBundle = NSBundle.mainBundle) {

    private val db = buildAppDatabase(iosDatabaseBuilder())
    private val quran = RoomQuranRepository(db)
    private val user = RoomUserDataRepository(db)
    val content: ContentRepository = ContentRepository.fromJson(
        resource("situations"),
        resource("duas"),
        resource("charities"),
    )

    fun strings(uiLanguage: String): Strings = stringsFor(uiLanguage)

    /** Localized.pick for Swift, where Kotlin extension functions on Map are awkward to call. */
    fun text(localized: Map<String, String>?, lang: String): String = localized?.pick(lang) ?: ""

    fun contentLanguage(uiLanguage: String): String = contentLanguageFor(uiLanguage)

    val situations: List<Situation> get() = content.situations

    fun duasFor(situationId: String): List<DuaItem> = content.duasFor(situationId)

    fun search(query: String): SearchResults = content.search.search(query)

    suspend fun prepareQuran() = quran.ensureImported { resource("quran") }

    suspend fun surahs(lang: String): List<SurahInfo> = quran.surahs(lang).first()

    suspend fun ayahs(surah: Int, lang: String): List<Ayah> = quran.ayahs(surah, lang)

    suspend fun searchQuran(terms: List<String>, lang: String): List<Ayah> = quran.search(terms, lang, 25)

    suspend fun favoriteIds(): Set<String> = user.favoriteIds.first()

    suspend fun toggleFavorite(itemId: String) = user.toggleFavorite(itemId)

    suspend fun toggleBookmark(surah: Int, ayah: Int) = user.toggleBookmark(AyahKey(surah, ayah))

    fun zakat(input: ZakatInput, rules: ZakatRules): ZakatResult = ZakatCalculator.calculate(input, rules)

    /** Reads content/<name>.json, copied into the app bundle by the Xcode build phase. */
    @OptIn(ExperimentalForeignApi::class)
    private fun resource(name: String): String {
        val path = requireNotNull(bundle.pathForResource(name, "json", "content")) { "content/$name.json missing from bundle" }
        return requireNotNull(NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)) { "cannot read $path" }
    }
}
