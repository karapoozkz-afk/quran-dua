package app.qurandua.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.qurandua.shared.data.ContentRepository
import app.qurandua.shared.data.QuranRepository
import app.qurandua.shared.data.UserDataRepository
import app.qurandua.shared.data.contentLanguageFor
import app.qurandua.shared.model.AppSettings
import app.qurandua.shared.model.Ayah
import app.qurandua.shared.model.AyahKey
import app.qurandua.shared.model.DuaItem
import app.qurandua.shared.model.Situation
import app.qurandua.shared.model.SurahInfo
import app.qurandua.shared.search.SearchResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuranSearchState(
    val terms: List<String> = emptyList(),
    val ayahs: List<Ayah> = emptyList(),
    val loading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val content: ContentRepository,
    private val quran: QuranRepository,
    private val user: UserDataRepository,
    private val readQuranAsset: suspend () -> String,
    uiLanguageFallback: String,
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        user.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    /** The chosen language, or the device language when the user has not chosen one. */
    val uiLanguage: StateFlow<String> = settings
        .map { it.language.ifBlank { uiLanguageFallback } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, uiLanguageFallback)

    val contentLanguage: StateFlow<String> = uiLanguage
        .map(::contentLanguageFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, contentLanguageFor(uiLanguageFallback))

    val favorites: StateFlow<Set<String>> =
        user.favoriteIds.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val bookmarks: StateFlow<List<AyahKey>> =
        user.bookmarks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val lastRead: StateFlow<AyahKey?> =
        user.lastRead.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val surahs: StateFlow<List<SurahInfo>> = contentLanguage
        .flatMapLatest { quran.surahs(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _quranReady = MutableStateFlow(false)
    val quranReady: StateFlow<Boolean> = _quranReady.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<SearchResults?>(null)
    val results: StateFlow<SearchResults?> = _results.asStateFlow()

    private val _quranResults = MutableStateFlow(QuranSearchState())
    val quranResults: StateFlow<QuranSearchState> = _quranResults.asStateFlow()

    private var quranSearchJob: Job? = null

    val situations: List<Situation> get() = content.situations

    init {
        viewModelScope.launch {
            quran.ensureImported(readQuranAsset)
            _quranReady.value = true
            rerunQuranSearch()
        }
    }

    fun situation(id: String): Situation? = content.situation(id)

    fun duasFor(situationId: String): List<DuaItem> = content.duasFor(situationId)

    fun allDuas(): List<DuaItem> = content.duas

    fun favoriteDuas(ids: Set<String>): List<DuaItem> = content.duas.filter { it.id in ids }

    fun onQueryChange(value: String) {
        _query.value = value
        if (value.isBlank()) {
            _results.value = null
            _quranResults.value = QuranSearchState()
            return
        }
        val results = content.search.search(value)
        _results.value = results
        _quranResults.value = QuranSearchState(terms = results.quranTerms, loading = _quranReady.value)
        rerunQuranSearch()
    }

    private fun rerunQuranSearch() {
        val terms = _quranResults.value.terms
        quranSearchJob?.cancel()
        if (terms.isEmpty() || !_quranReady.value) return
        quranSearchJob = viewModelScope.launch {
            val ayahs = quran.search(terms, contentLanguage.value, limit = 25)
            _quranResults.value = QuranSearchState(terms, ayahs, loading = false)
        }
    }

    suspend fun ayahs(surah: Int): List<Ayah> = quran.ayahs(surah, contentLanguage.value)

    suspend fun surahInfo(number: Int): SurahInfo? = quran.surah(number, contentLanguage.value)

    fun toggleFavorite(id: String) = viewModelScope.launch { user.toggleFavorite(id) }.let { }

    fun toggleBookmark(key: AyahKey) = viewModelScope.launch { user.toggleBookmark(key) }.let { }

    fun setLastRead(key: AyahKey) = viewModelScope.launch { user.setLastRead(key) }.let { }

    fun updateSettings(transform: (AppSettings) -> AppSettings) =
        viewModelScope.launch { user.updateSettings(transform) }.let { }
}
