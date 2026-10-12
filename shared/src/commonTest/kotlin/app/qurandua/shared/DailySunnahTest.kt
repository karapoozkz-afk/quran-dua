package app.qurandua.shared

import app.qurandua.shared.daily.DailyContent
import app.qurandua.shared.daily.DailyHadith
import app.qurandua.shared.daily.DailySlot
import app.qurandua.shared.daily.dailyStringsFor
import app.qurandua.shared.daily.forLang
import app.qurandua.shared.daily.formatMinute
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DailySunnahTest {
    private fun hadith(n: Int) = DailyHadith("bukhari", n, title = mapOf("ru" to "t$n"), summary = mapOf("ru" to "s$n"), text = mapOf("ru" to "r", "en" to "e"))
    private val content = DailyContent(morning = (1..365).map(::hadith), evening = (1001..1365).map(::hadith))

    @Test
    fun oneHadithPerDayRepeatingEachYear() {
        assertEquals(1, content.forDay(LocalDate(2026, 1, 1), DailySlot.MORNING)?.number)
        assertEquals(1365, content.forDay(LocalDate(2026, 12, 31), DailySlot.EVENING)?.number)
        // Leap years have a 366th day: it starts the list again.
        assertEquals(1, content.forDay(LocalDate(2028, 12, 31), DailySlot.MORNING)?.number)
    }

    @Test
    fun textFallsBackToTheLanguageReadersKnow() {
        val text = mapOf("ru" to "ru", "en" to "en", "tr" to "tr")
        assertEquals("tr", text.forLang("tr"))
        assertEquals("ru", text.forLang("kk"))
        assertEquals("ru", text.forLang("uz"))
        assertEquals("en", text.forLang("de"))
        assertEquals("en", text.forLang("ar"))
    }

    @Test
    fun sourcesAndTimes() {
        val muslim = DailyHadith("muslim", 6200, book = 45, hadith = 120, title = emptyMap(), summary = emptyMap(), text = emptyMap())
        assertEquals("Сахих Муслим, кн. 45, № 120", dailyStringsFor("ru").source(muslim))
        assertEquals("Sahih al-Bukhari, 7", dailyStringsFor("en").source(hadith(7)))
        assertEquals("07:05", formatMinute(7 * 60 + 5))
    }
}
