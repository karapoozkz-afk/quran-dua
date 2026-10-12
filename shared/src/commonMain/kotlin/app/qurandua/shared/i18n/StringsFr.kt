package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun frAyahs(n: Int) = if (n <= 1) "$n verset" else "$n versets"

val FrStrings = EnStrings.copy(
    translitScript = "Lettres de translittération",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Cyrillique"
            "en" -> "Latin"
            "es" -> "Espagnol"
            else -> "Automatique (selon la langue)"
        }
    },
    translitQuranNote = "Les versets du Coran sont translittérés uniquement en lettres latines (Tanzil.net). Aucun alphabet ne rend exactement les sons arabes : apprenez la récitation avec l'audio.",
    appName = "Coran et Dou'a",
    navHome = "Accueil",
    navQuran = "Coran",
    navDuas = "Dou'as",
    navSaved = "Favoris",
    navMore = "Plus",
    whatToRead = "Que lire ?",
    searchHint = "Décrivez votre situation : « j'ai des dettes », « mon père est décédé »…",
    searchExamples = listOf("dou'a contre les dettes", "pour mon père décédé", "je n'arrive pas à dormir", "avant un voyage", "je suis angoissé", "mon mari est malade"),
    situations = "Situations de la vie",
    continueReading = "Reprendre la lecture",
    surahAyah = { s, a -> "Sourate $s, verset $a" },
    allDuas = "Toutes les dou'as",
    noResults = "Aucun résultat. Essayez de décrire la situation avec d'autres mots ou choisissez-la dans la liste.",
    resultsSituations = "Situations",
    resultsDuas = "Dou'as",
    resultsAyahs = "Versets du Coran",
    source = "Source",
    repeatTimes = { n -> "Répéter $n fois" },
    openInQuran = "Ouvrir dans le Coran",
    share = "Partager",
    copy = "Copier",
    copied = "Copié",
    listen = "Écouter",
    cancel = "Annuler",
    audioDownload = "Audio du Coran",
    downloadingSurah = { n -> "Téléchargement de la sourate $n" },
    audioSaved = { s -> "Enregistré sur le téléphone : $s" },
    downloadAll = { s -> "Télécharger tout le Coran ($s)" },
    downloadSurah = { s -> "Télécharger cette sourate ($s)" },
    surahSaved = "Sourate enregistrée, lecture sans internet",
    deleteAudio = "Supprimer l'audio enregistré",
    audioHint = "Ce que vous écoutez reste enregistré sur le téléphone et n'est plus jamais retéléchargé. Il vaut mieux faire les gros téléchargements en Wi‑Fi.",
    downloadFailed = { n -> "Versets non téléchargés : $n. Appuyez de nouveau pour terminer." },
    downloadProgress = { d, t -> "$d sur $t versets" },
    megabytes = "Mo",
    gigabytes = "Go",
    listenSurah = "Écouter toute la sourate",
    ttsNote = "Voix du téléphone (synthèse vocale), pas l'enregistrement d'un récitateur. Simple repère de prononciation : vérifiez avec le texte arabe.",
    noArabicVoice = "Le téléphone n'a pas de voix arabe. Vous pouvez en installer une dans les paramètres de synthèse vocale.",
    voiceSettings = "Paramètres de la voix",
    stop = "Arrêter",
    addFavorite = "Enregistrer",
    removeFavorite = "Retirer des favoris",
    bookmark = "Signet",
    ayahsCount = ::frAyahs,
    meccan = "Mecquoise",
    medinan = "Médinoise",
    loadingQuran = "Préparation du texte du Coran (au premier lancement seulement)…",
    savedDuas = "Dou'as enregistrées",
    savedAyahs = "Signets du Coran",
    savedEmpty = "Les dou'as et les versets que vous enregistrez apparaîtront ici.",
    settings = "Paramètres",
    language = "Langue",
    showTranslit = "Afficher la translittération",
    showTranslation = "Afficher la traduction",
    arabicSize = "Taille du texte arabe",
    theme = "Thème",
    themeSystem = "Système",
    themeLight = "Clair",
    themeDark = "Sombre",
    donate = "Sadaqa et soutien",
    donateTitle = "Sadaqa jariya",
    donateBody = "L'application est gratuite et sans publicité. Les dons aux projets caritatifs se font sur les sites d'organisations vérifiées ; le bouton ci-dessous ouvre la page dans le navigateur.",
    donateCharity = "Ouvrir la page des dons",
    donateSoon = "La page des dons sera bientôt disponible.",
    about = "À propos",
    aboutBody = "Chaque contenu indique sa source et son degré d'authenticité ; les divergences entre savants sont signalées. L'application ne délivre pas de fatwas : pour vos questions personnelles, adressez-vous à un imam compétent.",
    sourcesTitle = "Sources des textes",
    sourcesBody = "Texte arabe du Coran : The Noble Qur'an Encyclopedia (quranenc.com). Traductions : Muhammad Hamidullah (français), Saheeh International (anglais), Elmir Kouliev (russe) ; translittération : Tanzil.net. Jeu de données : quran-json (CC BY-SA 4.0). Audio : EveryAyah.com, récitateur Mishary Rashid Alafasy. Les textes français sont en attente de relecture par un spécialiste.",
    back = "Retour",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Coran"
            Grade.SAHIH -> "Hadith authentique"
            Grade.HASAN -> "Hadith bon (hasan)"
            Grade.ATHAR -> "Compagnons / savants"
            Grade.DAIF -> "Hadith faible"
            Grade.DISPUTED -> "Les savants divergent"
            Grade.NOSOURCE -> "Sans source"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Texte tiré du Coran."
            Grade.SAHIH -> "Le hadith est jugé authentique (sahih)."
            Grade.HASAN -> "Le hadith est jugé bon (hasan) et sert de preuve."
            Grade.ATHAR -> "Ce n'est pas un hadith du Prophète ﷺ, mais des paroles de Compagnons ou de savants."
            Grade.DAIF -> "Hadith faible : on ne doit pas l'attribuer à la Sunna."
            Grade.DISPUTED -> "Les savants divergent à ce sujet. Ne le présentez pas comme une Sunna établie."
            Grade.NOSOURCE -> "Conseil répandu qui n'a pas de source dans le Coran ni dans la Sunna authentique. On peut toujours lire le Coran et invoquer Allah, mais le résultat promis et le nombre de répétitions ne sont pas établis."
        }
    },
    zakat = "Calculateur de zakat",
    zakatIntro = "La zakat est due lorsqu'une année lunaire s'est écoulée sans que vos biens soient descendus sous le nisab. Saisissez les montants dans une seule devise.",
    zakatMadhhab = "École juridique (madhhab)",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafite"
            Madhhab.MALIKI -> "Malikite"
            Madhhab.SHAFII -> "Chafiite"
            Madhhab.HANBALI -> "Hanbalite"
        }
    },
    zakatNisabBasis = "Nisab selon",
    nisabGold = "Or (85 g)",
    nisabSilver = "Argent (595 g)",
    zakatNisabNote = "Le nisab de l'argent est plus bas : davantage de personnes paient la zakat et les pauvres en profitent plus. Pour l'argent liquide, de nombreux savants contemporains retiennent le nisab de l'or. Si l'autorité religieuse de votre pays annonce un nisab, suivez-le.",
    zakatJewelryRule = "Compter les bijoux portés",
    zakatDebtRule = "Déduire les dettes exigibles",
    zakatRulesHint = "Réglé selon la position de votre école ; vous pouvez modifier chaque option.",
    zakatCash = "Espèces et comptes bancaires",
    zakatGold = "Or, en grammes (lingots, pièces, épargne)",
    zakatGoldJewelry = "Bijoux en or portés, en grammes",
    zakatSilver = "Argent (métal), en grammes",
    zakatSilverJewelry = "Bijoux en argent portés, en grammes",
    zakatTrade = "Marchandises destinées à la vente (valeur marchande)",
    zakatReceivables = "Créances que vous comptez récupérer",
    zakatDebts = "Dettes à payer maintenant",
    zakatGoldPrice = "Prix de 1 g d'or",
    zakatSilverPrice = "Prix de 1 g d'argent",
    zakatAssets = "Biens soumis à la zakat",
    zakatDeductions = "Dettes déduites",
    zakatNet = "Net",
    zakatNisab = "Nisab",
    zakatDue = "Zakat à payer (2,5 %)",
    zakatNotDue = "En dessous du nisab : aucune zakat n'est due sur ces biens.",
    zakatNeedPrice = "Saisissez le prix au gramme du métal choisi pour le nisab.",
    zakatRecipientsTitle = "Qui peut recevoir la zakat",
    zakatRecipients = "Les pauvres, les indigents, ceux qui la collectent, ceux dont les cœurs sont à gagner, l'affranchissement des captifs, les endettés, la cause d'Allah et le voyageur en détresse (Coran, 9:60).",
    zakatSources = "Sources : Coran, 9:60, 9:103 ; al-Boukhari, 1447 (pas de zakat en dessous de cinq onces d'argent) ; Abou Dawoud, 1573 (nisab de l'or et écoulement d'une année).",
    zakatDisclaimer = "Ce calcul est approximatif. Le bétail, les récoltes, les actions et les particularités du commerce ne sont pas pris en compte : pour votre cas, consultez un imam compétent.",
    charitiesTitle = "Organisations vérifiées",
    charitiesEmpty = "Aucune organisation vérifiée n'a encore été ajoutée. Une organisation n'apparaît ici qu'avec son numéro d'enregistrement et la date de sa vérification.",
    charityReg = "Enregistrement",
    charityVerified = { date, who -> "Vérifié le $date par $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zakat"
            "sadaqah" -> "Sadaqa"
            "mosque" -> "Mosquées"
            "orphans" -> "Orphelins"
            "water" -> "Puits"
            "food" -> "Alimentation"
            "education" -> "Éducation"
            else -> "Général"
        }
    },
    charityDonate = "Faire un don sur le site de l'organisation",
    charityWebsite = "Site web",
    supportApp = "Il sera possible de soutenir l'application elle-même via Google Play après sa publication.",
)
