package app.bibliya.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import app.bibliya.android.platform.Reminder
import app.bibliya.android.platform.ReminderSettings
import app.bibliya.android.ui.bible.BooksScreen
import app.bibliya.android.ui.bible.ChaptersScreen
import app.bibliya.android.ui.bible.ReaderScreen
import app.bibliya.android.ui.home.HomeScreen
import app.bibliya.android.ui.home.PlanToday
import app.bibliya.android.ui.more.MoreScreen
import app.bibliya.android.ui.plan.PlanScreen
import app.bibliya.android.ui.plan.currentPlanDay
import app.bibliya.android.ui.saved.SavedScreen
import app.bibliya.android.ui.search.SearchScreen
import app.bibliya.android.ui.situations.SituationScreen
import app.bibliya.android.ui.situations.SituationsScreen
import app.bibliya.android.ui.theme.BibleTheme
import app.bibliya.shared.i18n.stringsFor
import app.bibliya.shared.model.VerseRef

private enum class Tab(val icon: ImageVector) {
    HOME(Icons.Filled.Home),
    BIBLE(Icons.AutoMirrored.Filled.MenuBook),
    SITUATIONS(Icons.Filled.LightMode),
    SAVED(Icons.Filled.Bookmarks),
    MORE(Icons.Filled.MoreHoriz),
}

