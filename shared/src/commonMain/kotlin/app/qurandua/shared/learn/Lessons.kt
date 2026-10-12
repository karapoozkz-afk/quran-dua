package app.qurandua.shared.learn

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Lessons on purification and prayer (istinja, wudu, ghusl, tayammum, namaz), Hanafi madhhab,
 * following the ДУМК (muftyat.kz) guides. Every step names its evidence and grade, like the duas.
 */
@Serializable
data class Lesson(
    val id: String,
    val title: Map<String, String>,
    val steps: List<LessonStep>,
)

@Serializable
data class LessonStep(
    val text: Map<String, String>,
    /** What to say at this step; Quranic recitations carry only a reference, never typed Arabic. */
    val say: LessonSay? = null,
    val evidence: String,
    val grade: LessonGrade,
    val source: String,
    /** Prayer position drawn for this step (tools/gen_poses.py), e.g. "ruku" or "sujud_w". */
    val pose: String? = null,
)

@Serializable
data class LessonSay(
    val translit: String? = null,
    val ru: String? = null,
    val ref: String? = null,
)

@Serializable
enum class LessonGrade {
    @kotlinx.serialization.SerialName("quran") QURAN,
    @kotlinx.serialization.SerialName("sahih") SAHIH,
    @kotlinx.serialization.SerialName("hasan") HASAN,
    /** A ruling of the Hanafi school, not a direct text: shown so it is never mistaken for Sunnah. */
    @kotlinx.serialization.SerialName("fiqh_hanafi") FIQH_HANAFI,
    @kotlinx.serialization.SerialName("opinion") OPINION,
}

object LessonParser {
    private val json = Json { ignoreUnknownKeys = true }
    fun parse(text: String): List<Lesson> = json.decodeFromString(text)
}

/** The lessons are written in Russian; titles also exist in Kazakh. Other languages read Russian. */
fun Map<String, String>.inLanguage(lang: String): String = this[lang] ?: this["ru"] ?: values.first()

fun LessonGrade.label(lang: String): String = when (lang) {
    "kk" -> when (this) {
        LessonGrade.QURAN -> "Құран"; LessonGrade.SAHIH -> "Сахих хадис"; LessonGrade.HASAN -> "Хасан хадис"
        LessonGrade.FIQH_HANAFI -> "Ханафи фиқһы"; LessonGrade.OPINION -> "Ғалымдар пікірі"
    }
    "ky" -> when (this) {
        LessonGrade.QURAN -> "Куран"; LessonGrade.SAHIH -> "Сахих хадис"; LessonGrade.HASAN -> "Хасан хадис"
        LessonGrade.FIQH_HANAFI -> "Ханафий фикхы"; LessonGrade.OPINION -> "Аалымдардын пикири"
    }
    "uz" -> when (this) {
        LessonGrade.QURAN -> "Qur’on"; LessonGrade.SAHIH -> "Sahih hadis"; LessonGrade.HASAN -> "Hasan hadis"
        LessonGrade.FIQH_HANAFI -> "Hanafiy fiqhi"; LessonGrade.OPINION -> "Olimlar fikri"
    }
    "az" -> when (this) {
        LessonGrade.QURAN -> "Quran"; LessonGrade.SAHIH -> "Səhih hədis"; LessonGrade.HASAN -> "Həsən hədis"
        LessonGrade.FIQH_HANAFI -> "Hənəfi fiqhi"; LessonGrade.OPINION -> "Alimlərin rəyi"
    }
    "fr" -> when (this) {
        LessonGrade.QURAN -> "Coran"; LessonGrade.SAHIH -> "Hadith authentique"; LessonGrade.HASAN -> "Hadith bon (hasan)"
        LessonGrade.FIQH_HANAFI -> "Fiqh hanafite"; LessonGrade.OPINION -> "Avis des savants"
    }
    "de" -> when (this) {
        LessonGrade.QURAN -> "Koran"; LessonGrade.SAHIH -> "Sahih-Hadith"; LessonGrade.HASAN -> "Hasan-Hadith"
        LessonGrade.FIQH_HANAFI -> "Hanafitisches Fiqh"; LessonGrade.OPINION -> "Meinung der Gelehrten"
    }
    "tt" -> when (this) {
        LessonGrade.QURAN -> "Коръән"; LessonGrade.SAHIH -> "Сәхих хәдис"; LessonGrade.HASAN -> "Хәсән хәдис"
        LessonGrade.FIQH_HANAFI -> "Хәнәфи фикһе"; LessonGrade.OPINION -> "Галимнәр фикере"
    }
    "tg" -> when (this) {
        LessonGrade.QURAN -> "Қуръон"; LessonGrade.SAHIH -> "Ҳадиси саҳеҳ"; LessonGrade.HASAN -> "Ҳадиси ҳасан"
        LessonGrade.FIQH_HANAFI -> "Фиқҳи ҳанафӣ"; LessonGrade.OPINION -> "Андешаи уламо"
    }
    "ru" -> when (this) {
        LessonGrade.QURAN -> "Коран"; LessonGrade.SAHIH -> "Достоверный хадис"; LessonGrade.HASAN -> "Хороший хадис"
        LessonGrade.FIQH_HANAFI -> "Ханафитский фикх"; LessonGrade.OPINION -> "Мнение учёных"
    }
    else -> when (this) {
        LessonGrade.QURAN -> "Quran"; LessonGrade.SAHIH -> "Sahih hadith"; LessonGrade.HASAN -> "Hasan hadith"
        LessonGrade.FIQH_HANAFI -> "Hanafi fiqh"; LessonGrade.OPINION -> "Scholarly opinion"
    }
}

