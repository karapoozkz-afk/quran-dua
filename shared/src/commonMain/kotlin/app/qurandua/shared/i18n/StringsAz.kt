package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun azAyahs(n: Int) = "$n ayə"

val AzStrings = EnStrings.copy(
    translitScript = "Oxunuş hərfləri",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Kiril"
            "en" -> "Latın"
            "es" -> "İspan"
            else -> "Avtomatik (dilə görə)"
        }
    },
    translitQuranNote = "Quran ayələrinin oxunuşu yalnız latın hərfləri ilədir (Tanzil.net). Heç bir əlifba ərəb səslərini dəqiq çatdıra bilmir, ona görə də oxunuşu səs yazısı ilə öyrənin.",
    appName = "Quran və Dua",
    navHome = "Əsas",
    navQuran = "Quran",
    navDuas = "Dualar",
    navSaved = "Seçilmişlər",
    navMore = "Digər",
    whatToRead = "Nə oxumalı?",
    searchHint = "Vəziyyətinizi yazın: «borcum var», «atam vəfat edib»…",
    searchExamples = listOf("borc üçün dua", "rəhmətlik atam üçün", "yata bilmirəm", "yola çıxmazdan əvvəl", "narahatam", "ərim xəstələnib"),
    situations = "Həyat vəziyyətləri",
    continueReading = "Oxumağa davam et",
    surahAyah = { s, a -> "Surə $s, ayə $a" },
    allDuas = "Bütün dualar",
    noResults = "Heç nə tapılmadı. Vəziyyəti başqa sözlərlə təsvir etməyə çalışın və ya siyahıdan seçin.",
    resultsSituations = "Vəziyyətlər",
    resultsDuas = "Dualar",
    resultsAyahs = "Quran ayələri",
    source = "Mənbə",
    repeatTimes = { n -> "$n dəfə təkrar et" },
    openInQuran = "Quranda aç",
    share = "Paylaş",
    copy = "Kopyala",
    copied = "Kopyalandı",
    listen = "Dinlə",
    cancel = "Ləğv et",
    audioDownload = "Quranın səsi",
    downloadingSurah = { n -> "$n saylı surə yüklənir" },
    audioSaved = { s -> "Telefonda saxlanılıb: $s" },
    downloadAll = { s -> "Bütün Quranı yüklə ($s)" },
    downloadSurah = { s -> "Bu surəni yüklə ($s)" },
    surahSaved = "Surə saxlanıldı, internetsiz səslənir",
    deleteAudio = "Saxlanılmış səsi sil",
    audioHint = "Dinlədiyiniz hər şey telefonda həmişəlik saxlanılır və bir daha yüklənmir. Böyük yükləmələri Wi‑Fi ilə etmək daha yaxşıdır.",
    downloadFailed = { n -> "$n ayə yüklənmədi. Tamamlamaq üçün yenidən toxunun." },
    downloadProgress = { d, t -> "$t ayədən $d" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "Bütün surəni dinlə",
    ttsNote = "Telefonun səsi (nitq sintezi), qarinin yazısı deyil. Yalnız tələffüzə istiqamət vermək üçündür: ərəb mətni ilə yoxlayın.",
    noArabicVoice = "Telefonda ərəb səsi yoxdur. Onu nitq sintezi ayarlarından quraşdıra bilərsiniz.",
    voiceSettings = "Səs ayarları",
    stop = "Dayandır",
    addFavorite = "Yadda saxla",
    removeFavorite = "Seçilmişlərdən çıxar",
    bookmark = "Əlfəcin",
    ayahsCount = ::azAyahs,
    meccan = "Məkki",
    medinan = "Mədəni",
    loadingQuran = "Quranın mətni hazırlanır (yalnız ilk açılışda)…",
    savedDuas = "Saxlanılmış dualar",
    savedAyahs = "Quran əlfəcinləri",
    savedEmpty = "Saxladığınız dualar və ayələr burada görünəcək.",
    settings = "Ayarlar",
    language = "Dil",
    showTranslit = "Oxunuşu göstər",
    showTranslation = "Tərcüməni göstər",
    arabicSize = "Ərəb mətninin ölçüsü",
    theme = "Mövzu",
    themeSystem = "Sistem",
    themeLight = "İşıqlı",
    themeDark = "Qaranlıq",
    donate = "Sədəqə və dəstək",
    donateTitle = "Sədəqeyi-cariyə",
    donateBody = "Tətbiq pulsuzdur və reklamsızdır. Xeyriyyə layihələrinə ianələr yoxlanılmış fondların saytlarında qəbul edilir — aşağıdakı düymə səhifəni brauzerdə açır.",
    donateCharity = "İanə səhifəsini aç",
    donateSoon = "İanə səhifəsi tezliklə əlavə olunacaq.",
    about = "Tətbiq haqqında",
    aboutBody = "Hər materialın mənbəyi və etibarlılıq dərəcəsi göstərilib; alimlər arasındakı ixtilaflar qeyd olunub. Tətbiq fətva vermir: şəxsi suallarınızla bilikli bir imama müraciət edin.",
    sourcesTitle = "Mətn mənbələri",
    sourcesBody = "Quranın ərəb mətni: The Noble Qur'an Encyclopedia (quranenc.com). Tərcümələr: Alıxan Musayev (Azərbaycan dili, quranenc.com), Saheeh International (ingilis dili), Elmir Kuliyev (rus dili); oxunuş: Tanzil.net. Verilənlər toplusu: quran-json (CC BY-SA 4.0). Səs: EveryAyah.com, qari Mişari Rəşid əl-Əfasi. Azərbaycan dilindəki mətnlər mütəxəssis yoxlamasını gözləyir.",
    back = "Geri",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Quran"
            Grade.SAHIH -> "Səhih hədis"
            Grade.HASAN -> "Həsən hədis"
            Grade.ATHAR -> "Səhabə / alim sözü"
            Grade.DAIF -> "Zəif hədis"
            Grade.DISPUTED -> "Alimlər ixtilaf edib"
            Grade.NOSOURCE -> "Mənbəyi yoxdur"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Qurandan mətn."
            Grade.SAHIH -> "Hədis səhih (etibarlı) sayılıb."
            Grade.HASAN -> "Hədis həsən (yaxşı) sayılıb və dəlil kimi istifadə olunur."
            Grade.ATHAR -> "Peyğəmbərin ﷺ hədisi deyil; səhabələrin və ya alimlərin sözüdür."
            Grade.DAIF -> "Zəif hədis: onu Sünnəyə aid etmək olmaz."
            Grade.DISPUTED -> "Alimlər bu barədə fərqli fikirdədir. Bunu sabit Sünnə kimi təqdim etməyin."
            Grade.NOSOURCE -> "Quranda və səhih Sünnədə mənbəyi olmayan geniş yayılmış tövsiyə. Quran oxumaq və dua etmək həmişə olar, lakin vəd edilən nəticə və təkrar sayı sabit deyil."
        }
    },
    zakat = "Zəkat kalkulyatoru",
    zakatIntro = "Malınız bir qəməri il ərzində nisabdan aşağı düşməyibsə, zəkat fərz olur. Məbləğləri bir valyutada daxil edin.",
    zakatMadhhab = "Məzhəb",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hənəfi"
            Madhhab.MALIKI -> "Maliki"
            Madhhab.SHAFII -> "Şafei"
            Madhhab.HANBALI -> "Hənbəli"
        }
    },
    zakatNisabBasis = "Nisab ölçüsü",
    nisabGold = "Qızıl (85 q)",
    nisabSilver = "Gümüş (595 q)",
    zakatNisabNote = "Gümüş nisabı daha aşağıdır, ona görə də daha çox insan zəkat verir və kasıblar daha çox faydalanır. Nağd pul üçün bir çox müasir alim qızıl nisabını əsas götürür. Ölkənizin dini idarəsi nisab elan edibsə, ona əməl edin.",
    zakatJewelryRule = "Taxılan zinət əşyalarını nəzərə al",
    zakatDebtRule = "Ödəmə vaxtı çatmış borcları çıx",
    zakatRulesHint = "Məzhəbinizin mövqeyinə uyğun qurulub; hər birini dəyişə bilərsiniz.",
    zakatCash = "Nağd pul və bank hesabları",
    zakatGold = "Qızıl, qram (külçə, sikkə, yığım)",
    zakatGoldJewelry = "Taxılan qızıl zinət əşyaları, qram",
    zakatSilver = "Gümüş, qram",
    zakatSilverJewelry = "Taxılan gümüş zinət əşyaları, qram",
    zakatTrade = "Satış üçün saxlanılan mallar (bazar dəyəri)",
    zakatReceivables = "Geri almağı gözlədiyiniz alacaqlar",
    zakatDebts = "İndi ödəməli olduğunuz borclar",
    zakatGoldPrice = "1 q qızılın qiyməti",
    zakatSilverPrice = "1 q gümüşün qiyməti",
    zakatAssets = "Zəkata tabe olan mal",
    zakatDeductions = "Çıxılan borclar",
    zakatNet = "Xalis",
    zakatNisab = "Nisab",
    zakatDue = "Veriləcək zəkat (2,5%)",
    zakatNotDue = "Nisabdan aşağıdır: bu maldan zəkat fərz deyil.",
    zakatNeedPrice = "Seçilmiş nisab metalının qram qiymətini daxil edin.",
    zakatRecipientsTitle = "Zəkat kimlərə verilir",
    zakatRecipients = "Yoxsullar, kasıblar, zəkatı toplayanlar, qəlbləri İslama isinişdirilmək istənilənlər, kölələrin azad edilməsi, borclular, Allah yolunda olanlar və yolda qalmış müsafirlər (Quran, 9:60).",
    zakatSources = "Mənbələr: Quran, 9:60, 9:103; Buxari, 1447 (beş ukiyyədən az gümüşdə zəkat yoxdur); Əbu Davud, 1573 (qızılın nisabı və ilin tamamlanması).",
    zakatDisclaimer = "Bu, təxmini hesablamadır. Heyvanlar, məhsul, səhmlər və ticarətin xüsusiyyətləri nəzərə alınmayıb — öz vəziyyətiniz üçün bilikli bir imama müraciət edin.",
    charitiesTitle = "Yoxlanılmış fondlar",
    charitiesEmpty = "Hələ yoxlanılmış fond əlavə olunmayıb. Fond burada yalnız qeydiyyat nömrəsi və yoxlanılma tarixi ilə birlikdə görünür.",
    charityReg = "Qeydiyyat",
    charityVerified = { date, who -> "Yoxlanılıb: $date, $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zəkat"
            "sadaqah" -> "Sədəqə"
            "mosque" -> "Məscidlər"
            "orphans" -> "Yetimlər"
            "water" -> "Su quyuları"
            "food" -> "Ərzaq"
            "education" -> "Təhsil"
            else -> "Ümumi"
        }
    },
    charityDonate = "Fondun saytında ianə et",
    charityWebsite = "Veb-sayt",
    supportApp = "Tətbiqin özünü dəstəkləmək, dərc olunduqdan sonra Google Play vasitəsilə mümkün olacaq.",
)
