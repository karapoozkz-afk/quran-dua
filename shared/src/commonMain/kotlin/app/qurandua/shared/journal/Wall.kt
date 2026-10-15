package app.qurandua.shared.journal

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Ready-made replies under a post on the shared wall; there is no free-text comment. */
const val WALL_DUA_COUNT = 3

/** Longest post the server accepts (firestore.rules says the same). */
const val WALL_MAX_CHARS = 2000

/**
 * One approved post on the shared repentance wall. It carries no author: nothing that links two
 * posts to the same person is stored next to the text.
 */
data class WallPost(
    val id: String,
    val text: String,
    val kind: JournalKind,
    val createdAt: Long,
    /** How many people answered with each ready-made dua, indexed like [WallStrings.duas]. */
    val duas: List<Int>,
)

/**
 * Request bodies and response parsing for the Firestore REST API, kept free of HTTP so it is shared
 * and tested. Moderation happens in the Firebase console: a post becomes visible once its status is
 * changed from "pending" to "approved". The server side lives in /firebase (rules and indexes).
 */
object WallFirestore {
    private val json = Json { ignoreUnknownKeys = true }

    /** "projects/<id>/databases/(default)/documents" */
    fun root(projectId: String) = "projects/$projectId/databases/(default)/documents"

    /** Approved posts in one language, newest first. */
    fun postsQuery(lang: String, limit: Int = 50): String = buildJsonObject {
        putJsonObject("structuredQuery") {
            putJsonArray("from") { add(buildJsonObject { put("collectionId", "posts") }) }
            putJsonObject("where") {
                putJsonObject("compositeFilter") {
                    put("op", "AND")
                    putJsonArray("filters") {
                        add(equal("status", "approved"))
                        add(equal("lang", lang))
                    }
                }
            }
            putJsonArray("orderBy") {
                add(buildJsonObject {
                    putJsonObject("field") { put("fieldPath", "createdAt") }
                    put("direction", "DESCENDING")
                })
            }
            put("limit", limit)
        }
    }.toString()

    fun parsePosts(body: String): List<WallPost> =
        json.parseToJsonElement(body).jsonArray.mapNotNull { row ->
            val doc = row.jsonObject["document"]?.jsonObject ?: return@mapNotNull null
            val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
            val text = fields.string("text") ?: return@mapNotNull null
            WallPost(
                id = doc["name"]?.jsonPrimitive?.contentOrNull?.substringAfterLast('/') ?: return@mapNotNull null,
                text = text,
                kind = JournalKind.entries.firstOrNull { it.name == fields.string("kind") } ?: JournalKind.REGRET,
                createdAt = fields.timestamp("createdAt"),
                duas = (0 until WALL_DUA_COUNT).map { fields.int("d$it") },
            )
        }

    /** A new post, waiting for the moderator. The server stamps the time. */
    fun newPost(root: String, id: String, text: String, kind: JournalKind, lang: String): String = commit(
        buildJsonObject {
            putJsonObject("update") {
                put("name", "$root/posts/$id")
                putJsonObject("fields") {
                    putJsonObject("text") { put("stringValue", text.take(WALL_MAX_CHARS)) }
                    putJsonObject("kind") { put("stringValue", kind.name) }
                    putJsonObject("lang") { put("stringValue", lang) }
                    putJsonObject("status") { put("stringValue", "pending") }
                    for (i in 0 until WALL_DUA_COUNT) putJsonObject("d$i") { put("integerValue", "0") }
                }
            }
            putJsonObject("currentDocument") { put("exists", false) }
            putJsonArray("updateTransforms") { add(serverTime("createdAt")) }
        },
    )

