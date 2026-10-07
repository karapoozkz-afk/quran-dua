package app.qurandua.android.ui.prayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.qurandua.android.ui.LocalDeps
import app.qurandua.android.ui.components.BackTopBar
import app.qurandua.android.ui.components.NoticeCard
import app.qurandua.android.ui.components.SectionTitle
import app.qurandua.shared.i18n.PrayerStrings
import app.qurandua.shared.prayer.AdhanSound
import app.qurandua.shared.prayer.AsrSchool
import app.qurandua.shared.prayer.CITIES
import app.qurandua.shared.prayer.CalculationMethod
import app.qurandua.shared.prayer.Prayer
import app.qurandua.shared.prayer.PrayerSettings
import app.qurandua.shared.prayer.PrayerTimesCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val PERMISSION_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"
private const val PERMISSION_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

/** "HH:mm" of [instant] in [zone]. */
fun formatTime(instant: Instant, zone: TimeZone): String {
    val t = instant.toLocalDateTime(zone)
    return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
}

fun PrayerSettings.zone(): TimeZone =
    place?.let { runCatching { TimeZone.of(it.timeZone) }.getOrNull() } ?: TimeZone.currentSystemDefault()

/** "Next: Asr at 15:42" for the home screen, or null when no place is chosen. */
fun nextPrayerLine(settings: PrayerSettings, strings: PrayerStrings, now: Instant = Clock.System.now()): String? {
    val config = settings.config() ?: return null
    val zone = settings.zone()
    val (prayer, at) = PrayerTimesCalculator.next(now, now.toLocalDateTime(zone).date, config)
    return strings.next(strings.prayerName(prayer), formatTime(at, zone))
}

