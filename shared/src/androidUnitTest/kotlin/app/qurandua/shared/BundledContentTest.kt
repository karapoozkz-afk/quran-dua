package app.qurandua.shared

import app.qurandua.shared.content.ContentParser
import app.qurandua.shared.data.ContentRepository
import app.qurandua.shared.model.ItemKind
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs the search over the real bundled content, as the app ships it. */
class BundledContentTest {

    private fun asset(name: String): String {
        val candidates = listOf("../androidApp/src/main/assets/content/$name", "androidApp/src/main/assets/content/$name")
        val file = candidates.map(::File).first { it.exists() }
        return file.readText()
    }

    private val repo = ContentRepository.fromJson(asset("situations.json"), asset("duas.json"))

    private fun topSituation(query: String): String? = repo.search.search(query).situations.firstOrNull()?.id

    @Test
    fun lessonsParseAndEveryStepHasEvidence() {
        val lessons = app.qurandua.shared.learn.LessonParser.parse(asset("lessons.json"))
        assertTrue(lessons.isNotEmpty())
        for (l in lessons) for (step in l.steps) {
            assertTrue(step.evidence.isNotBlank() && step.source.startsWith("https://"), l.id)
        }
    }

    @Test
    fun everySituationHasContent() {
        for (s in repo.situations) assertTrue(repo.duasFor(s.id).isNotEmpty(), s.id)
    }

    @Test
    fun everyItemIsTranslatedIntoEveryContentLanguage() {
        for (lang in app.qurandua.shared.data.CONTENT_LANGUAGES) {
            for (s in repo.situations) assertTrue(!s.title[lang].isNullOrBlank(), "${s.id} title $lang")
            for (d in repo.duas) {
                assertTrue(!d.translation[lang].isNullOrBlank(), "${d.id} translation $lang")
                assertTrue(!d.source[lang].isNullOrBlank(), "${d.id} source $lang")
            }
        }
    }

    @Test
    fun recitableDuasHaveArabicAndSource() {
        for (d in repo.duas.filter { it.kind == ItemKind.DUA }) {
            assertTrue(!d.arabic.isNullOrBlank(), d.id)
            assertTrue(d.source.values.all { it.isNotBlank() }, d.id)
        }
    }

    @Test
    fun understandsEverydayQueries() {
        // query -> situations accepted as the top hit
        val expected = mapOf(
            "что читать от долгов" to setOf("debts"),
            "у меня большой долг" to setOf("debts"),
            "что читать за умершего отца" to setOf("deceased"),
            "что читать возле могилы" to setOf("cemetery"),
            "какую дуа читать перед дорогой" to setOf("travel"),
            "не могу заснуть ночью" to setOf("sleep"),
            "боюсь будущего" to setOf("anxiety"),
            "что читать если заболел" to setOf("illness"),
            "хочу чтобы Аллах простил мои грехи" to setOf("forgiveness"),
            "хочу найти хорошую жену" to setOf("marriage"),
            "хочу детей" to setOf("children"),
            "что читать когда денег нет" to setOf("rizq"),
            "у меня проблемы с родителями" to setOf("family", "parents"),
            "день рождения" to setOf("birthday"),
            "что читать при входе в туалет" to setOf("toilet"),
            "идёт сильный дождь" to setOf("weather"),
            "гром и молния" to setOf("weather"),
            "защита от сглаза" to setOf("evil_eye"),
            "дуа за маму и папу" to setOf("parents"),
            "дуа за палестину" to setOf("oppression"),
            "меня обидели несправедливо" to setOf("oppression"),
            "скоро роды" to setOf("newborn"),
            "можно ли печь 7 шелпек" to setOf("customs"),
            "красная нить от сглаза" to setOf("customs", "evil_eye"),
            "как выразить соболезнование" to setOf("death_news"),
            "что читать если закрыта дорога" to setOf("hardship"),
            "нет удачи ничего не получается" to setOf("hardship"),
            "аяты от долгов" to setOf("debts"),
            "что читать в хадже" to setOf("hajj"),
            "bad luck nothing works" to setOf("hardship"),
            "talbiyah for umrah" to setOf("hajj"),
            "жолым жабық" to setOf("hardship"),
            "қажылыққа барамын" to setOf("hajj"),
            "дуа чтобы найти потерянную вещь" to setOf("lost_item"),
            "какую суру читать чтобы исполнилось желание" to setOf("wish"),
            "дуа чтобы муж любил жену" to setOf("spouses"),
            "как помириться с человеком" to setOf("reconcile"),
            "какую суру читать чтобы забеременеть" to setOf("conceive"),
            "дуа чтобы муж не пил" to setOf("addiction"),
            "дуа от лени" to setOf("laziness"),
            "дуа у зеркала" to setOf("appearance"),
            "I lost my keys" to setOf("lost_item"),
            "dua to get pregnant" to setOf("conceive"),
            "жоғалған затты табу дұғасы" to setOf("lost_item"),
            "жүкті болу үшін оқылатын сүре" to setOf("conceive"),
            "I have a lot of debt" to setOf("debts"),
            "dua for my late father" to setOf("deceased"),
            "visiting the grave" to setOf("cemetery"),
            "I can't sleep" to setOf("sleep"),
            "rain and thunder" to setOf("weather"),
            "protection from evil eye" to setOf("evil_eye"),
            "dua for parents" to setOf("parents"),
            "қарыздан құтылу дұғасы" to setOf("debts"),
            "марқұм әкем үшін дұға" to setOf("deceased"),
            "қабір басында не оқу керек" to setOf("cemetery"),
            "ұйқым келмейді" to setOf("sleep"),
            "сапарға шығар алдында" to setOf("travel"),
            "зекет кімге беріледі" to setOf("zakat"),
            "сколько платить закят" to setOf("zakat"),
            "дәретханаға кіргенде" to setOf("toilet"),
            "балама көз тиді" to setOf("evil_eye"),
            "жеті шелпек" to setOf("customs"),
        ) + LOCALIZED_QUERIES
        val failures = expected.mapNotNull { (query, ids) ->
            val actual = topSituation(query)
            if (actual in ids) null else "\"$query\" -> $actual (expected $ids)"
        }
        assertEquals(emptyList(), failures)
    }