/** Short UI texts of the learning section. */
class LearnStrings(val title: String, val subtitle: String, val note: String, val say: String, val evidence: String, val source: String)

fun learnStringsFor(lang: String): LearnStrings = when (lang) {
    "kk" -> LearnStrings("Тахарат пен намазды үйрену", "Істінжа, дәрет, ғұсыл, намаз қадам-қадаммен",
        "Ханафи мазхабы. Мәтін орыс тілінде; имаммен тексеру күтілуде.", "Айтылады", "Дәлел", "Дереккөз")
    "ky" -> LearnStrings("Тахарат жана намазды үйрөнүү", "Истинжа, даарат, гусул, намаз кадам-кадам",
        "Ханафий мазхабы. Текст орусча; имам текшерүүсүн күтүүдө.", "Айтылат", "Далил", "Булак")
    "uz" -> LearnStrings("Tahorat va namozni o‘rganish", "Istinjo, tahorat, g‘usl, namoz bosqichma-bosqich",
        "Hanafiy mazhabi. Matn rus tilida; imom tekshiruvi kutilmoqda.", "Aytiladi", "Dalil", "Manba")
    "az" -> LearnStrings("Təharət və namazı öyrən", "İstinca, dəstəmaz, qüsl, namaz addım-addım", "Hənəfi məzhəbi. Mətn rus dilindədir; imamın yoxlamasını gözləyir.", "De", "Dəlil", "Mənbə")
    "fr" -> LearnStrings("Apprendre la purification et la prière", "Istinja, ablutions (woudou), grandes ablutions (ghousl), prière (salat) pas à pas", "Madhhab hanafite. Texte en russe ; en attente de relecture par un imam.", "Dites", "Preuve", "Source")
    "de" -> LearnStrings("Reinheit und Gebet lernen", "Istindscha, Wudu, Ghusl, Gebet Schritt für Schritt", "Hanafitische Rechtsschule. Text auf Russisch; wartet auf die Prüfung durch einen Imam.", "Sprich", "Beleg", "Quelle")
    "tt" -> LearnStrings("Пакьлекне һәм намазны өйрәнү", "Истинҗа, тәһарәт, гөсел, намаз адымлап", "Хәнәфи мәзһәбе. Текст рус телендә; имам тикшерүен көтә.", "Әйтегез", "Дәлил", "Чыганак")
    "tg" -> LearnStrings("Омӯзиши таҳорат ва намоз", "Истинҷо, таҳорат, ғусл, намоз қадам ба қадам", "Мазҳаби ҳанафӣ. Матн ба забони русӣ; интизори санҷиши имом.", "Бигӯед", "Далел", "Манбаъ")
    "ru" -> LearnStrings("Обучение тахарату и намазу", "Истинджа, омовение, гусль, намаз по шагам",
        "Ханафитский мазхаб. Ожидает проверки имамом.", "Произносится", "Доказательство", "Источник")
    else -> LearnStrings("Learn purification and prayer", "Istinja, wudu, ghusl, namaz step by step",
        "Hanafi madhhab. Text in Russian; awaiting review by an imam.", "Say", "Evidence", "Source")
}
