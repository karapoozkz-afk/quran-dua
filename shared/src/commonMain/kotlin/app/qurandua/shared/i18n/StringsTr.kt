package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun trAyahs(n: Int) = "$n ayet"

val TrStrings = EnStrings.copy(
    translitScript = "Okunuş harfleri",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Kiril"
            "en" -> "Latin"
            "es" -> "İspanyolca"
            else -> "Otomatik (dile göre)"
        }
    },
    translitQuranNote = "Kur'an ayetlerinin okunuşu yalnızca Latin harfleriyledir (Tanzil.net). Hiçbir alfabe Arapça sesleri tam olarak veremez; okuyuşu sesli kayıtla öğrenin.",
    appName = "Kur'an ve Dua",
    navHome = "Ana sayfa",
    navQuran = "Kur'an",
    navDuas = "Dualar",
    navSaved = "Kaydedilenler",
    navMore = "Diğer",
    whatToRead = "Ne okumalıyım?",
    searchHint = "Durumunuzu yazın: «borcum var», «babam vefat etti»…",
    searchExamples = listOf("borç için dua", "rahmetli babam için", "uyuyamıyorum", "yolculuktan önce", "kaygılıyım", "eşim hastalandı"),
    situations = "Hayattan durumlar",
    continueReading = "Okumaya devam et",
    surahAyah = { s, a -> "$s. sure, $a. ayet" },
    allDuas = "Tüm dualar",
    noResults = "Hiçbir şey bulunamadı. Durumu başka kelimelerle anlatmayı deneyin veya listeden seçin.",
    resultsSituations = "Durumlar",
    resultsDuas = "Dualar",
    resultsAyahs = "Kur'an ayetleri",
    source = "Kaynak",
    repeatTimes = { n -> "$n kez tekrarla" },
    openInQuran = "Kur'an'da aç",
    share = "Paylaş",
    copy = "Kopyala",
    copied = "Kopyalandı",
    listen = "Dinle",
    stop = "Durdur",
    addFavorite = "Kaydet",
    removeFavorite = "Kaydedilenlerden çıkar",
    bookmark = "Yer imi",
    ayahsCount = ::trAyahs,
    meccan = "Mekkî",
    medinan = "Medenî",
    loadingQuran = "Kur'an metni hazırlanıyor (yalnızca ilk açılışta)…",
    savedDuas = "Kaydedilen dualar",
    savedAyahs = "Kur'an yer imleri",
    savedEmpty = "Kaydettiğiniz dualar ve ayetler burada görünecek.",
    settings = "Ayarlar",
    language = "Dil",
    showTranslit = "Okunuşu göster",
    showTranslation = "Meali göster",
    arabicSize = "Arapça metin boyutu",
    theme = "Tema",
    themeSystem = "Sistem",
    themeLight = "Açık",
    themeDark = "Koyu",
    donate = "Sadaka ve destek",
    donateTitle = "Sadaka-i câriye",
    donateBody = "Uygulama ücretsiz ve reklamsızdır. Hayır projelerine bağışlar, güvenilirliği doğrulanmış kuruluşların sitelerinde kabul edilir; aşağıdaki düğme sayfayı tarayıcıda açar.",
    donateCharity = "Bağış sayfasını aç",
    donateSoon = "Bağış sayfası yakında eklenecek.",
    about = "Hakkında",
    aboutBody = "Her içeriğin kaynağı ve sıhhat derecesi belirtilmiştir; âlimler arasındaki görüş ayrılıkları işaretlenmiştir. Uygulama fetva vermez: kişisel sorularınız için bilgili bir imama danışın.",
    sourcesTitle = "Metin kaynakları",
    sourcesBody = "Kur'an'ın Arapça metni: The Noble Qur'an Encyclopedia (quranenc.com). Mealler: Shaban Britch (Türkçe, quranenc.com), Saheeh International (İngilizce), Elmir Kuliev (Rusça); okunuş: Tanzil.net. Veri seti: quran-json (CC BY-SA 4.0). Ses: EveryAyah.com, kâri Mişari Raşid el-Afasi. Türkçe metinler uzman incelemesini beklemektedir.",
    back = "Geri",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Kur'an"
            Grade.SAHIH -> "Sahih hadis"
            Grade.HASAN -> "Hasen hadis"
            Grade.ATHAR -> "Sahabe / âlim sözü"
            Grade.DAIF -> "Zayıf hadis"
            Grade.DISPUTED -> "Âlimler ihtilaf etmiştir"
            Grade.NOSOURCE -> "Kaynağı yok"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Kur'an'dan bir metin."
            Grade.SAHIH -> "Hadis sahih (güvenilir) olarak değerlendirilmiştir."
            Grade.HASAN -> "Hadis hasen (iyi) olarak değerlendirilmiştir ve delil olarak kullanılır."
            Grade.ATHAR -> "Peygamber'in ﷺ hadisi değildir; sahabelerin veya âlimlerin sözüdür."
            Grade.DAIF -> "Zayıf hadis: Sünnete nispet edilemez."
            Grade.DISPUTED -> "Âlimler bu konuda farklı görüştedir. Bunu sabit bir sünnet gibi sunmayın."
            Grade.NOSOURCE -> "Kur'an'da ve sahih Sünnet'te kaynağı olmayan yaygın bir tavsiye. Kur'an okumak ve dua etmek her zaman mümkündür, ancak vaat edilen sonuç ve tekrar sayısı sabit değildir."
        }
    },
    zakat = "Zekât hesaplayıcı",
    zakatIntro = "Malınız bir kamerî yıl boyunca nisap miktarının altına düşmediyse zekât farz olur. Tutarları tek bir para biriminde girin.",
    zakatMadhhab = "Mezhep",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanefî"
            Madhhab.MALIKI -> "Mâlikî"
            Madhhab.SHAFII -> "Şâfiî"
            Madhhab.HANBALI -> "Hanbelî"
        }
    },
    zakatNisabBasis = "Nisap ölçüsü",
    nisabGold = "Altın (85 g)",
    nisabSilver = "Gümüş (595 g)",
    zakatNisabNote = "Gümüş nisabı daha düşüktür; bu yüzden daha fazla kişi zekât verir ve fakirler daha çok faydalanır. Nakit para için birçok çağdaş âlim altın nisabını esas alır. Ülkenizin din kurumu bir nisap ilan ettiyse ona uyun.",
    zakatJewelryRule = "Takılan ziynet eşyasını hesaba kat",
    zakatDebtRule = "Vadesi gelmiş borçları düş",
    zakatRulesHint = "Mezhebinizin görüşüne göre ayarlandı; her birini değiştirebilirsiniz.",
    zakatCash = "Nakit ve banka hesapları",
    zakatGold = "Altın, gram (külçe, sikke, birikim)",
    zakatGoldJewelry = "Takılan altın ziynet, gram",
    zakatSilver = "Gümüş, gram",
    zakatSilverJewelry = "Takılan gümüş ziynet, gram",
    zakatTrade = "Satış için tutulan mallar (piyasa değeri)",
    zakatReceivables = "Geri almayı beklediğiniz alacaklar",
    zakatDebts = "Şu anda ödemeniz gereken borçlar",
    zakatGoldPrice = "1 g altının fiyatı",
    zakatSilverPrice = "1 g gümüşün fiyatı",
    zakatAssets = "Zekâta tabi mal",
    zakatDeductions = "Düşülen borçlar",
    zakatNet = "Net",
    zakatNisab = "Nisap",
    zakatDue = "Ödenecek zekât (%2,5)",
    zakatNotDue = "Nisabın altında: bu maldan zekât farz değildir.",
    zakatNeedPrice = "Seçilen nisap madeninin gram fiyatını girin.",
    zakatRecipientsTitle = "Zekât kimlere verilir",
    zakatRecipients = "Fakirler, yoksullar, zekât toplamakla görevli olanlar, kalpleri İslam'a ısındırılmak istenenler, köleleri azat etmek, borçlular, Allah yolunda olanlar ve yolda kalmış yolcular (Kur'an, 9:60).",
    zakatSources = "Kaynaklar: Kur'an, 9:60, 9:103; Buhârî, 1447 (beş ukiyyeden az gümüşte zekât yoktur); Ebû Dâvûd, 1573 (altın nisabı ve bir yılın geçmesi).",
    zakatDisclaimer = "Bu yaklaşık bir hesaptır. Hayvanlar, ürünler, hisse senetleri ve ticaretin ayrıntıları dahil değildir; kendi durumunuz için bilgili bir imama danışın.",
    charitiesTitle = "Doğrulanmış kuruluşlar",
    charitiesEmpty = "Henüz doğrulanmış kuruluş eklenmedi. Bir kuruluş burada yalnızca kayıt numarası ve doğrulama tarihiyle birlikte görünür.",
    charityReg = "Kayıt",
    charityVerified = { date, who -> "Doğrulandı: $date, $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zekât"
            "sadaqah" -> "Sadaka"
            "mosque" -> "Camiler"
            "orphans" -> "Yetimler"
            "water" -> "Su kuyuları"
            "food" -> "Gıda"
            "education" -> "Eğitim"
            else -> "Genel"
        }
    },
    charityDonate = "Kuruluşun sitesinde bağış yap",
    charityWebsite = "Web sitesi",
    supportApp = "Uygulamanın kendisini desteklemek, yayımlandıktan sonra Google Play üzerinden mümkün olacak.",
)