    @Test
    fun adviceWithoutSourceIsNeverOfferedAsADuaToRecite() {
        val offenders = repo.duas.filter { it.grade == app.qurandua.shared.model.Grade.NOSOURCE && it.kind != ItemKind.INFO }
        assertEquals(emptyList(), offenders.map { it.id })
    }

    @Test
    fun debtQueryReturnsDebtDuasFirst() {
        val duas = repo.search.search("у меня много долгов").duas.take(3).map { it.id }
        assertTrue("debt_halal" in duas, duas.toString())
    }

    @Test
    fun quranAssetIsComplete() {
        val quran = ContentParser.quran(asset("quran.json"))
        assertEquals(114, quran.surahs.size)
        assertEquals(6236, quran.surahs.sumOf { it.ayahs.size })
        assertEquals(app.qurandua.shared.data.QURAN_TRANSLATION_LANGUAGES, quran.langs)
        assertTrue(quran.surahs.all { s -> s.ayahs.all { it.size == 2 + quran.langs.size && it.all(String::isNotBlank) } })
    }

    @Test
    fun deviceLanguagesMapToUiLanguages() {
        assertEquals("id", app.qurandua.shared.data.supportedUiLanguage("in"))
        assertEquals("ur", app.qurandua.shared.data.supportedUiLanguage("ur_PK"))
        assertEquals("tr", app.qurandua.shared.data.supportedUiLanguage("tr-TR"))
        assertEquals("es", app.qurandua.shared.data.supportedUiLanguage("es_MX"))
        assertEquals("ky", app.qurandua.shared.data.supportedUiLanguage("ky_KG"))
        assertEquals("uz", app.qurandua.shared.data.supportedUiLanguage("uz-Latn-UZ"))
        assertEquals("", app.qurandua.shared.data.supportedUiLanguage("de"))
    }

    private companion object {
        /** Spanish, Turkish, Indonesian, Urdu, Kyrgyz and Uzbek queries, filled in with those languages. */
        val LOCALIZED_QUERIES: Map<String, Set<String>> = mapOf(
            // tr
            "borçtan kurtulmak için dua" to setOf("debts"),
            "borc duasi" to setOf("debts"),
            "kabir ziyaretinde ne okunur" to setOf("cemetery"),
            "uyuyamıyorum" to setOf("sleep"),
            "nazar duası" to setOf("evil_eye"),
            "yağmur yağarken" to setOf("weather"),
            "anne baba için dua" to setOf("parents"),
            "şansım yok işlerim ters gidiyor" to setOf("hardship"),
            // id
            "doa agar terhindar dari hutang" to setOf("debts"),
            "doa ziarah kubur" to setOf("cemetery"),
            "tidak bisa tidur" to setOf("sleep"),
            "doa masuk kamar mandi" to setOf("toilet"),
            "doa ketika hujan" to setOf("weather"),
            "doa untuk orang tua" to setOf("parents"),
            "doa melahirkan lancar" to setOf("newborn"),
            "doa agar tidak sial" to setOf("hardship"),
            // es
            "dua para pagar las deudas" to setOf("debts"),
            "que leer en el cementerio" to setOf("cemetery"),
            "no puedo dormir" to setOf("sleep"),
            "dua para mis padres" to setOf("parents"),
            "proteccion contra el mal de ojo" to setOf("evil_eye"),
            "perdi mis llaves" to setOf("lost_item"),
            "dua para el viaje" to setOf("travel"),
            "murio mi padre" to setOf("deceased", "death_news"),
            // ur
            "قرض سے نجات کی دعا" to setOf("debts"),
            "qarz ki dua" to setOf("debts"),
            "قبر پر کیا پڑھیں" to setOf("cemetery"),
            "نیند نہیں آتی" to setOf("sleep"),
            "نظر بد سے حفاظت" to setOf("evil_eye"),
            "بارش کی دعا" to setOf("weather"),
            "والدین کے لیے دعا" to setOf("parents"),
            "بندش کھولنے کی دعا" to setOf("hardship"),
            // ky
            "карызым көп" to setOf("debts"),
            "карыздан кутулуу дубасы" to setOf("debts"),
            "маркум атам үчүн дуба" to setOf("deceased"),
            "мүрзөгө зыярат" to setOf("cemetery"),
            "уктай албай жатам" to setOf("sleep"),
            "көз тийүүдөн коргонуу" to setOf("evil_eye", "protection"),
            "ата-эне үчүн дуба" to setOf("parents", "family"),
            "жолум жабык" to setOf("hardship"),
            // uz (typed with a plain apostrophe, as on most keyboards)
            "qarzim ko'p" to setOf("debts"),
            "qarzdan qutulish duosi" to setOf("debts"),
            "otam vafot etdi" to setOf("deceased", "death_news"),
            "qabr ziyorati" to setOf("cemetery"),
            "uxlay olmayapman" to setOf("sleep"),
            "ko'z tegishidan himoya" to setOf("evil_eye", "protection"),
            "ota-ona uchun duo" to setOf("parents", "family"),
            "yomg'ir yog'yapti" to setOf("weather"),
        )
    }
}