    /** One dua under a post: a marker doc for this user (once per post) plus the counter, in one commit. */
    fun dua(root: String, postId: String, uid: String, index: Int): String = commit(
        buildJsonObject {
            putJsonObject("update") {
                put("name", "$root/posts/$postId/duas/$uid")
                putJsonObject("fields") { putJsonObject("kind") { put("integerValue", index.toString()) } }
            }
            putJsonObject("currentDocument") { put("exists", false) }
        },
        buildJsonObject {
            putJsonObject("transform") {
                put("document", "$root/posts/$postId")
                putJsonArray("fieldTransforms") {
                    add(buildJsonObject {
                        put("fieldPath", "d$index")
                        putJsonObject("increment") { put("integerValue", "1") }
                    })
                }
            }
            putJsonObject("currentDocument") { put("exists", true) }
        },
    )

    /** A report for the moderator; one per person per post. */
    fun report(root: String, postId: String, uid: String): String = commit(
        buildJsonObject {
            putJsonObject("update") {
                put("name", "$root/reports/${postId}_$uid")
                putJsonObject("fields") { putJsonObject("post") { put("stringValue", postId) } }
            }
            putJsonObject("currentDocument") { put("exists", false) }
            putJsonArray("updateTransforms") { add(serverTime("createdAt")) }
        },
    )

    /** Random document id, the same shape Firestore makes. */
    fun newId(random: kotlin.random.Random = kotlin.random.Random.Default): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun commit(vararg writes: JsonObject): String =
        buildJsonObject { put("writes", JsonArray(writes.toList())) }.toString()

    private fun equal(field: String, value: String) = buildJsonObject {
        putJsonObject("fieldFilter") {
            putJsonObject("field") { put("fieldPath", field) }
            put("op", "EQUAL")
            putJsonObject("value") { put("stringValue", value) }
        }
    }

    private fun serverTime(field: String) = buildJsonObject {
        put("fieldPath", field)
        put("setToServerValue", "REQUEST_TIME")
    }

    private fun JsonObject.string(name: String): String? =
        this[name]?.jsonObject?.get("stringValue")?.jsonPrimitive?.contentOrNull

    private fun JsonObject.int(name: String): Int =
        this[name]?.jsonObject?.get("integerValue")?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0

    private fun JsonObject.timestamp(name: String): Long =
        this[name]?.jsonObject?.get("timestampValue")?.jsonPrimitive?.contentOrNull
            ?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() } ?: 0L
}

/** Texts of the shared wall. Machine-written outside ru/en; awaiting native review. */
class WallStrings(
    val tabWall: String,
    val tabMine: String,
    val notice: String,
    val hint: String,
    val send: String,
    val sent: String,
    val duas: List<String>,
    val report: String,
    val reportConfirm: String,
    val reported: String,
    val empty: String,
    val offline: String,
    val retry: String,
    val notReady: String,
)

