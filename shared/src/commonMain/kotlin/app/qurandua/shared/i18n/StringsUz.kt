package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun uzAyahs(n: Int) = "$n oyat"

val UzStrings = EnStrings.copy(
    translitScript = "Transliteratsiya harflari",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Kirill"
            "en" -> "Lotin (inglizcha)"
            "es" -> "Ispancha"
            else -> "Avto (til bo‘yicha)"
        }
    },
    translitQuranNote = "Qur’on oyatlari faqat lotin harflarida berilgan (Tanzil.net). Arab tovushlarini hech bir alifbo aniq bera olmaydi, shuning uchun o‘qishni audio orqali o‘rganing.",
    appName = "Qur’on va Duo",
    navHome = "Bosh sahifa",
    navQuran = "Qur’on",
    navDuas = "Duolar",
    navSaved = "Saqlanganlar",
    navMore = "Yana",
    whatToRead = "Nima o‘qish kerak?",
    searchHint = "Holatingizni yozing: «qarzim bor», «otam vafot etdi»…",
    searchExamples = listOf("qarzdan qutulish duosi", "marhum otam uchun", "uxlay olmayapman", "safardan oldin", "qo‘rqyapman", "erim kasal bo‘lib qoldi"),
    situations = "Hayotiy holatlar",
    continueReading = "O‘qishni davom ettirish",
    surahAyah = { s, a -> "$s-sura, $a-oyat" },
    allDuas = "Barcha duolar",
    noResults = "Hech narsa topilmadi. Holatni boshqa so‘zlar bilan yozib ko‘ring yoki ro‘yxatdan tanlang.",
    resultsSituations = "Holatlar",
    resultsDuas = "Duolar",
    resultsAyahs = "Qur’on oyatlari",
    source = "Manba",
    repeatTimes = { n -> "$n marta takrorlang" },
    openInQuran = "Qur’onda ochish",
    share = "Ulashish",
    copy = "Nusxa olish",
    copied = "Nusxa olindi",
    listen = "Tinglash",
    cancel = "Bekor qilish",
    audioDownload = "Qur’on audiosi",
    downloadingSurah = { n -> "$n-sura yuklanmoqda" },
    audioSaved = { s -> "Telefonda saqlangan: $s" },
    downloadAll = { s -> "Butun Qur’onni yuklab olish ($s)" },
    downloadSurah = { s -> "Surani yuklab olish ($s)" },
    surahSaved = "Sura saqlandi, internetsiz eshitiladi",
    deleteAudio = "Saqlangan audioni o‘chirish",
    audioHint = "Tinglangan audio telefonda butunlay saqlanadi va qayta yuklanmaydi. Katta yuklamalarni Wi‑Fi orqali qilgan ma’qul.",
    downloadFailed = { n -> "$n oyat yuklanmadi. Davom ettirish uchun yana bosing." },
    downloadProgress = { d, t -> "$t oyatdan $d" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "Butun surani tinglash",
    ttsNote = "Telefon ovozi (nutq sintezi), qori yozuvi emas. Faqat talaffuzni chamalash uchun: arabcha matn bilan solishtiring.",
    noArabicVoice = "Telefonda arabcha ovoz yo‘q. Uni nutq sintezi sozlamalaridan o‘rnatish mumkin.",
    voiceSettings = "Ovoz sozlamalari",
    stop = "To‘xtatish",
    addFavorite = "Saqlash",
    removeFavorite = "Saqlanganlardan olib tashlash",
    bookmark = "Xatcho‘p",
    ayahsCount = ::uzAyahs,
    meccan = "Makkiy",
    medinan = "Madaniy",
    loadingQuran = "Qur’on matni tayyorlanmoqda (faqat birinchi ishga tushirishda)…",
    savedDuas = "Saqlangan duolar",
    savedAyahs = "Qur’ondagi xatcho‘plar",
    savedEmpty = "Siz saqlagan duolar va oyatlar shu yerda ko‘rinadi.",
    settings = "Sozlamalar",
    language = "Til",
    showTranslit = "Transliteratsiyani ko‘rsatish",
    showTranslation = "Tarjimani ko‘rsatish",
    arabicSize = "Arabcha matn o‘lchami",
    theme = "Mavzu",
    themeSystem = "Tizimdagidek",
    themeLight = "Yorug‘",
    themeDark = "Qorong‘i",
    donate = "Sadaqa va qo‘llab-quvvatlash",
    donateTitle = "Sadaqai joriya",
    donateBody = "Ilova bepul va reklamasiz. Xayriya loyihalari uchun ehsonlar tekshirilgan jamg‘armalarning saytlarida qabul qilinadi: quyidagi tugma sahifani brauzerda ochadi.",
    donateCharity = "Ehson sahifasini ochish",
    donateSoon = "Ehson sahifasi tez orada qo‘shiladi.",
    about = "Ilova haqida",
    aboutBody = "Har bir materialning manbasi va ishonchlilik darajasi ko‘rsatilgan, olimlar ixtilofi belgilangan. Ilova fatvo bermaydi: shaxsiy savollaringizni bilimli imomga bering.",
    sourcesTitle = "Matn manbalari",
    sourcesBody = "Qur’onning arabcha matni: The Noble Qur'an Encyclopedia (quranenc.com). Ma’nolar tarjimasi: Alouddin Mansur (o‘zbekcha, kirill yozuvida, QuranEnc.com), Elmir Kuliyev (ruscha), Saheeh International (inglizcha); transliteratsiya: Tanzil.net. Ma’lumotlar to‘plami: quran-json (CC BY-SA 4.0). Audio: EveryAyah.com, qori Mishari Roshid al-Afasiy. O‘zbekcha matnlar mutaxassis tekshiruvini kutmoqda.",
    back = "Orqaga",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Qur’on"
            Grade.SAHIH -> "Sahih hadis"
            Grade.HASAN -> "Hasan hadis"
            Grade.ATHAR -> "Sahobalar / olimlar so‘zi"
            Grade.DAIF -> "Zaif hadis"
            Grade.DISPUTED -> "Olimlar ixtilof qilgan"
            Grade.NOSOURCE -> "Manbasi yo‘q"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Qur’on matni."
            Grade.SAHIH -> "Hadis sahih (ishonchli) deb baholangan."
            Grade.HASAN -> "Hadis hasan (yaxshi) deb baholangan va dalil sifatida qo‘llanadi."
            Grade.ATHAR -> "Bu Payg‘ambar ﷺ hadisi emas, sahobalar yoki olimlarning so‘zi."
            Grade.DAIF -> "Zaif hadis: uni Sunnatga nisbat berib bo‘lmaydi."
            Grade.DISPUTED -> "Olimlarning fikri turlicha. Buni sobit Sunnat sifatida ko‘rsatmang."
            Grade.NOSOURCE -> "Qur’onda ham, ishonchli Sunnatda ham manbasi yo‘q keng tarqalgan maslahat. Qur’on o‘qish va duo qilish doim mumkin, ammo va’da qilingan natija va takrorlash soni sobit emas."
        }
    },
    zakat = "Zakot kalkulyatori",
    zakatIntro = "Mol-mulk bir hijriy yil davomida nisobdan kam bo‘lmasa, zakot farz bo‘ladi. Summalarni bitta valyutada kiriting.",
    zakatMadhhab = "Mazhab",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafiy"
            Madhhab.MALIKI -> "Molikiy"
            Madhhab.SHAFII -> "Shofe’iy"
            Madhhab.HANBALI -> "Hanbaliy"
        }
    },
    zakatNisabBasis = "Nisob bo‘yicha",
    nisabGold = "oltin (85 g)",
    nisabSilver = "kumush (595 g)",
    zakatNisabNote = "Kumush nisobi pastroq, shuning uchun zakotni ko‘proq odam to‘laydi va kambag‘allarga foydasi ko‘p; pul uchun oltin nisobini ko‘plab zamonaviy olimlar qo‘llaydi. Agar mamlakatingiz musulmonlari idorasi nisobni e’lon qilgan bo‘lsa, shunga amal qiling.",
    zakatJewelryRule = "Taqib yurgan zargarlik buyumlarini hisoblash",
    zakatDebtRule = "Hozir to‘lanadigan qarzlarni ayirish",
    zakatRulesHint = "Mazhab qarashiga ko‘ra belgilandi; har bir bandni o‘zgartirish mumkin.",
    zakatCash = "Naqd pul va hisobdagi mablag‘",
    zakatGold = "Oltin, gramm (quyma, tanga, jamg‘arma)",
    zakatGoldJewelry = "Taqib yurgan oltin buyumlar, gramm",
    zakatSilver = "Kumush, gramm",
    zakatSilverJewelry = "Taqib yurgan kumush buyumlar, gramm",
    zakatTrade = "Sotish uchun mo‘ljallangan tovar (bozor narxida)",
    zakatReceivables = "Qaytarilishini kutayotgan sizga qarz pullar",
    zakatDebts = "Hozir to‘lashingiz kerak bo‘lgan qarzlar",
    zakatGoldPrice = "1 g oltin narxi",
    zakatSilverPrice = "1 g kumush narxi",
    zakatAssets = "Zakot olinadigan mol",
    zakatDeductions = "Ayirilgan qarzlar",
    zakatNet = "Jami",
    zakatNisab = "Nisob",
    zakatDue = "To‘lanadigan zakot (2,5%)",
    zakatNotDue = "Nisobdan kam: bu moldan zakot farz emas.",
    zakatNeedPrice = "Nisob hisoblanadigan metallning 1 gramm narxini kiriting.",
    zakatRecipientsTitle = "Zakot kimga beriladi",
    zakatRecipients = "Faqirlarga, miskinlarga, zakot yig‘uvchilarga, qalblari Islomga oshno qilinadiganlarga, qullarni ozod qilishga, qarzdorlarga, Alloh yo‘lida va yo‘lda qolgan musofirlarga (Qur’on, 9:60).",
    zakatSources = "Manbalar: Qur’on, 9:60, 9:103; Buxoriy, 1447 (besh uqiyadan kam kumushda zakot yo‘q); Abu Dovud, 1573 (oltin nisobi va bir yil o‘tishi).",
    zakatDisclaimer = "Bu taxminiy hisob. Chorva, hosil, aksiyalar va tijorat xususiyatlari hisobga olinmagan — o‘z holatingiz bo‘yicha bilimli imomdan so‘rang.",
    charitiesTitle = "Tekshirilgan jamg‘armalar",
    charitiesEmpty = "Tekshirilgan jamg‘armalar hozircha qo‘shilmagan. Jamg‘arma bu yerda faqat ro‘yxatdan o‘tish raqami va tekshirilgan sanasi bilan ko‘rinadi.",
    charityReg = "Ro‘yxatdan o‘tgan",
    charityVerified = { date, who -> "Tekshirildi: $date, $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zakot"
            "sadaqah" -> "Sadaqa"
            "mosque" -> "Masjidlar"
            "orphans" -> "Yetimlar"
            "water" -> "Quduqlar"
            "food" -> "Oziq-ovqat"
            "education" -> "Ta’lim"
            else -> "Umumiy ehtiyojlar"
        }
    },
    charityDonate = "Jamg‘arma saytida ehson qilish",
    charityWebsite = "Sayt",
    supportApp = "Ilovaning o‘zini qo‘llab-quvvatlash e’lon qilingandan keyin Google Play orqali mumkin bo‘ladi.",
)
