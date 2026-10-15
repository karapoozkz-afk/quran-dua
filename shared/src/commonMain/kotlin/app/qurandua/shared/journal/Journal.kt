package app.qurandua.shared.journal

import kotlinx.serialization.Serializable

/** What a note in the repentance journal is about; the person picks it when writing. */
@Serializable
enum class JournalKind { REGRET, GRUDGE, REPENTANCE }

/** One private note. Lives only on the phone (androidApp platform/JournalPlatform.kt). */
@Serializable
data class JournalEntry(
    val id: Long,
    val createdAt: Long,
    val kind: JournalKind,
    val text: String,
)

/** Everything the journal keeps, stored as one encrypted file. */
@Serializable
data class JournalData(val entries: List<JournalEntry> = emptyList())

/** PIN: digits only, 4 to 8 of them. */
fun isValidPin(pin: String): Boolean = pin.length in 4..8 && pin.all { it in '0'..'9' }

/** Texts of the repentance journal. Machine-written outside ru/en; awaiting native review. */
class JournalStrings(
    val title: String,
    val subtitle: String,
    val privacy: String,
    val hint: String,
    val regret: String,
    val grudge: String,
    val repentance: String,
    val save: String,
    val cancel: String,
    val edit: String,
    val delete: String,
    val deleteConfirm: String,
    val empty: String,
    val duasTitle: String,
    val protection: String,
    val pinLock: String,
    val newPin: String,
    val repeatPin: String,
    val enterPin: String,
    val wrongPin: String,
    val pinMismatch: String,
    val unlock: String,
    val forgotPin: String,
    val forgotBody: String,
    val deleteAll: String,
    val deleteAllConfirm: String,
    /** %1 = seconds left. */
    val tooManyTries: String,
    val entriesTitle: String,
) {
    fun kind(kind: JournalKind): String = when (kind) {
        JournalKind.REGRET -> regret
        JournalKind.GRUDGE -> grudge
        JournalKind.REPENTANCE -> repentance
    }
}