fun wallStringsFor(lang: String): WallStrings = when (lang) {
    "ru" -> WallStrings(
        "Общее", "Личное",
        "Пишите без имён: ни своих, ни чужих. Каждую запись проверяют перед публикацией. Отвечать можно только дуа. Записи хранятся на сервере Google Firebase без имени и номера телефона.",
        "Чем хотите поделиться?", "Отправить на проверку", "Отправлено. Запись появится после проверки.",
        listOf("Да простит тебя Аллах", "Да облегчит тебе Аллах", "Дуа за тебя"),
        "Пожаловаться", "Пожаловаться на эту запись? Модератор её проверит.", "Жалоба отправлена",
        "Пока здесь нет записей на вашем языке.", "Нет связи с сервером.", "Повторить", "Общая стена скоро откроется.",
    )
    "kk" -> WallStrings(
        "Ортақ", "Жеке",
        "Есімсіз жазыңыз: өзіңіздікін де, басқанікін де. Әр жазба жарияланар алдында тексеріледі. Тек дұғамен жауап беруге болады. Жазбалар Google Firebase серверінде атсыз және телефон нөмірінсіз сақталады.",
        "Немен бөліскіңіз келеді?", "Тексеруге жіберу", "Жіберілді. Жазба тексерістен кейін шығады.",
        listOf("Алла сені кешірсін", "Алла саған жеңілдік берсін", "Саған дұға еттім"),
        "Шағым айту", "Бұл жазбаға шағым айтасыз ба? Модератор тексереді.", "Шағым жіберілді",
        "Әзірге сіздің тіліңізде жазба жоқ.", "Сервермен байланыс жоқ.", "Қайталау", "Ортақ қабырға жақында ашылады.",
    )
    "ky" -> WallStrings(
        "Жалпы", "Жеке",
        "Ысымсыз жазыңыз: өзүңүздүкүн да, башкалардыкын да. Ар бир жазуу жарыяланардан мурун текшерилет. Дуба менен гана жооп берсе болот. Жазуулар Google Firebase серверинде атсыз жана телефон номерисиз сакталат.",
        "Эмне менен бөлүшкүңүз келет?", "Текшерүүгө жөнөтүү", "Жөнөтүлдү. Жазуу текшерүүдөн кийин чыгат.",
        listOf("Алла сени кечирсин", "Алла сага жеңилдик берсин", "Сага дуба кылдым"),
        "Даттануу", "Бул жазууга даттанасызбы? Модератор текшерет.", "Даттануу жөнөтүлдү",
        "Азырынча сиздин тилиңизде жазуу жок.", "Сервер менен байланыш жок.", "Кайталоо", "Жалпы дубал жакында ачылат.",
    )
    "uz" -> WallStrings(
        "Umumiy", "Shaxsiy",
        "Ismsiz yozing: na o‘zingiznikini, na boshqalarnikini. Har bir yozuv e’lon qilinishidan oldin tekshiriladi. Faqat duo bilan javob berish mumkin. Yozuvlar Google Firebase serverida ism va telefon raqamisiz saqlanadi.",
        "Nima bilan bo‘lishmoqchisiz?", "Tekshiruvga yuborish", "Yuborildi. Yozuv tekshiruvdan so‘ng chiqadi.",
        listOf("Alloh seni kechirsin", "Alloh senga oson qilsin", "Sen uchun duo qildim"),
        "Shikoyat qilish", "Bu yozuv ustidan shikoyat qilasizmi? Moderator tekshiradi.", "Shikoyat yuborildi",
        "Hozircha sizning tilingizda yozuv yo‘q.", "Server bilan aloqa yo‘q.", "Qayta urinish", "Umumiy devor tez orada ochiladi.",
    )
    "tg" -> WallStrings(
        "Умумӣ", "Шахсӣ",
        "Бе ном нависед: на номи худ, на номи дигарон. Ҳар навишта пеш аз нашр санҷида мешавад. Танҳо бо дуо ҷавоб додан мумкин аст. Навиштаҳо дар сервери Google Firebase бе ном ва рақами телефон нигоҳ дошта мешаванд.",
        "Бо чӣ мубодила кардан мехоҳед?", "Ба санҷиш фиристодан", "Фиристода шуд. Навишта пас аз санҷиш пайдо мешавад.",
        listOf("Худо туро бубахшад", "Худо барои ту осон гардонад", "Барои ту дуо кардам"),
        "Шикоят кардан", "Аз ин навишта шикоят мекунед? Модератор онро месанҷад.", "Шикоят фиристода шуд",
        "Ҳоло ба забони шумо навишта нест.", "Бо сервер пайваст нест.", "Такрор", "Девори умумӣ ба наздикӣ кушода мешавад.",
    )
    "tt" -> WallStrings(
        "Гомуми", "Шәхси",
        "Исемнәрсез языгыз: үзегезнекен дә, башкаларныкын да. Һәр язма бастырылганчы тикшерелә. Бары тик дога белән җавап бирергә була. Язмалар Google Firebase серверында исемсез һәм телефон номерыннан башка саклана.",
        "Нәрсә белән уртаклашасыгыз килә?", "Тикшерүгә җибәрү", "Җибәрелде. Язма тикшерүдән соң чыгачак.",
        listOf("Аллаһ сине гафу итсен", "Аллаһ сиңа җиңеллек бирсен", "Синең өчен дога кылдым"),
        "Шикаять итү", "Бу язмага шикаять итәсезме? Модератор тикшерер.", "Шикаять җибәрелде",
        "Әлегә сезнең телдә язма юк.", "Сервер белән элемтә юк.", "Кабатлау", "Гомуми стена тиздән ачылачак.",
    )
    "az" -> WallStrings(
        "Ümumi", "Şəxsi",
        "Adsız yazın: nə öz adınızı, nə başqalarının. Hər qeyd dərc olunmazdan əvvəl yoxlanılır. Yalnız dua ilə cavab vermək olar. Qeydlər Google Firebase serverində adsız və telefon nömrəsiz saxlanılır.",
        "Nəyi bölüşmək istəyirsiniz?", "Yoxlamaya göndər", "Göndərildi. Qeyd yoxlamadan sonra görünəcək.",
        listOf("Allah səni bağışlasın", "Allah sənə asanlıq versin", "Sənin üçün dua etdim"),
        "Şikayət et", "Bu qeyddən şikayət edirsiniz? Moderator yoxlayacaq.", "Şikayət göndərildi",
        "Hələ sizin dildə qeyd yoxdur.", "Serverlə əlaqə yoxdur.", "Təkrar et", "Ümumi divar tezliklə açılacaq.",
    )
    "tr" -> WallStrings(
        "Ortak", "Kişisel",
        "İsim yazmayın: ne kendinizin ne başkalarının. Her not yayımlanmadan önce incelenir. Yalnızca dua ile karşılık verilebilir. Notlar Google Firebase sunucusunda isim ve telefon numarası olmadan saklanır.",
        "Ne paylaşmak istersiniz?", "İncelemeye gönder", "Gönderildi. Not incelemeden sonra görünecek.",
        listOf("Allah seni affetsin", "Allah sana kolaylık versin", "Senin için dua ettim"),
        "Şikâyet et", "Bu notu şikâyet etmek istiyor musunuz? Moderatör inceleyecek.", "Şikâyet gönderildi",
        "Henüz sizin dilinizde not yok.", "Sunucuya bağlanılamıyor.", "Tekrar dene", "Ortak duvar yakında açılacak.",
    )
    "fr" -> WallStrings(
        "Commun", "Personnel",
        "N’écrivez aucun nom, ni le vôtre ni celui d’autrui. Chaque note est vérifiée avant publication. On ne peut répondre que par une invocation. Les notes sont stockées sur un serveur Google Firebase, sans nom ni numéro de téléphone.",
        "Que voulez-vous partager ?", "Envoyer pour vérification", "Envoyé. La note paraîtra après vérification.",
        listOf("Qu’Allah te pardonne", "Qu’Allah te facilite", "J’ai invoqué pour toi"),
        "Signaler", "Signaler cette note ? Un modérateur la vérifiera.", "Signalement envoyé",
        "Aucune note dans votre langue pour l’instant.", "Pas de connexion au serveur.", "Réessayer", "Le mur commun ouvrira bientôt.",
    )
    "de" -> WallStrings(
        "Gemeinsam", "Persönlich",
        "Schreiben Sie keine Namen, weder Ihren noch fremde. Jede Notiz wird vor der Veröffentlichung geprüft. Antworten sind nur mit einem Bittgebet möglich. Die Notizen liegen auf einem Google-Firebase-Server, ohne Namen und Telefonnummer.",
        "Was möchten Sie teilen?", "Zur Prüfung senden", "Gesendet. Die Notiz erscheint nach der Prüfung.",
        listOf("Möge Allah dir vergeben", "Möge Allah es dir leicht machen", "Ich habe für dich gebetet"),
        "Melden", "Diese Notiz melden? Ein Moderator prüft sie.", "Meldung gesendet",
        "Noch keine Notizen in Ihrer Sprache.", "Keine Verbindung zum Server.", "Erneut versuchen", "Die gemeinsame Wand öffnet bald.",
    )
    "es" -> WallStrings(
        "Común", "Personal",
        "No escribas nombres, ni el tuyo ni los de otros. Cada nota se revisa antes de publicarse. Solo se puede responder con una dua. Las notas se guardan en un servidor de Google Firebase, sin nombre ni número de teléfono.",
        "¿Qué quieres compartir?", "Enviar a revisión", "Enviado. La nota aparecerá tras la revisión.",
        listOf("Que Allah te perdone", "Que Allah te lo facilite", "He hecho dua por ti"),
        "Denunciar", "¿Denunciar esta nota? Un moderador la revisará.", "Denuncia enviada",
        "Aún no hay notas en tu idioma.", "No hay conexión con el servidor.", "Reintentar", "El muro común abrirá pronto.",
    )
    "ar" -> WallStrings(
        "العام", "الخاص",
        "لا تكتب أسماء، لا اسمك ولا أسماء غيرك. تُراجَع كل ملاحظة قبل نشرها. لا يمكن الرد إلا بالدعاء. تُحفظ الملاحظات على خادم Google Firebase دون اسم أو رقم هاتف.",
        "ماذا تريد أن تشارك؟", "إرسال للمراجعة", "أُرسلت. ستظهر الملاحظة بعد المراجعة.",
        listOf("غفر الله لك", "يسّر الله لك", "دعوت لك"),
        "إبلاغ", "الإبلاغ عن هذه الملاحظة؟ سيراجعها المشرف.", "أُرسل البلاغ",
        "لا ملاحظات بلغتك بعد.", "لا اتصال بالخادم.", "إعادة المحاولة", "سيُفتح الجدار العام قريبًا.",
    )
    "id" -> WallStrings(
        "Bersama", "Pribadi",
        "Jangan menulis nama, baik nama Anda maupun orang lain. Setiap catatan diperiksa sebelum diterbitkan. Balasan hanya berupa doa. Catatan disimpan di server Google Firebase tanpa nama dan nomor telepon.",
        "Apa yang ingin Anda bagikan?", "Kirim untuk diperiksa", "Terkirim. Catatan akan muncul setelah diperiksa.",
        listOf("Semoga Allah mengampunimu", "Semoga Allah memudahkanmu", "Aku mendoakanmu"),
        "Laporkan", "Laporkan catatan ini? Moderator akan memeriksanya.", "Laporan terkirim",
        "Belum ada catatan dalam bahasa Anda.", "Tidak ada koneksi ke server.", "Coba lagi", "Dinding bersama segera dibuka.",
    )
    "ur" -> WallStrings(
        "عام", "ذاتی",
        "نام نہ لکھیں، نہ اپنا نہ کسی اور کا۔ ہر نوٹ شائع ہونے سے پہلے جانچا جاتا ہے۔ جواب صرف دعا سے دیا جا سکتا ہے۔ نوٹس Google Firebase کے سرور پر بغیر نام اور فون نمبر کے محفوظ ہوتے ہیں۔",
        "آپ کیا بتانا چاہتے ہیں؟", "جانچ کے لیے بھیجیں", "بھیج دیا گیا۔ جانچ کے بعد نوٹ نظر آئے گا۔",
        listOf("اللہ آپ کو معاف کرے", "اللہ آپ کے لیے آسانی کرے", "میں نے آپ کے لیے دعا کی"),
        "رپورٹ کریں", "اس نوٹ کی رپورٹ کریں؟ منتظم اسے دیکھے گا۔", "رپورٹ بھیج دی گئی",
        "ابھی آپ کی زبان میں کوئی نوٹ نہیں۔", "سرور سے رابطہ نہیں۔", "دوبارہ کوشش کریں", "عام دیوار جلد کھلے گی۔",
    )
    else -> WallStrings(
        "Shared", "Personal",
        "Write no names, neither yours nor anyone else’s. Every note is reviewed before it is published. You can answer only with a dua. Notes are stored on a Google Firebase server without a name or phone number.",
        "What would you like to share?", "Send for review", "Sent. The note will appear after review.",
        listOf("May Allah forgive you", "May Allah make it easy for you", "I made dua for you"),
        "Report", "Report this note? A moderator will review it.", "Report sent",
        "No notes in your language yet.", "No connection to the server.", "Retry", "The shared wall opens soon.",
    )
}
