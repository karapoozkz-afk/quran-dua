package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

val UrStrings = EnStrings.copy(
    translitScript = "تلفظ کے حروف",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "سیریلک"
            "en" -> "لاطینی"
            "es" -> "ہسپانوی"
            else -> "خودکار (زبان کے مطابق)"
        }
    },
    translitQuranNote = "قرآن کی آیات کا تلفظ صرف لاطینی حروف میں ہے (Tanzil.net)۔ کوئی بھی رسم الخط عربی آوازیں بالکل درست نہیں لکھ سکتا، اس لیے تلاوت آڈیو سے سیکھیں۔",
    appName = "قرآن اور دعا",
    navHome = "ہوم",
    navQuran = "قرآن",
    navDuas = "دعائیں",
    navSaved = "محفوظ",
    navMore = "مزید",
    whatToRead = "کیا پڑھوں؟",
    searchHint = "اپنی صورتحال لکھیں: «مجھ پر قرض ہے»، «میرے والد فوت ہو گئے»…",
    searchExamples = listOf("قرض سے نجات کی دعا", "مرحوم والد کے لیے", "نیند نہیں آتی", "سفر سے پہلے", "مجھے ڈر لگتا ہے", "میرے شوہر بیمار ہیں"),
    situations = "زندگی کے حالات",
    continueReading = "پڑھنا جاری رکھیں",
    surahAyah = { s, a -> "سورہ $s، آیت $a" },
    allDuas = "تمام دعائیں",
    noResults = "کچھ نہیں ملا۔ دوسرے الفاظ میں لکھ کر دیکھیں یا فہرست سے کوئی صورتحال منتخب کریں۔",
    resultsSituations = "حالات",
    resultsDuas = "دعائیں",
    resultsAyahs = "قرآن کی آیات",
    source = "حوالہ",
    repeatTimes = { n -> "$n بار دہرائیں" },
    openInQuran = "قرآن میں کھولیں",
    share = "شیئر کریں",
    copy = "کاپی کریں",
    copied = "کاپی ہو گیا",
    listen = "سنیں",
    cancel = "منسوخ کریں",
    audioDownload = "قرآن کی تلاوت",
    downloadingSurah = { n -> "سورہ $n ڈاؤن لوڈ ہو رہی ہے" },
    audioSaved = { s -> "فون میں محفوظ: $s" },
    downloadAll = { s -> "پورا قرآن ڈاؤن لوڈ کریں ($s)" },
    downloadSurah = { s -> "یہ سورہ ڈاؤن لوڈ کریں ($s)" },
    surahSaved = "سورہ محفوظ ہے، انٹرنیٹ کے بغیر چلتی ہے",
    deleteAudio = "محفوظ آڈیو حذف کریں",
    audioHint = "جو آپ سنتے ہیں وہ فون میں ہمیشہ کے لیے محفوظ رہتا ہے اور دوبارہ ڈاؤن لوڈ نہیں ہوتا۔ بڑی ڈاؤن لوڈ Wi‑Fi پر بہتر ہے۔",
    downloadFailed = { n -> "$n آیات ڈاؤن لوڈ نہیں ہوئیں۔ مکمل کرنے کے لیے دوبارہ دبائیں۔" },
    downloadProgress = { d, t -> "$t میں سے $d آیات" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "پوری سورت سنیں",
    ttsNote = "فون کی آواز (ٹیکسٹ ٹو اسپیچ)، کسی قاری کی ریکارڈنگ نہیں۔ صرف تلفظ کی رہنمائی کے لیے: عربی متن سے ملا لیں۔",
    noArabicVoice = "فون میں عربی آواز نہیں ہے۔ اسے ٹیکسٹ ٹو اسپیچ کی ترتیبات سے انسٹال کیا جا سکتا ہے۔",
    voiceSettings = "آواز کی ترتیبات",
    stop = "روکیں",
    addFavorite = "محفوظ کریں",
    removeFavorite = "محفوظ سے ہٹائیں",
    bookmark = "بک مارک",
    ayahsCount = { n -> "$n آیات" },
    meccan = "مکی",
    medinan = "مدنی",
    loadingQuran = "قرآن کا متن تیار ہو رہا ہے (صرف پہلی بار)…",
    savedDuas = "محفوظ دعائیں",
    savedAyahs = "قرآن کے بک مارکس",
    savedEmpty = "جو دعائیں اور آیات آپ محفوظ کریں گے وہ یہاں نظر آئیں گی۔",
    settings = "ترتیبات",
    language = "زبان",
    showTranslit = "رومن تلفظ دکھائیں",
    showTranslation = "ترجمہ دکھائیں",
    arabicSize = "عربی متن کا سائز",
    theme = "تھیم",
    themeSystem = "سسٹم کے مطابق",
    themeLight = "روشن",
    themeDark = "تاریک",
    donate = "صدقہ اور تعاون",
    donateTitle = "صدقہ جاریہ",
    donateBody = "یہ ایپ مفت اور اشتہارات سے پاک ہے۔ خیراتی منصوبوں کے لیے عطیات تصدیق شدہ اداروں کی ویب سائٹس پر قبول کیے جاتے ہیں: نیچے والا بٹن صفحہ براؤزر میں کھولتا ہے۔",
    donateCharity = "عطیہ کا صفحہ کھولیں",
    donateSoon = "عطیہ کا صفحہ جلد شامل کیا جائے گا۔",
    about = "ایپ کے بارے میں",
    aboutBody = "ہر مواد کا حوالہ اور صحت کا درجہ دکھایا گیا ہے، اور علماء کے اختلاف کی نشاندہی کی گئی ہے۔ یہ ایپ فتویٰ نہیں دیتی: ذاتی مسائل کے لیے کسی مستند عالم یا امام سے پوچھیں۔",
    sourcesTitle = "متن کے ماخذ",
    sourcesBody = "قرآن کا عربی متن: The Noble Qur'an Encyclopedia (quranenc.com)۔ ترجمے: مولانا محمد جوناگڑھی (اردو)، Saheeh International (انگریزی)، المیر کولییف (روسی)؛ رومن تلفظ: Tanzil.net۔ ڈیٹاسیٹ: quran-json (CC BY-SA 4.0)۔ آڈیو: EveryAyah.com، قاری مشاری راشد العفاسی۔ اردو متن ماہر کی نظرثانی کے منتظر ہیں۔",
    back = "واپس",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "قرآن"
            Grade.SAHIH -> "صحیح حدیث"
            Grade.HASAN -> "حسن حدیث"
            Grade.ATHAR -> "صحابہ / علماء کے اقوال"
            Grade.DAIF -> "ضعیف حدیث"
            Grade.DISPUTED -> "علماء کا اختلاف ہے"
            Grade.NOSOURCE -> "بے سند"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "قرآن کا متن۔"
            Grade.SAHIH -> "اس حدیث کو صحیح (مستند) قرار دیا گیا ہے۔"
            Grade.HASAN -> "اس حدیث کو حسن (اچھی) قرار دیا گیا ہے اور یہ دلیل کے طور پر استعمال ہوتی ہے۔"
            Grade.ATHAR -> "یہ نبی ﷺ کی حدیث نہیں، بلکہ صحابہ یا علماء کے اقوال ہیں۔"
            Grade.DAIF -> "ضعیف حدیث: اسے سنت کی طرف منسوب نہیں کیا جا سکتا۔"
            Grade.DISPUTED -> "اس میں علماء کا اختلاف ہے۔ اسے ثابت شدہ سنت کے طور پر پیش نہ کریں۔"
            Grade.NOSOURCE -> "ایک مشہور مشورہ جس کی قرآن اور صحیح سنت میں کوئی سند نہیں۔ قرآن پڑھنا اور دعا کرنا ہمیشہ جائز ہے، لیکن وعدہ کیا گیا نتیجہ اور تکرار کی تعداد ثابت نہیں۔"
        }
    },
    zakat = "زکوٰۃ کیلکولیٹر",
    zakatIntro = "زکوٰۃ اس وقت فرض ہوتی ہے جب مال پورا ایک قمری سال نصاب کے برابر یا اس سے زیادہ رہا ہو۔ تمام رقمیں ایک ہی کرنسی میں درج کریں۔",
    zakatMadhhab = "مسلک (فقہی مذہب)",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "حنفی"
            Madhhab.MALIKI -> "مالکی"
            Madhhab.SHAFII -> "شافعی"
            Madhhab.HANBALI -> "حنبلی"
        }
    },
    zakatNisabBasis = "نصاب بلحاظ",
    nisabGold = "سونا (85 گرام)",
    nisabSilver = "چاندی (595 گرام)",
    zakatNisabNote = "چاندی کا نصاب کم ہے، اس لیے زیادہ لوگ زکوٰۃ دیتے ہیں اور غریبوں کو زیادہ فائدہ ہوتا ہے؛ نقد رقم کے لیے بہت سے معاصر علماء سونے کا نصاب استعمال کرتے ہیں۔ اگر آپ کے ملک کے مذہبی ادارے نے نصاب کا اعلان کیا ہے تو اس کی پیروی کریں۔",
    zakatJewelryRule = "پہنے جانے والے زیورات شامل کریں",
    zakatDebtRule = "ابھی واجب الادا قرض منہا کریں",
    zakatRulesHint = "آپ کے مسلک کے مؤقف کے مطابق ترتیب دیا گیا ہے؛ ہر نکتہ تبدیل کیا جا سکتا ہے۔",
    zakatCash = "نقدی اور بینک میں رقم",
    zakatGold = "سونا، گرام (بسکٹ، سکے، بچت)",
    zakatGoldJewelry = "پہنے جانے والے سونے کے زیورات، گرام",
    zakatSilver = "چاندی، گرام",
    zakatSilverJewelry = "پہنے جانے والے چاندی کے زیورات، گرام",
    zakatTrade = "فروخت کے لیے مال (بازاری قیمت پر)",
    zakatReceivables = "دوسروں پر آپ کا قرض جس کی واپسی کی امید ہے",
    zakatDebts = "آپ کے ذمے قرض جو ابھی ادا کرنا ہے",
    zakatGoldPrice = "1 گرام سونے کی قیمت",
    zakatSilverPrice = "1 گرام چاندی کی قیمت",
    zakatAssets = "قابل زکوٰۃ مال",
    zakatDeductions = "منہا کیے گئے قرض",
    zakatNet = "خالص",
    zakatNisab = "نصاب",
    zakatDue = "قابل ادا زکوٰۃ (2.5%)",
    zakatNotDue = "نصاب سے کم: اس مال پر زکوٰۃ فرض نہیں۔",
    zakatNeedPrice = "جس دھات کے لحاظ سے نصاب لگایا جا رہا ہے اس کی فی گرام قیمت درج کریں۔",
    zakatRecipientsTitle = "زکوٰۃ کسے دی جائے",
    zakatRecipients = "فقراء، مساکین، زکوٰۃ وصول کرنے والے کارکن، جن کے دلوں کو مانوس کرنا ہو، غلاموں کی آزادی، قرض دار، اللہ کی راہ میں، اور مسافر (قرآن 9:60)۔",
    zakatSources = "حوالہ جات: قرآن 9:60، 9:103؛ صحیح بخاری، 1447 (پانچ اوقیہ سے کم چاندی پر زکوٰۃ نہیں)؛ سنن ابو داود، 1573 (سونے کا نصاب اور سال کا گزرنا)۔",
    zakatDisclaimer = "یہ ایک تخمینہ ہے۔ مویشی، فصلیں، حصص اور کاروبار کی تفصیلات شامل نہیں — اپنے معاملے کے لیے کسی مستند امام سے پوچھیں۔",
    charitiesTitle = "تصدیق شدہ ادارے",
    charitiesEmpty = "ابھی کوئی تصدیق شدہ ادارہ شامل نہیں کیا گیا۔ کوئی ادارہ یہاں صرف اپنے رجسٹریشن نمبر اور تصدیق کی تاریخ کے ساتھ ظاہر ہوگا۔",
    charityReg = "رجسٹریشن",
    charityVerified = { date, who -> "تصدیق: $date، $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "زکوٰۃ"
            "sadaqah" -> "صدقہ"
            "mosque" -> "مساجد"
            "orphans" -> "یتیم"
            "water" -> "کنویں"
            "food" -> "کھانا"
            "education" -> "تعلیم"
            else -> "عمومی ضروریات"
        }
    },
    charityDonate = "ادارے کی ویب سائٹ پر عطیہ دیں",
    charityWebsite = "ویب سائٹ",
    supportApp = "ایپ کی اشاعت کے بعد خود ایپ کی معاونت Google Play کے ذریعے ممکن ہوگی۔",
)