/**
 * Today's prayer times, computed on the phone for a chosen city or the device location.
 * Every choice the result depends on (method, Asr school, minute adjustments) is visible
 * and changeable, because local mosque timetables differ by a few minutes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerScreen(strings: PrayerStrings, onBack: () -> Unit) {
    val deps = LocalDeps.current
    val controller = deps.prayer
    val settings by controller.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var now by remember { mutableStateOf(Clock.System.now()) }
    var pickingCity by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }
    val adhanPlaying by controller.adhanPlaying.collectAsState()
    // Re-read on every return to the screen: the user may have just changed them in system settings.
    var fullScreenAllowed by remember { mutableStateOf(controller.canUseFullScreen()) }
    var exactAllowed by remember { mutableStateOf(controller.canScheduleExact()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = Clock.System.now()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            fullScreenAllowed = controller.canUseFullScreen()
            exactAllowed = controller.canScheduleExact()
            delay(2_000)
        }
    }
    // A preview stops when the user leaves the screen; a real prayer-time adhan keeps playing.
    val previewStarted = remember { booleanArrayOf(false) }
    DisposableEffect(Unit) { onDispose { if (previewStarted[0]) controller.stopAdhan() } }

    Scaffold(topBar = { BackTopBar(strings.title, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Place
            SectionTitle(strings.place)
            Text(settings.place?.name ?: strings.noPlace, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickingCity = true }) { Text(strings.chooseCity) }
                OutlinedButton(
                    enabled = !locating,
                    onClick = {
                        deps.permissions.request(PERMISSION_LOCATION) { granted ->
                            if (!granted) {
                                message = strings.locationDenied
                            } else {
                                locating = true
                                scope.launch {
                                    val place = controller.currentLocation()
                                    locating = false
                                    if (place == null) message = strings.locationDenied
                                    else controller.update { it.copy(place = place) }
                                }
                            }
                        }
                    },
                ) { Text(strings.useLocation) }
            }
            message?.let { NoticeCard(it) }

            val config = settings.config()
            if (config != null) {
                val zone = settings.zone()
                val today = now.toLocalDateTime(zone).date
                val day = PrayerTimesCalculator.compute(today, config)
                val (nextPrayer, nextAt) = PrayerTimesCalculator.next(now, today, config)

                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            strings.next(strings.prayerName(nextPrayer), formatTime(nextAt, zone)),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        HorizontalDivider()
                        Prayer.entries.forEach { prayer ->
                            val bold = prayer == nextPrayer && day[prayer] == nextAt
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    strings.prayerName(prayer),
                                    Modifier.weight(1f),
                                    fontWeight = if (bold) FontWeight.Bold else null,
                                )
                                Text(formatTime(day[prayer], zone), fontWeight = if (bold) FontWeight.Bold else null)
                            }
                        }
                    }
                }

                if (settings.place?.country == "KZ" || settings.effectiveMethod == CalculationMethod.KAZAKHSTAN_DUMK) {
                    NoticeCard(strings.dumkNote)
                }

                // Notifications
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.notifications, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = settings.notificationsOn,
                        onCheckedChange = { on ->
                            if (!on) {
                                controller.update { it.copy(notificationsOn = false) }
                            } else {
                                deps.permissions.request(PERMISSION_NOTIFICATIONS) { granted ->
                                    if (!granted) message = strings.notificationsDenied
                                    controller.update { it.copy(notificationsOn = granted) }
                                }
                            }
                        },
                    )
                }
                if (settings.notificationsOn) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Prayer.entries.filter { it != Prayer.SUNRISE }.forEach { prayer ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = prayer in settings.notify,
                                    onCheckedChange = { on ->
                                        controller.update { s -> s.copy(notify = if (on) s.notify + prayer else s.notify - prayer) }
                                    },
                                )
                                Text(strings.prayerName(prayer))
                            }
                        }
                    }

                    // Adhan: what sounds when the time comes, and the full-screen prayer window.
                    SectionTitle(strings.adhan)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdhanSound.entries.forEach { sound ->
                            FilterChip(
                                selected = settings.adhan == sound,
                                onClick = { controller.update { it.copy(adhan = sound) } },
                                label = { Text(strings.adhanName(sound)) },
                            )
                        }
                    }
                    if (settings.adhan != AdhanSound.SILENT) {
                        OutlinedButton(onClick = { if (adhanPlaying) {
                                controller.stopAdhan()
                            } else {
                                previewStarted[0] = true
                                controller.preview(settings.adhan)
                            }
                        }) {
                            Text(if (adhanPlaying) strings.stop else strings.listen)
                        }
                    }
                    if (settings.adhan == AdhanSound.FULL || settings.adhan == AdhanSound.SHORT) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.fullScreen, Modifier.weight(1f))
                            Switch(checked = settings.fullScreen, onCheckedChange = { on -> controller.update { it.copy(fullScreen = on) } })
                        }
                        if (settings.fullScreen && !fullScreenAllowed) {
                            NoticeCard(strings.fullScreenDenied)
                            OutlinedButton(onClick = controller::openFullScreenSettings) { Text(strings.openSettings) }
                        }
                        Text(strings.fajrNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(strings.adhanCredit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!exactAllowed) {
                        NoticeCard(strings.exactDenied)
                        OutlinedButton(onClick = controller::openExactAlarmSettings) { Text(strings.openSettings) }
                    }
                }

                // Method
                SectionTitle(strings.method)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalculationMethod.entries.forEach { method ->
                        FilterChip(
                            selected = settings.effectiveMethod == method,
                            onClick = { controller.update { it.copy(method = method) } },
                            label = { Text(strings.methodName(method)) },
                        )
                    }
                }

                SectionTitle(strings.asr)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AsrSchool.HANAFI to strings.asrHanafi, AsrSchool.STANDARD to strings.asrStandard).forEach { (school, label) ->
                        FilterChip(
                            selected = settings.effectiveAsr == school,
                            onClick = { controller.update { it.copy(asr = school) } },
                            label = { Text(label) },
                        )
                    }
                }

                // Manual adjustment, minute by minute, to match the local mosque.
                SectionTitle(strings.offsets)
                Prayer.entries.forEach { prayer ->
                    val offset = settings.userOffsets[prayer] ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.prayerName(prayer), Modifier.weight(1f))
                        TextButton(onClick = { controller.update { it.withOffset(prayer, offset - 1) } }) { Text("−") }
                        Text(
                            if (offset > 0) "+$offset" else offset.toString(),
                            Modifier.width(40.dp),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        TextButton(onClick = { controller.update { it.withOffset(prayer, offset + 1) } }) { Text("+") }
                    }
                }
                Spacer(Modifier.heightIn(min = 16.dp))
            }
        }
    }

    if (pickingCity) {
        AlertDialog(
            onDismissRequest = { pickingCity = false },
            confirmButton = { TextButton(onClick = { pickingCity = false }) { Text("✕") } },
            title = { Text(strings.chooseCity) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    CITIES.forEach { city ->
                        Text(
                            city.name,
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controller.update { it.copy(place = city) }
                                    message = null
                                    pickingCity = false
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
        )
    }
}

private fun PrayerSettings.withOffset(prayer: Prayer, minutes: Int): PrayerSettings {
    val clamped = minutes.coerceIn(-30, 30)
    return copy(userOffsets = if (clamped == 0) userOffsets - prayer else userOffsets + (prayer to clamped))
}