fun journalStringsFor(lang: String): JournalStrings = when (lang) {
    "ru" -> JournalStrings(
        "Дневник покаяния", "Сожаления, обиды и покаяние. Только для вас",
        "Записи видите только вы. Они хранятся на этом телефоне в зашифрованном виде, никуда не отправляются и не попадают в резервную копию. Аккаунт не нужен. Если удалить приложение, записи исчезнут вместе с ним.",
        "Что у вас на сердце?", "Сожаление", "Обида", "Покаяние",
        "Сохранить", "Отмена", "Изменить", "Удалить", "Удалить эту запись навсегда?",
        "Здесь пока пусто. Напишите то, о чём жалеете, что держите на сердце или в чём каетесь.",
        "Дуа о прощении", "Защита", "Вход по PIN-коду", "Придумайте PIN из 4–8 цифр", "Повторите PIN",
        "Введите PIN", "Неверный PIN", "PIN-коды не совпадают", "Открыть", "Забыли PIN?",
        "PIN нельзя восстановить. Можно удалить все записи вместе с PIN-кодом и начать заново.",
        "Удалить все записи", "Удалить все записи и PIN-код безвозвратно?",
        "Слишком много попыток. Подождите %1 с", "Мои записи",
    )
    "kk" -> JournalStrings(
        "Тәубе күнделігі", "Өкініш, өкпе және тәубе. Тек сіз үшін",
        "Жазбаларды тек сіз көресіз. Олар осы телефонда шифрланып сақталады, ешқайда жіберілмейді және сақтық көшірмеге түспейді. Аккаунт қажет емес. Қолданбаны өшірсеңіз, жазбалар да жойылады.",
        "Жүрегіңізде не бар?", "Өкініш", "Өкпе", "Тәубе",
        "Сақтау", "Болдырмау", "Өзгерту", "Жою", "Бұл жазбаны біржола жоясыз ба?",
        "Әзірге бос. Өкінгеніңізді, жүрегіңізде жүргенін немесе неден тәубе еткеніңізді жазыңыз.",
        "Кешірім сұрау дұғалары", "Қорғау", "PIN-кодпен кіру", "4–8 саннан тұратын PIN ойлап табыңыз", "PIN-ды қайталаңыз",
        "PIN енгізіңіз", "PIN қате", "PIN-кодтар сәйкес емес", "Ашу", "PIN-ды ұмыттыңыз ба?",
        "PIN-ды қалпына келтіру мүмкін емес. Барлық жазбаны PIN-кодпен бірге жойып, қайта бастауға болады.",
        "Барлық жазбаны жою", "Барлық жазба мен PIN-кодты біржола жоясыз ба?",
        "Тым көп әрекет. %1 с күтіңіз", "Менің жазбаларым",
    )
    "ky" -> JournalStrings(
        "Тообо күндөлүгү", "Өкүнүч, таарыныч жана тообо. Сиз үчүн гана",
        "Жазууларды сиз гана көрөсүз. Алар ушул телефондо шифрленип сакталат, эч жакка жөнөтүлбөйт жана камдык көчүрмөгө кирбейт. Аккаунт керек эмес. Колдонмону өчүрсөңүз, жазуулар да жок болот.",
        "Жүрөгүңүздө эмне бар?", "Өкүнүч", "Таарыныч", "Тообо",
        "Сактоо", "Жокко чыгаруу", "Өзгөртүү", "Өчүрүү", "Бул жазууну биротоло өчүрөсүзбү?",
        "Азырынча бош. Эмнеге өкүнгөнүңүздү, жүрөгүңүздө эмне жүргөнүн же эмнеден тообо кылганыңызды жазыңыз.",
        "Кечирим сурануу дубалары", "Коргоо", "PIN-код менен кирүү", "4–8 сандан турган PIN ойлоп табыңыз", "PIN'ди кайталаңыз",
        "PIN киргизиңиз", "PIN туура эмес", "PIN-коддор дал келбейт", "Ачуу", "PIN'ди унуттуңузбу?",
        "PIN'ди калыбына келтирүүгө болбойт. Бардык жазууларды PIN-код менен кошо өчүрүп, кайра баштасаңыз болот.",
        "Бардык жазууларды өчүрүү", "Бардык жазуулар менен PIN-кодду биротоло өчүрөсүзбү?",
        "Өтө көп аракет. %1 с күтүңүз", "Менин жазууларым",
    )
    "uz" -> JournalStrings(
        "Tavba kundaligi", "Afsuslar, gina va tavba. Faqat siz uchun",
        "Yozuvlarni faqat siz ko‘rasiz. Ular shu telefonda shifrlangan holda saqlanadi, hech qayerga yuborilmaydi va zaxira nusxaga tushmaydi. Akkaunt kerak emas. Ilovani o‘chirsangiz, yozuvlar ham yo‘qoladi.",
        "Qalbingizda nima bor?", "Afsus", "Gina", "Tavba",
        "Saqlash", "Bekor qilish", "Tahrirlash", "O‘chirish", "Bu yozuv butunlay o‘chirilsinmi?",
        "Hozircha bo‘sh. Nimadan afsuslanayotganingizni, qalbingizda nima borligini yoki nimadan tavba qilganingizni yozing.",
        "Mag‘firat so‘rash duolari", "Himoya", "PIN-kod bilan kirish", "4–8 raqamdan iborat PIN o‘ylab toping", "PIN-ni takrorlang",
        "PIN-ni kiriting", "PIN noto‘g‘ri", "PIN-kodlar mos kelmadi", "Ochish", "PIN-ni unutdingizmi?",
        "PIN-ni tiklab bo‘lmaydi. Barcha yozuvlarni PIN-kod bilan birga o‘chirib, qaytadan boshlash mumkin.",
        "Barcha yozuvlarni o‘chirish", "Barcha yozuvlar va PIN-kod butunlay o‘chirilsinmi?",
        "Juda ko‘p urinish. %1 soniya kuting", "Mening yozuvlarim",
    )
    "tg" -> JournalStrings(
        "Рӯзномаи тавба", "Пушаймонӣ, ранҷиш ва тавба. Танҳо барои шумо",
        "Ин навиштаҳоро танҳо шумо мебинед. Онҳо дар ҳамин телефон рамзгузорӣ шуда нигоҳ дошта мешаванд, ба ҳеҷ ҷо фиристода намешаванд ва ба нусхаи эҳтиётӣ намераванд. Ҳисоб лозим нест. Агар барномаро нест кунед, навиштаҳо низ нест мешаванд.",
        "Дар дилатон чӣ ҳаст?", "Пушаймонӣ", "Ранҷиш", "Тавба",
        "Нигоҳ доштан", "Бекор кардан", "Тағйир додан", "Нест кардан", "Ин навиштаро тамоман нест мекунед?",
        "Ҳоло холӣ аст. Аз чӣ пушаймонед, чӣ дар дил доред ё аз чӣ тавба кардед, нависед.",
        "Дуоҳои талаби омурзиш", "Ҳимоя", "Даромад бо PIN-рамз", "PIN аз 4–8 рақам фикр кунед", "PIN-ро такрор кунед",
        "PIN-ро ворид кунед", "PIN нодуруст", "PIN-рамзҳо мувофиқ нестанд", "Кушодан", "PIN-ро фаромӯш кардед?",
        "PIN-ро барқарор кардан мумкин нест. Ҳамаи навиштаҳоро бо PIN якҷоя нест карда, аз нав оғоз кардан мумкин аст.",
        "Ҳамаи навиштаҳоро нест кардан", "Ҳамаи навиштаҳо ва PIN-ро тамоман нест мекунед?",
        "Кӯшишҳо аз ҳад зиёданд. %1 сония интизор шавед", "Навиштаҳои ман",
    )
    "tt" -> JournalStrings(
        "Тәүбә көндәлеге", "Үкенүләр, үпкәләр һәм тәүбә. Бары тик сезнең өчен",
        "Язмаларны бары тик сез күрәсез. Алар шушы телефонда шифрланып саклана, беркая да җибәрелми һәм резерв күчермәгә эләкми. Аккаунт кирәкми. Кушымтаны бетерсәгез, язмалар да юкка чыга.",
        "Күңелегездә ни бар?", "Үкенү", "Үпкә", "Тәүбә",
        "Саклау", "Баш тарту", "Үзгәртү", "Бетерү", "Бу язманы бөтенләйгә бетерәсезме?",
        "Әлегә буш. Нәрсәгә үкенүегезне, күңелегездә ни йөргәнен яки нәрсә өчен тәүбә итүегезне языгыз.",
        "Гафу сорау догалары", "Саклану", "PIN-код белән керү", "4–8 саннан торган PIN уйлап табыгыз", "PIN-ны кабатлагыз",
        "PIN кертегез", "PIN дөрес түгел", "PIN-кодлар туры килми", "Ачу", "PIN-ны оныттыгызмы?",
        "PIN-ны торгызып булмый. Барлык язмаларны PIN-код белән бергә бетереп, яңадан башларга була.",
        "Барлык язмаларны бетерү", "Барлык язмаларны һәм PIN-кодны бөтенләйгә бетерәсезме?",
        "Артык күп омтылыш. %1 с көтегез", "Минем язмалар",
    )
    "az" -> JournalStrings(
        "Tövbə gündəliyi", "Peşmanlıqlar, incikliklər və tövbə. Yalnız sizin üçün",
        "Bu qeydləri yalnız siz görürsünüz. Onlar bu telefonda şifrələnmiş şəkildə saxlanılır, heç yerə göndərilmir və ehtiyat nüsxəyə düşmür. Hesab lazım deyil. Tətbiqi silsəniz, qeydlər də silinəcək.",
        "Qəlbinizdə nə var?", "Peşmanlıq", "İnciklik", "Tövbə",
        "Yadda saxla", "Ləğv et", "Dəyiş", "Sil", "Bu qeyd həmişəlik silinsin?",
        "Hələ boşdur. Nəyə peşman olduğunuzu, qəlbinizdə nə gəzdirdiyinizi və ya nədən tövbə etdiyinizi yazın.",
        "Bağışlanma duaları", "Qoruma", "PIN kodla giriş", "4–8 rəqəmdən ibarət PIN seçin", "PIN-i təkrarlayın",
        "PIN-i daxil edin", "PIN səhvdir", "PIN kodlar uyğun gəlmir", "Aç", "PIN-i unutmusunuz?",
        "PIN-i bərpa etmək olmur. Bütün qeydləri PIN kodla birlikdə silib yenidən başlaya bilərsiniz.",
        "Bütün qeydləri sil", "Bütün qeydlər və PIN kod həmişəlik silinsin?",
        "Çox cəhd edildi. %1 san gözləyin", "Qeydlərim",
    )
    "tr" -> JournalStrings(
        "Tövbe günlüğü", "Pişmanlıklar, kırgınlıklar ve tövbe. Yalnızca sizin için",
        "Bu notları yalnızca siz görürsünüz. Bu telefonda şifreli olarak saklanır, hiçbir yere gönderilmez ve yedeklere alınmaz. Hesap gerekmez. Uygulamayı silerseniz notlar da silinir.",
        "Kalbinizde ne var?", "Pişmanlık", "Kırgınlık", "Tövbe",
        "Kaydet", "İptal", "Düzenle", "Sil", "Bu not kalıcı olarak silinsin mi?",
        "Henüz boş. Pişman olduğunuz şeyi, içinizde taşıdığınızı ya da tövbe ettiğiniz şeyi yazın.",
        "Bağışlanma duaları", "Koruma", "PIN ile giriş", "4–8 haneli bir PIN belirleyin", "PIN'i tekrar girin",
        "PIN'inizi girin", "Yanlış PIN", "PIN'ler eşleşmiyor", "Aç", "PIN'i mi unuttunuz?",
        "PIN kurtarılamaz. Tüm notları PIN ile birlikte silip yeniden başlayabilirsiniz.",
        "Tüm notları sil", "Tüm notlar ve PIN kalıcı olarak silinsin mi?",
        "Çok fazla deneme. %1 sn bekleyin", "Notlarım",
    )
    "fr" -> JournalStrings(
        "Journal de repentir", "Regrets, rancœurs et repentir. Rien que pour vous",
        "Vous seul voyez ces notes. Elles sont chiffrées sur ce téléphone, ne sont envoyées nulle part et restent hors des sauvegardes. Aucun compte n’est nécessaire. Si vous désinstallez l’application, les notes disparaissent avec elle.",
        "Qu’avez-vous sur le cœur ?", "Regret", "Rancœur", "Repentir",
        "Enregistrer", "Annuler", "Modifier", "Supprimer", "Supprimer définitivement cette note ?",
        "Rien pour l’instant. Écrivez ce que vous regrettez, ce qui pèse sur votre cœur ou ce dont vous vous repentez.",
        "Invocations pour le pardon", "Protection", "Demander un code PIN", "Choisissez un PIN de 4 à 8 chiffres", "Répétez le PIN",
        "Saisissez votre PIN", "PIN incorrect", "Les PIN ne correspondent pas", "Ouvrir", "PIN oublié ?",
        "Le PIN ne peut pas être récupéré. Vous pouvez supprimer toutes les notes avec le PIN et recommencer.",
        "Supprimer toutes les notes", "Supprimer définitivement toutes les notes et le PIN ?",
        "Trop d’essais. Patientez %1 s", "Mes notes",
    )
    "de" -> JournalStrings(
        "Reue-Tagebuch", "Bedauern, Kränkungen und Reue. Nur für Sie",
        "Nur Sie sehen diese Notizen. Sie werden verschlüsselt auf diesem Telefon gespeichert, nirgendwohin gesendet und nicht ins Backup aufgenommen. Kein Konto nötig. Wenn Sie die App deinstallieren, sind die Notizen mit ihr weg.",
        "Was liegt Ihnen auf dem Herzen?", "Bedauern", "Kränkung", "Reue",
        "Speichern", "Abbrechen", "Bearbeiten", "Löschen", "Diese Notiz endgültig löschen?",
        "Noch leer. Schreiben Sie, was Sie bedauern, was Ihnen auf dem Herzen liegt oder was Sie bereuen.",
        "Bittgebete um Vergebung", "Schutz", "PIN abfragen", "Wählen Sie eine PIN aus 4–8 Ziffern", "PIN wiederholen",
        "PIN eingeben", "Falsche PIN", "Die PINs stimmen nicht überein", "Öffnen", "PIN vergessen?",
        "Eine PIN lässt sich nicht wiederherstellen. Sie können alle Notizen samt PIN löschen und neu beginnen.",
        "Alle Notizen löschen", "Alle Notizen und die PIN endgültig löschen?",
        "Zu viele Versuche. Warten Sie %1 s", "Meine Notizen",
    )
    "es" -> JournalStrings(
        "Diario de arrepentimiento", "Remordimientos, rencores y arrepentimiento. Solo para ti",
        "Solo tú ves estas notas. Se guardan cifradas en este teléfono, no se envían a ningún sitio y quedan fuera de las copias de seguridad. No hace falta cuenta. Si desinstalas la aplicación, las notas desaparecen con ella.",
        "¿Qué llevas en el corazón?", "Remordimiento", "Rencor", "Arrepentimiento",
        "Guardar", "Cancelar", "Editar", "Eliminar", "¿Eliminar esta nota para siempre?",
        "Aún no hay nada. Escribe lo que lamentas, lo que te pesa en el corazón o aquello de lo que te arrepientes.",
        "Duas para pedir perdón", "Protección", "Pedir un PIN", "Elige un PIN de 4 a 8 cifras", "Repite el PIN",
        "Introduce tu PIN", "PIN incorrecto", "Los PIN no coinciden", "Abrir", "¿Olvidaste el PIN?",
        "El PIN no se puede recuperar. Puedes eliminar todas las notas junto con el PIN y empezar de nuevo.",
        "Eliminar todas las notas", "¿Eliminar para siempre todas las notas y el PIN?",
        "Demasiados intentos. Espera %1 s", "Mis notas",
    )
    "ar" -> JournalStrings(
        "دفتر التوبة", "الندم والضغائن والتوبة. لك وحدك",
        "لا يرى هذه الملاحظات أحد غيرك. تُحفظ مشفّرة على هذا الهاتف، ولا تُرسل إلى أي مكان، ولا تدخل في النسخ الاحتياطية. لا حاجة إلى حساب. إذا حذفت التطبيق حُذفت الملاحظات معه.",
        "ما الذي في قلبك؟", "ندم", "ضغينة", "توبة",
        "حفظ", "إلغاء", "تعديل", "حذف", "حذف هذه الملاحظة نهائيًا؟",
        "لا شيء هنا بعد. اكتب ما تندم عليه، أو ما يثقل قلبك، أو ما تبت منه.",
        "أدعية الاستغفار", "الحماية", "طلب رمز PIN", "اختر رمز PIN من 4 إلى 8 أرقام", "أعد إدخال الرمز",
        "أدخل رمز PIN", "رمز خاطئ", "الرمزان غير متطابقين", "فتح", "نسيت الرمز؟",
        "لا يمكن استعادة الرمز. يمكنك حذف جميع الملاحظات مع الرمز والبدء من جديد.",
        "حذف جميع الملاحظات", "حذف جميع الملاحظات والرمز نهائيًا؟",
        "محاولات كثيرة. انتظر %1 ث", "ملاحظاتي",
    )
    "id" -> JournalStrings(
        "Jurnal tobat", "Penyesalan, sakit hati, dan tobat. Hanya untuk Anda",
        "Hanya Anda yang dapat melihat catatan ini. Catatan disimpan terenkripsi di ponsel ini, tidak dikirim ke mana pun, dan tidak ikut dicadangkan. Tidak perlu akun. Jika aplikasi dihapus, catatan ikut terhapus.",
        "Apa yang ada di hati Anda?", "Penyesalan", "Sakit hati", "Tobat",
        "Simpan", "Batal", "Ubah", "Hapus", "Hapus catatan ini selamanya?",
        "Belum ada apa-apa. Tulis apa yang Anda sesali, apa yang mengganjal di hati, atau apa yang Anda tobati.",
        "Doa memohon ampunan", "Perlindungan", "Minta PIN", "Buat PIN 4–8 angka", "Ulangi PIN",
        "Masukkan PIN", "PIN salah", "PIN tidak cocok", "Buka", "Lupa PIN?",
        "PIN tidak dapat dipulihkan. Anda dapat menghapus semua catatan beserta PIN lalu mulai lagi.",
        "Hapus semua catatan", "Hapus semua catatan dan PIN selamanya?",
        "Terlalu banyak percobaan. Tunggu %1 dtk", "Catatan saya",
    )
    "ur" -> JournalStrings(
        "توبہ کی ڈائری", "پچھتاوے، رنجشیں اور توبہ۔ صرف آپ کے لیے",
        "یہ نوٹس صرف آپ دیکھ سکتے ہیں۔ یہ اسی فون پر خفیہ شکل میں محفوظ رہتے ہیں، کہیں نہیں بھیجے جاتے اور بیک اپ میں شامل نہیں ہوتے۔ کسی اکاؤنٹ کی ضرورت نہیں۔ ایپ ہٹانے پر نوٹس بھی ختم ہو جائیں گے۔",
        "آپ کے دل میں کیا ہے؟", "پچھتاوا", "رنجش", "توبہ",
        "محفوظ کریں", "منسوخ", "ترمیم", "حذف کریں", "یہ نوٹ ہمیشہ کے لیے حذف کریں؟",
        "ابھی کچھ نہیں۔ لکھیں کہ آپ کو کس بات کا پچھتاوا ہے، دل پر کیا بوجھ ہے یا آپ نے کس بات سے توبہ کی ہے۔",
        "استغفار کی دعائیں", "حفاظت", "PIN مانگیں", "4 سے 8 ہندسوں کا PIN بنائیں", "PIN دوبارہ درج کریں",
        "اپنا PIN درج کریں", "غلط PIN", "PIN مماثل نہیں", "کھولیں", "PIN بھول گئے؟",
        "PIN بحال نہیں ہو سکتا۔ آپ تمام نوٹس PIN سمیت حذف کر کے دوبارہ شروع کر سکتے ہیں۔",
        "تمام نوٹس حذف کریں", "تمام نوٹس اور PIN ہمیشہ کے لیے حذف کریں؟",
        "بہت زیادہ کوششیں۔ %1 سیکنڈ انتظار کریں", "میرے نوٹس",
    )
    else -> JournalStrings(
        "Repentance journal", "Regrets, grudges and repentance. Only for you",
        "Only you can see these notes. They are stored encrypted on this phone, never sent anywhere and left out of backups. No account is needed. If you uninstall the app, the notes are gone with it.",
        "What is on your heart?", "Regret", "Grudge", "Repentance",
        "Save", "Cancel", "Edit", "Delete", "Delete this note for good?",
        "Nothing here yet. Write what you regret, what weighs on your heart, or what you repent of.",
        "Duas for forgiveness", "Protection", "Ask for a PIN", "Choose a PIN of 4–8 digits", "Repeat the PIN",
        "Enter your PIN", "Wrong PIN", "The PINs do not match", "Open", "Forgot your PIN?",
        "A PIN cannot be recovered. You can delete all notes together with the PIN and start again.",
        "Delete all notes", "Delete all notes and the PIN for good?",
        "Too many tries. Wait %1 s", "My notes",
    )
}