/** Full-screen destinations pushed over the tabs. */
private sealed interface Route {
    data object Search : Route
    data object Plan : Route
    data class Situation(val id: String) : Route
    data class Chapters(val book: String) : Route
    data class Reader(val book: String, val chapter: Int, val focus: IntRange?) : Route
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: AppViewModel, deps: AppDeps) {
    val settings by viewModel.settings.collectAsState()
    val uiLanguage by viewModel.uiLanguage.collectAsState()
    val translation by viewModel.translation.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val planDone by viewModel.planDone.collectAsState()
    val planStart by viewModel.planStart.collectAsState()
    val ready by viewModel.ready.collectAsState()
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val reference by viewModel.reference.collectAsState()
    val verseResults by viewModel.verseResults.collectAsState()
    val openVerse by deps.openVerseRequested.collectAsState()
    val context = LocalContext.current

    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    val stack = remember { mutableStateListOf<Route>() }
    val route = stack.lastOrNull()
    fun push(r: Route) = stack.add(r)
    fun pop() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
    val homeList = rememberLazyListState()

    val strings = stringsFor(uiLanguage)
    val content = viewModel.content
    val books = remember(translation, settings.showDeuterocanon) { content.booksFor(translation, settings.showDeuterocanon) }
    var notificationsAllowed by remember { mutableStateOf(true) }

    // The reminder runs without the database (after a reboot), so its settings are mirrored for it.
    LaunchedEffect(settings.dailyReminder, settings.reminderMinute, uiLanguage, translation) {
        Reminder.save(context, ReminderSettings(settings.dailyReminder, settings.reminderMinute, uiLanguage, translation))
        notificationsAllowed = deps.permissions.isGranted?.invoke("android.permission.POST_NOTIFICATIONS") ?: true
    }
    // The verse of the day is on by default, so ask for notifications once, on first launch.
    LaunchedEffect(Unit) {
        val current = viewModel.settings.value
        if (current.dailyReminder && !current.askedNotifications) {
            viewModel.updateSettings { it.copy(askedNotifications = true) }
            deps.permissions.request("android.permission.POST_NOTIFICATIONS") { notificationsAllowed = it }
        }
    }
    LaunchedEffect(openVerse) {
        if (openVerse) {
            stack.clear()
            tab = Tab.HOME
        }
    }

    BackHandler(enabled = route != null) { pop() }
    BackHandler(enabled = route == null && tab != Tab.HOME) { tab = Tab.HOME }

    val openRef: (VerseRef) -> Unit = { ref ->
        push(Route.Reader(ref.book, ref.chapter, if (ref.verse > 1 || ref.verseEnd > ref.verse) ref.verse..ref.verseEnd else null))
    }

    BibleTheme(settings.theme) {
        CompositionLocalProvider(
            LocalDeps provides deps,
            LocalStrings provides strings,
            LocalSettings provides settings,
            LocalUiLanguage provides uiLanguage,
            LocalTranslation provides translation,
        ) {
            when (val current = route) {
                Route.Search -> SearchScreen(
                    query = query,
                    results = results,
                    reference = reference,
                    verses = verseResults,
                    favorites = favorites,
                    onQueryChange = viewModel::onQueryChange,
                    onOpenSituation = { push(Route.Situation(it)) },
                    onOpenRef = openRef,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onBack = ::pop,
                )

                Route.Plan -> PlanScreen(
                    plan = content.plan,
                    started = planStart != null,
                    done = planDone,
                    onStart = viewModel::startPlan,
                    onReset = viewModel::resetPlan,
                    onToggleDay = viewModel::setPlanDay,
                    onOpenRef = openRef,
                    onBack = ::pop,
                )

                is Route.Situation -> {
                    val situation = content.situation(current.id)
                    if (situation == null) {
                        LaunchedEffect(current) { pop() }
                    } else {
                        SituationScreen(
                            situation = situation,
                            passages = content.passagesFor(current.id),
                            favorites = favorites,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onOpenRef = openRef,
                            onBack = ::pop,
                        )
                    }
                }

                is Route.Chapters -> {
                    val book = content.book(current.book)
                    if (book == null) {
                        LaunchedEffect(current) { pop() }
                    } else {
                        ChaptersScreen(book, translation, onOpenChapter = { push(Route.Reader(book.id, it, null)) }, onBack = ::pop)
                    }
                }

                is Route.Reader -> {
                    val book = content.book(current.book)
                    if (book == null) {
                        LaunchedEffect(current) { pop() }
                    } else {
                        ReaderScreen(
                            book = book,
                            // A psalm opened from Russian numbering may not exist in English: stay inside the book.
                            chapter = current.chapter.coerceIn(1, book.chapterCount(translation).coerceAtLeast(1)),
                            focus = current.focus,
                            lang = translation,
                            books = books,
                            ready = translation in ready,
                            bookmarks = bookmarks,
                            loadChapter = viewModel::chapter,
                            onToggleBookmark = { v -> viewModel.toggleBookmark(v.lang, v.book, v.chapter, v.number) },
                            onLastRead = viewModel::setLastRead,
                            onNavigate = { nav ->
                                pop()
                                push(Route.Reader(nav.book, nav.chapter, null))
                            },
                            onTranslation = { code -> viewModel.updateSettings { it.copy(translation = code) } },
                            onFontSize = { size -> viewModel.updateSettings { it.copy(fontSize = size) } },
                            onBack = ::pop,
                        )
                    }
                }

                null -> Scaffold(
                    topBar = {
                        if (tab != Tab.HOME) TopAppBar(title = { Text(strings.tabTitle(tab)) })
                    },
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach { entry ->
                                NavigationBarItem(
                                    selected = tab == entry,
                                    onClick = { tab = entry },
                                    icon = { Icon(entry.icon, contentDescription = null) },
                                    label = { Text(strings.tabTitle(entry), maxLines = 1) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        when (tab) {
                            Tab.HOME -> {
                                val planToday = planStart?.let {
                                    val day = currentPlanDay(planDone, content.plan.size)
                                    PlanToday(day, content.plan[day - 1], planDone.size, doneToday = day in planDone)
                                }
                                HomeScreen(
                                    verseOfDay = remember(uiLanguage) { content.verseOfDay(viewModel.today()) },
                                    situations = content.situations,
                                    lastRead = lastRead,
                                    plan = planToday,
                                    scrollToVerse = openVerse,
                                    onVerseShown = { deps.openVerseRequested.value = false },
                                    onOpenSearch = { example ->
                                        viewModel.onQueryChange(example)
                                        push(Route.Search)
                                    },
                                    onOpenSituation = { push(Route.Situation(it)) },
                                    onAllSituations = { tab = Tab.SITUATIONS },
                                    onOpenRef = openRef,
                                    onContinue = { pos ->
                                        if (pos.lang != translation) viewModel.updateSettings { it.copy(translation = pos.lang) }
                                        push(Route.Reader(pos.book, pos.chapter, if (pos.verse > 1) pos.verse..pos.verse else null))
                                    },
                                    onOpenPlan = { push(Route.Plan) },
                                    onStartPlan = viewModel::startPlan,
                                    onMarkPlanDay = { viewModel.setPlanDay(it, true) },
                                    listState = homeList,
                                )
                            }

                            Tab.BIBLE -> BooksScreen(books, translation, onOpenBook = { push(Route.Chapters(it)) })

                            Tab.SITUATIONS -> SituationsScreen(
                                situations = content.situations,
                                onOpenSituation = { push(Route.Situation(it)) },
                                onOpenSearch = {
                                    viewModel.onQueryChange("")
                                    push(Route.Search)
                                },
                            )

                            Tab.SAVED -> SavedScreen(
                                passages = content.passages.filter { it.id in favorites },
                                bookmarks = bookmarks,
                                favorites = favorites,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onOpenRef = openRef,
                                onOpenBookmark = { b ->
                                    if (b.lang != translation) viewModel.updateSettings { it.copy(translation = b.lang) }
                                    push(Route.Reader(b.book, b.chapter, b.verse..b.verse))
                                },
                            )

                            Tab.MORE -> MoreScreen(
                                notificationsAllowed = notificationsAllowed,
                                onUpdateSettings = viewModel::updateSettings,
                                onEnableReminder = {
                                    deps.permissions.request("android.permission.POST_NOTIFICATIONS") { notificationsAllowed = it }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun app.bibliya.shared.i18n.Strings.tabTitle(tab: Tab): String = when (tab) {
    Tab.HOME -> navHome
    Tab.BIBLE -> navBible
    Tab.SITUATIONS -> navSituations
    Tab.SAVED -> navSaved
    Tab.MORE -> navMore
}
