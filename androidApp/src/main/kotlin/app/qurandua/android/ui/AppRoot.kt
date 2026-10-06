package app.qurandua.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Mosque
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.qurandua.android.ui.duas.DuasScreen
import app.qurandua.android.ui.home.HomeScreen
import app.qurandua.android.ui.more.MoreScreen
import app.qurandua.android.ui.prayer.PrayerScreen
import app.qurandua.android.ui.prayer.nextPrayerLine
import app.qurandua.android.ui.quran.SurahListScreen
import app.qurandua.android.ui.quran.SurahReaderScreen
import app.qurandua.android.ui.saved.SavedScreen
import app.qurandua.android.ui.search.SearchScreen
import app.qurandua.android.ui.situation.SituationScreen
import app.qurandua.android.ui.theme.QuranDuaTheme
import app.qurandua.android.ui.zakat.ZakatScreen
import app.qurandua.shared.data.RTL_LANGUAGES
import app.qurandua.shared.i18n.prayerStringsFor
import app.qurandua.shared.i18n.stringsFor
import kotlinx.coroutines.delay
import app.qurandua.shared.model.AyahKey

private enum class Tab(val icon: ImageVector) {
    HOME(Icons.Filled.Home),
    QURAN(Icons.AutoMirrored.Filled.MenuBook),
    DUAS(Icons.Filled.Mosque),
    SAVED(Icons.Filled.Favorite),
    MORE(Icons.Filled.MoreHoriz),
}

/** Full-screen destinations pushed over the tabs. */
private sealed interface Route {
    data object None : Route
    data object Search : Route
    data object Zakat : Route
    data object Prayer : Route
    data class Situation(val id: String) : Route
    data class Reader(val surah: Int, val ayah: Int?) : Route
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: AppViewModel, deps: AppDeps) {
    val settings by viewModel.settings.collectAsState()
    val uiLanguage by viewModel.uiLanguage.collectAsState()
    val contentLanguage by viewModel.contentLanguage.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val quranReady by viewModel.quranReady.collectAsState()
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val quranResults by viewModel.quranResults.collectAsState()

    var tab by remember { mutableStateOf(Tab.HOME) }
    var route by remember { mutableStateOf<Route>(Route.None) }
    val strings = stringsFor(uiLanguage)
    val prayerStrings = prayerStringsFor(uiLanguage)
    val prayerSettings by deps.prayer.settings.collectAsState()
    var minuteTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            minuteTick++
        }
    }
    LaunchedEffect(uiLanguage) { deps.prayer.setLanguage(uiLanguage) }
    // Arabic and Urdu read right to left; the whole UI mirrors with the language.
    val direction = if (uiLanguage in RTL_LANGUAGES) LayoutDirection.Rtl else LayoutDirection.Ltr

    QuranDuaTheme(settings.theme) {
        CompositionLocalProvider(
            LocalDeps provides deps,
            LocalStrings provides strings,
            LocalSettings provides settings,
            LocalContentLang provides contentLanguage,
            LocalLayoutDirection provides direction,
        ) {
            val openAyah: (AyahKey) -> Unit = { key -> route = Route.Reader(key.surah, key.ayah) }

            when (val current = route) {
                is Route.Search -> SearchScreen(
                    query = query,
                    results = results,
                    quranResults = quranResults,
                    favorites = favorites,
                    onQueryChange = viewModel::onQueryChange,
                    onOpenSituation = { route = Route.Situation(it) },
                    onOpenAyah = openAyah,
                    onBack = { route = Route.None },
                )

                Route.Zakat -> ZakatScreen(onBack = { route = Route.None })

                Route.Prayer -> PrayerScreen(prayerStrings, onBack = { route = Route.None })

                is Route.Situation -> {
                    val situation = viewModel.situation(current.id)
                    if (situation == null) {
                        route = Route.None
                    } else {
                        SituationScreen(
                            situation = situation,
                            duas = viewModel.duasFor(current.id),
                            favorites = favorites,
                            onBack = { route = Route.None },
                            onOpenInQuran = openAyah,
                        )
                    }
                }

                is Route.Reader -> SurahReaderScreen(
                    surahNumber = current.surah,
                    initialAyah = current.ayah,
                    loadSurah = viewModel::surahInfo,
                    loadAyahs = viewModel::ayahs,
                    bookmarks = bookmarks,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onLastRead = viewModel::setLastRead,
                    onBack = { route = Route.None },
                )

                Route.None -> Scaffold(
                    topBar = {
                        if (tab != Tab.HOME) {
                            TopAppBar(title = { Text(strings.tabTitle(tab)) })
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach { entry ->
                                NavigationBarItem(
                                    selected = tab == entry,
                                    onClick = { tab = entry },
                                    icon = { Icon(entry.icon, contentDescription = null) },
                                    label = { Text(strings.tabTitle(entry)) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        when (tab) {
                            Tab.HOME -> HomeScreen(
                                situations = viewModel.situations,
                                lastRead = lastRead,
                                onSearch = { example ->
                                    viewModel.onQueryChange(example)
                                    route = Route.Search
                                },
                                onOpenSearch = { route = Route.Search },
                                onOpenSituation = { route = Route.Situation(it) },
                                onOpenQuran = { tab = Tab.QURAN },
                                onContinueReading = openAyah,
                                prayerTitle = prayerStrings.title,
                                prayerLine = remember(prayerSettings, uiLanguage, minuteTick) {
                                    nextPrayerLine(prayerSettings, prayerStrings)
                                },
                                onOpenPrayer = { route = Route.Prayer },
                            )

                            Tab.QURAN -> SurahListScreen(
                                surahs = surahs,
                                quranReady = quranReady,
                                onOpenSurah = { route = Route.Reader(it, null) },
                            )

                            Tab.DUAS -> DuasScreen(
                                duas = viewModel.allDuas(),
                                favorites = favorites,
                                onOpenInQuran = openAyah,
                            )

                            Tab.SAVED -> SavedScreen(
                                duas = viewModel.favoriteDuas(favorites),
                                bookmarks = bookmarks,
                                favorites = favorites,
                                onOpenAyah = openAyah,
                            )

                            Tab.MORE -> MoreScreen(
                                onUpdateSettings = viewModel::updateSettings,
                                onOpenZakat = { route = Route.Zakat },
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun app.qurandua.shared.i18n.Strings.tabTitle(tab: Tab): String = when (tab) {
    Tab.HOME -> navHome
    Tab.QURAN -> navQuran
    Tab.DUAS -> navDuas
    Tab.SAVED -> navSaved
    Tab.MORE -> navMore
}
