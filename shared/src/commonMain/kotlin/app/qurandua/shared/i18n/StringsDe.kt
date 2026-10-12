package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun deAyahs(n: Int) = if (n == 1) "1 Vers" else "$n Verse"

val DeStrings = EnStrings.copy(
    translitScript = "Buchstaben der Umschrift",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Kyrillisch"
            "en" -> "Lateinisch"
            "es" -> "Spanisch"
            else -> "Automatisch (nach Sprache)"
        }
    },
    translitQuranNote = "Die Umschrift der Koranverse gibt es nur in lateinischen Buchstaben (Tanzil.net). Kein Alphabet gibt die arabischen Laute genau wieder; lernen Sie die Rezitation anhand der Audioaufnahme.",
    appName = "Koran & Du'a",
    navHome = "Start",
    navQuran = "Koran",
    navDuas = "Bittgebete",
    navSaved = "Gespeichert",
    navMore = "Mehr",
    whatToRead = "Was soll ich lesen?",
    searchHint = "Beschreiben Sie Ihre Lage: „ich habe Schulden“, „mein Vater ist gestorben“…",
    searchExamples = listOf("Du'a gegen Schulden", "für meinen verstorbenen Vater", "kann nicht schlafen", "vor der Reise", "ich habe Angst", "mein Mann ist krank"),
    situations = "Lebenssituationen",
    continueReading = "Weiterlesen",
    surahAyah = { s, a -> "Sure $s, Vers $a" },
    allDuas = "Alle Bittgebete",
    noResults = "Nichts gefunden. Beschreiben Sie Ihre Lage mit anderen Worten oder wählen Sie eine Situation aus der Liste.",
    resultsSituations = "Situationen",
    resultsDuas = "Bittgebete",
    resultsAyahs = "Koranverse",
    source = "Quelle",
    repeatTimes = { n -> "${n}-mal wiederholen" },
    openInQuran = "Im Koran öffnen",
    share = "Teilen",
    copy = "Kopieren",
    copied = "Kopiert",
    listen = "Anhören",
    cancel = "Abbrechen",
    audioDownload = "Koran-Audio",
    downloadingSurah = { n -> "Sure $n wird heruntergeladen" },
    audioSaved = { s -> "Auf dem Handy gespeichert: $s" },
    downloadAll = { s -> "Den ganzen Koran herunterladen ($s)" },
    downloadSurah = { s -> "Diese Sure herunterladen ($s)" },
    surahSaved = "Sure gespeichert, läuft ohne Internet",
    deleteAudio = "Gespeichertes Audio löschen",
    audioHint = "Alles, was Sie anhören, bleibt dauerhaft auf dem Handy gespeichert und wird nicht erneut heruntergeladen. Große Downloads besser über WLAN.",
    downloadFailed = { n -> "$n Verse wurden nicht heruntergeladen. Tippen Sie erneut, um den Download abzuschließen." },
    downloadProgress = { d, t -> "$d von $t Versen" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "Ganze Sure anhören",
    ttsNote = "Stimme des Handys (Sprachausgabe), keine Aufnahme eines Rezitators. Nur als Hilfe zur Aussprache: Vergleichen Sie mit dem arabischen Text.",
    noArabicVoice = "Auf dem Handy ist keine arabische Stimme installiert. Sie können eine in den Einstellungen der Sprachausgabe hinzufügen.",
    voiceSettings = "Spracheinstellungen",
    stop = "Stopp",
    addFavorite = "Speichern",
    removeFavorite = "Aus Gespeichertem entfernen",
    bookmark = "Lesezeichen",
    ayahsCount = ::deAyahs,
    meccan = "Mekkanisch",
    medinan = "Medinensisch",
    loadingQuran = "Der Korantext wird vorbereitet (nur beim ersten Start)…",
    savedDuas = "Gespeicherte Bittgebete",
    savedAyahs = "Lesezeichen im Koran",
    savedEmpty = "Hier erscheinen die Bittgebete und Verse, die Sie speichern.",
    settings = "Einstellungen",
    language = "Sprache",
    showTranslit = "Umschrift anzeigen",
    showTranslation = "Übersetzung anzeigen",
    arabicSize = "Größe des arabischen Textes",
    theme = "Design",
    themeSystem = "System",
    themeLight = "Hell",
    themeDark = "Dunkel",
    donate = "Sadaqa und Unterstützung",
    donateTitle = "Sadaqa Dschariya (fortlaufende Spende)",
    donateBody = "Die App ist kostenlos und werbefrei. Spenden für wohltätige Projekte werden auf den Websites geprüfter Organisationen angenommen – der Knopf unten öffnet die Seite im Browser.",
    donateCharity = "Spendenseite öffnen",
    donateSoon = "Die Spendenseite folgt in Kürze.",
    about = "Über die App",
    aboutBody = "Bei jedem Inhalt sind Quelle und Grad der Authentizität angegeben; Meinungsverschiedenheiten der Gelehrten sind gekennzeichnet. Die App erteilt keine Fatwas: Wenden Sie sich mit persönlichen Fragen an einen kundigen Imam.",
    sourcesTitle = "Textquellen",
    sourcesBody = "Arabischer Korantext: The Noble Qur'an Encyclopedia (quranenc.com). Übersetzungen der Bedeutungen: Frank Bubenheim und Nadeem Elyas (Deutsch, über fawazahmed0/quran-api), Saheeh International (Englisch), Elmir Kuliev (Russisch); Umschrift: Tanzil.net. Datensatz: quran-json (CC BY-SA 4.0). Audio: EveryAyah.com, Rezitator Mishary Rashid Alafasy. Die deutschen Texte warten noch auf die Prüfung durch einen Fachkundigen.",
    back = "Zurück",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Koran"
            Grade.SAHIH -> "Authentischer Hadith (sahih)"
            Grade.HASAN -> "Guter Hadith (hasan)"
            Grade.ATHAR -> "Prophetengefährten / Gelehrte"
            Grade.DAIF -> "Schwacher Hadith"
            Grade.DISPUTED -> "Gelehrte sind uneins"
            Grade.NOSOURCE -> "Ohne Quelle"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Text aus dem Koran."
            Grade.SAHIH -> "Der Hadith gilt als authentisch (sahih)."
            Grade.HASAN -> "Der Hadith gilt als gut (hasan) und wird als Beleg verwendet."
            Grade.ATHAR -> "Kein Hadith des Propheten ﷺ, sondern Worte der Prophetengefährten oder Gelehrten."
            Grade.DAIF -> "Schwacher Hadith: Er darf nicht der Sunna zugeschrieben werden."
            Grade.DISPUTED -> "Die Gelehrten sind sich hierin uneins. Stellen Sie es nicht als feststehende Sunna dar."
            Grade.NOSOURCE -> "Ein verbreiteter Ratschlag ohne Quelle im Koran und in der authentischen Sunna. Koran lesen und Bittgebete sprechen kann man immer, aber das versprochene Ergebnis und die Zahl der Wiederholungen sind nicht belegt."
        }
    },
    zakat = "Zakat-Rechner",
    zakatIntro = "Die Zakat wird fällig, wenn Ihr Vermögen ein ganzes Mondjahr lang den Nisab nicht unterschritten hat. Geben Sie alle Beträge in einer Währung ein.",
    zakatMadhhab = "Rechtsschule (Madhhab)",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafitisch"
            Madhhab.MALIKI -> "Malikitisch"
            Madhhab.SHAFII -> "Schafiitisch"
            Madhhab.HANBALI -> "Hanbalitisch"
        }
    },
    zakatNisabBasis = "Nisab nach",
    nisabGold = "Gold (85 g)",
    nisabSilver = "Silber (595 g)",
    zakatNisabNote = "Der Silber-Nisab ist niedriger: So zahlen mehr Menschen Zakat und die Armen profitieren mehr. Für Bargeld legen viele heutige Gelehrte den Gold-Nisab zugrunde. Wenn die religiöse Behörde Ihres Landes einen Nisab bekannt gibt, richten Sie sich danach.",
    zakatJewelryRule = "Getragenen Schmuck mitrechnen",
    zakatDebtRule = "Fällige Schulden abziehen",
    zakatRulesHint = "Nach der Position Ihrer Rechtsschule eingestellt; Sie können jede Option ändern.",
    zakatCash = "Bargeld und Bankguthaben",
    zakatGold = "Gold, Gramm (Barren, Münzen, Ersparnisse)",
    zakatGoldJewelry = "Getragener Goldschmuck, Gramm",
    zakatSilver = "Silber, Gramm",
    zakatSilverJewelry = "Getragener Silberschmuck, Gramm",
    zakatTrade = "Waren zum Verkauf (Marktwert)",
    zakatReceivables = "Forderungen, die Sie zurückerwarten",
    zakatDebts = "Schulden, die Sie jetzt zahlen müssen",
    zakatGoldPrice = "Preis für 1 g Gold",
    zakatSilverPrice = "Preis für 1 g Silber",
    zakatAssets = "Zakatpflichtiges Vermögen",
    zakatDeductions = "Abgezogene Schulden",
    zakatNet = "Netto",
    zakatNisab = "Nisab",
    zakatDue = "Zu zahlende Zakat (2,5 %)",
    zakatNotDue = "Unter dem Nisab: Auf dieses Vermögen ist keine Zakat fällig.",
    zakatNeedPrice = "Geben Sie den Grammpreis des gewählten Nisab-Metalls ein.",
    zakatRecipientsTitle = "Wer Zakat erhalten darf",
    zakatRecipients = "Die Armen, die Bedürftigen, die mit der Zakat Beauftragten, diejenigen, deren Herzen gewonnen werden sollen, der Freikauf von Sklaven, die Verschuldeten, auf dem Weg Allahs und der gestrandete Reisende (Koran 9:60).",
    zakatSources = "Quellen: Koran 9:60, 9:103; al-Buchari 1447 (keine Zakat auf weniger als fünf Uqiya Silber); Abu Dawud 1573 (Gold-Nisab und Ablauf eines Jahres).",
    zakatDisclaimer = "Dies ist eine ungefähre Berechnung. Vieh, Ernteerträge, Aktien und Einzelheiten des Handels sind nicht berücksichtigt – fragen Sie für Ihren Fall einen kundigen Imam.",
    charitiesTitle = "Geprüfte Organisationen",
    charitiesEmpty = "Es wurden noch keine geprüften Organisationen hinzugefügt. Eine Organisation erscheint hier nur mit ihrer Registernummer und dem Datum der Prüfung.",
    charityReg = "Registernummer",
    charityVerified = { date, who -> "Geprüft am $date von $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zakat"
            "sadaqah" -> "Sadaqa"
            "mosque" -> "Moscheen"
            "orphans" -> "Waisen"
            "water" -> "Brunnen"
            "food" -> "Lebensmittel"
            "education" -> "Bildung"
            else -> "Allgemein"
        }
    },
    charityDonate = "Auf der Website der Organisation spenden",
    charityWebsite = "Website",
    supportApp = "Die App selbst kann nach ihrer Veröffentlichung über Google Play unterstützt werden.",
)
