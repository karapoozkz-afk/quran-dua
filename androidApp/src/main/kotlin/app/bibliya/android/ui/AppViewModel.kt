package app.bibliya.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bibliya.shared.data.BibleRepository
import app.bibliya.shared.data.ContentRepository
import app.bibliya.shared.data.UserDataRepository
import app.bibliya.shared.data.translationFor
import app.bibliya.shared.model.AppSettings
import app.bibliya.shared.model.Tradition
import app.bibliya.shared.model.Bookmark
import app.bibliya.shared.model.ReadingPosition
import app.bibliya.shared.model.Verse
import app.bibliya.shared.model.VerseRef
import app.bibliya.shared.search.SearchResults
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class VerseSearchState(
    val terms: List<String> = emptyList(),
    val verses: List<Verse> = emptyList(),
    val loading: Boolean = false,
)

class AppViewModel(
    val content: ContentRepository,
    private val bible: BibleRepository,
    private val user: UserDataRepository,
    private val readBibleAsset: suspend (String) -> String,
    uiLanguageFallback: String,
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        user.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    /** False until the saved settings are read, so first-launch questions don't flash for returning users. */
    val settingsLoaded: StateFlow<Boolean> =
        user.settings.map { true }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** The chosen language, or the device language when the user has not chosen one. */
    val uiLanguage: StateFlow<String> = settings
        .map { it.language.ifBlank { uiLanguageFallback } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, uiLanguageFallback)

    val translation: StateFlow<String> = combine(settings, uiLanguage) { s, ui -> translationFor(s, ui) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, translationFor(AppSettings(), uiLanguageFallback))

    val favorites: StateFlow<Set<String>> =
        user.favoritePassages.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val bookmarks: StateFlow<List<Bookmark>> =
        user.bookmarks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val lastRead: StateFlow<ReadingPosition?> =
        user.lastRead.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val planDone: StateFlow<Set<Int>> =
        user.planDone.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val planStart: StateFlow<LocalDate?> =
        user.planStart.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Translations already imported into the database and ready to read and search. */
    private val _ready = MutableStateFlow<Set<String>>(emptySet())
    val ready: StateFlow<Set<String>> = _ready.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<SearchResults?>(null)
    val results: StateFlow<SearchResults?> = _results.asStateFlow()

    private val _reference = MutableStateFlow<VerseRef?>(null)
    /** Set when the query is a reference like "Ин 3:16". */
    val reference: StateFlow<VerseRef?> = _reference.asStateFlow()

    private val _verseResults = MutableStateFlow(VerseSearchState())
    val verseResults: StateFlow<VerseSearchState> = _verseResults.asStateFlow()

    private var verseSearchJob: Job? = null

    init {
        // Import the translation being read first, then the other one in the background.
        viewModelScope.launch {
            translation.collect { lang -> ensure(lang) }
        }
    }

    private suspend fun ensure(lang: String) {
        if (lang in _ready.value) return
        bible.ensureImported(lang, readBibleAsset)
        _ready.value = _ready.value + lang
        if (lang == translation.value) rerunVerseSearch()
    }

    fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun onQueryChange(value: String) {
        _query.value = value
        if (value.isBlank()) {
            _results.value = null
            _reference.value = null
            _verseResults.value = VerseSearchState()
            return
        }
        _reference.value = content.references.parse(value)?.takeIf { content.book(it.book) != null }
        val results = content.search.search(value)
        _results.value = results
        _verseResults.value = VerseSearchState(terms = results.bibleTerms, loading = translation.value in _ready.value)
        rerunVerseSearch()
    }

    private fun rerunVerseSearch() {
        val terms = _verseResults.value.terms
        verseSearchJob?.cancel()
        val lang = translation.value
        if (terms.isEmpty() || lang !in _ready.value) return
        verseSearchJob = viewModelScope.launch {
            val verses = bible.search(lang, terms, limit = 40)
            _verseResults.value = VerseSearchState(terms, verses, loading = false)
        }
    }

    suspend fun chapter(lang: String, book: String, chapter: Int): List<Verse> {
        ensure(lang)
        return bible.chapter(lang, book, chapter)
    }

    fun toggleFavorite(id: String) = viewModelScope.launch { user.toggleFavorite(id) }.let { }

    fun toggleBookmark(lang: String, book: String, chapter: Int, verse: Int) =
        viewModelScope.launch { user.toggleBookmark(lang, book, chapter, verse) }.let { }

    fun setLastRead(position: ReadingPosition) = viewModelScope.launch { user.setLastRead(position) }.let { }

    fun setPlanDay(day: Int, done: Boolean) = viewModelScope.launch { user.setPlanDay(day, done) }.let { }

    fun startPlan() = viewModelScope.launch { user.setPlanStart(today()) }.let { }

    fun resetPlan() = viewModelScope.launch { user.setPlanStart(null) }.let { }

    /** Picking a tradition also sets whether the deuterocanonical books are listed; the switch can still change it. */
    fun chooseTradition(tradition: Tradition) =
        updateSettings { it.copy(tradition = tradition, showDeuterocanon = tradition.showsDeuterocanon) }

    fun updateSettings(transform: (AppSettings) -> AppSettings) =
        viewModelScope.launch { user.updateSettings(transform) }.let { }
}
