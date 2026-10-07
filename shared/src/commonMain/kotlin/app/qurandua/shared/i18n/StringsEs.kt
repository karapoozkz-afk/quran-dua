package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

private fun esAyahs(n: Int) = if (n == 1) "1 aleya" else "$n aleyas"

val EsStrings = EnStrings.copy(
    translitScript = "Letras de la transliteración",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Cirílico"
            "en" -> "Latino (inglés)"
            "es" -> "Español"
            else -> "Automático (según el idioma)"
        }
    },
    translitQuranNote = "Las aleyas del Corán se transliteran solo con letras latinas (Tanzil.net). Ningún alfabeto reproduce exactamente los sonidos del árabe; aprende la recitación con el audio.",
    appName = "Corán y Dua",
    navHome = "Inicio",
    navQuran = "Corán",
    navDuas = "Duas",
    navSaved = "Guardados",
    navMore = "Más",
    whatToRead = "¿Qué debo leer?",
    searchHint = "Describe tu situación: «tengo deudas», «murió mi padre»…",
    searchExamples = listOf("dua para las deudas", "por mi padre fallecido", "no puedo dormir", "antes de viajar", "tengo ansiedad", "mi esposo está enfermo"),
    situations = "Situaciones de la vida",
    continueReading = "Seguir leyendo",
    surahAyah = { s, a -> "Sura $s, aleya $a" },
    allDuas = "Todas las duas",
    noResults = "No se encontró nada. Intenta describirlo con otras palabras o elige una situación de la lista.",
    resultsSituations = "Situaciones",
    resultsDuas = "Duas",
    resultsAyahs = "Aleyas del Corán",
    source = "Fuente",
    repeatTimes = { n -> "Repetir $n veces" },
    openInQuran = "Abrir en el Corán",
    share = "Compartir",
    copy = "Copiar",
    copied = "Copiado",
    listen = "Escuchar",
    cancel = "Cancelar",
    audioDownload = "Audio del Corán",
    downloadingSurah = { n -> "Descargando la sura $n" },
    audioSaved = { s -> "Guardado en el teléfono: $s" },
    downloadAll = { s -> "Descargar todo el Corán ($s)" },
    downloadSurah = { s -> "Descargar esta sura ($s)" },
    surahSaved = "Sura guardada, suena sin internet",
    deleteAudio = "Borrar el audio guardado",
    audioHint = "Lo que escucha se guarda en el teléfono para siempre y no se vuelve a descargar. Las descargas grandes, mejor por Wi‑Fi.",
    downloadFailed = { n -> "No se descargaron $n aleyas. Pulse otra vez para completar." },
    downloadProgress = { d, t -> "$d de $t aleyas" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "Escuchar toda la sura",
    ttsNote = "Voz del teléfono (síntesis de voz), no la grabación de un recitador. Solo orienta la pronunciación: compárela con el texto árabe.",
    noArabicVoice = "El teléfono no tiene voz en árabe. Puede instalarla en los ajustes de síntesis de voz.",
    voiceSettings = "Ajustes de voz",
    stop = "Detener",
    addFavorite = "Guardar",
    removeFavorite = "Quitar de guardados",
    bookmark = "Marcador",
    ayahsCount = ::esAyahs,
    meccan = "Mecana",
    medinan = "Medinense",
    loadingQuran = "Preparando el texto del Corán (solo la primera vez)…",
    savedDuas = "Duas guardadas",
    savedAyahs = "Marcadores del Corán",
    savedEmpty = "Las duas y aleyas que guardes aparecerán aquí.",
    settings = "Ajustes",
    language = "Idioma",
    showTranslit = "Mostrar transliteración",
    showTranslation = "Mostrar traducción",
    arabicSize = "Tamaño del texto árabe",
    theme = "Tema",
    themeSystem = "Sistema",
    themeLight = "Claro",
    themeDark = "Oscuro",
    donate = "Sadaqa y apoyo",
    donateTitle = "Sadaqa yariya",
    donateBody = "Esta aplicación es gratuita y sin anuncios. Las donaciones a proyectos benéficos se reciben en los sitios web de organizaciones verificadas; el botón de abajo abre la página en tu navegador.",
    donateCharity = "Abrir la página de donaciones",
    donateSoon = "La página de donaciones estará disponible pronto.",
    about = "Acerca de",
    aboutBody = "Cada elemento muestra su fuente y su grado de autenticidad, y se señalan las diferencias de opinión entre los sabios. La aplicación no emite fatwas: para preguntas personales, consulta a un imam con conocimiento.",
    sourcesTitle = "Fuentes de los textos",
    sourcesBody = "Texto árabe del Corán: The Noble Qur'an Encyclopedia (quranenc.com). Traducciones: Muhammad Isa García (español, Tanzil.net), Saheeh International (inglés), Elmir Kuliev (ruso); transliteración: Tanzil.net. Conjunto de datos: quran-json (CC BY-SA 4.0). Audio: EveryAyah.com, recitador Mishary Rashid Alafasy. Los textos en español están pendientes de revisión por un especialista.",
    back = "Atrás",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Corán"
            Grade.SAHIH -> "Hadiz auténtico"
            Grade.HASAN -> "Hadiz bueno (hasan)"
            Grade.ATHAR -> "Compañeros / sabios"
            Grade.DAIF -> "Hadiz débil"
            Grade.DISPUTED -> "Los sabios difieren"
            Grade.NOSOURCE -> "Sin fuente"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Texto del Corán."
            Grade.SAHIH -> "El hadiz está calificado como auténtico (sahih)."
            Grade.HASAN -> "El hadiz está calificado como bueno (hasan) y se usa como prueba."
            Grade.ATHAR -> "No es un hadiz del Profeta ﷺ: son palabras de los Compañeros o de los sabios."
            Grade.DAIF -> "Hadiz débil: no debe atribuirse a la Sunna."
            Grade.DISPUTED -> "Los sabios difieren sobre esto. No lo presentes como Sunna establecida."
            Grade.NOSOURCE -> "Consejo popular sin fuente en el Corán ni en la Sunna auténtica. Siempre puedes recitar el Corán y hacer dua, pero el resultado prometido y el número de repeticiones no están establecidos."
        }
    },
    zakat = "Calculadora de zakat",
    zakatIntro = "El zakat es obligatorio cuando ha pasado un año lunar en el que tus bienes se mantuvieron en el nisab o por encima. Introduce las cantidades en una sola moneda.",
    zakatMadhhab = "Escuela jurídica (madhab)",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafí"
            Madhhab.MALIKI -> "Malikí"
            Madhhab.SHAFII -> "Shafi‘í"
            Madhhab.HANBALI -> "Hanbalí"
        }
    },
    zakatNisabBasis = "Nisab según",
    nisabGold = "Oro (85 g)",
    nisabSilver = "Plata (595 g)",
    zakatNisabNote = "El nisab de la plata es más bajo, así que paga más gente y los pobres se benefician más; muchos sabios contemporáneos usan el nisab del oro para el dinero en efectivo. Si la autoridad religiosa de tu país anuncia un nisab, síguelo.",
    zakatJewelryRule = "Contar las joyas que usas",
    zakatDebtRule = "Restar las deudas que vencen ahora",
    zakatRulesHint = "Ajustado según la postura de tu escuela; puedes cambiar cada opción.",
    zakatCash = "Efectivo y saldos bancarios",
    zakatGold = "Oro, gramos (lingotes, monedas, ahorros)",
    zakatGoldJewelry = "Joyas de oro que usas, gramos",
    zakatSilver = "Plata, gramos",
    zakatSilverJewelry = "Joyas de plata que usas, gramos",
    zakatTrade = "Mercancías destinadas a la venta (valor de mercado)",
    zakatReceivables = "Dinero que te deben y esperas recuperar",
    zakatDebts = "Deudas que debes pagar ahora",
    zakatGoldPrice = "Precio de 1 g de oro",
    zakatSilverPrice = "Precio de 1 g de plata",
    zakatAssets = "Bienes sujetos a zakat",
    zakatDeductions = "Deudas restadas",
    zakatNet = "Neto",
    zakatNisab = "Nisab",
    zakatDue = "Zakat a pagar (2,5 %)",
    zakatNotDue = "Por debajo del nisab: no hay zakat obligatorio sobre estos bienes.",
    zakatNeedPrice = "Introduce el precio por gramo del metal elegido para el nisab.",
    zakatRecipientsTitle = "Quién puede recibir el zakat",
    zakatRecipients = "Los pobres, los necesitados, los encargados de administrarlo, aquellos cuyos corazones se quiere ganar, la liberación de cautivos, los endeudados, la causa de Allah y el viajero que se ha quedado sin recursos (Corán, 9:60).",
    zakatSources = "Fuentes: Corán, 9:60, 9:103; al-Bujari, 1447 (no hay zakat en menos de cinco awaq de plata); Abu Dawud, 1573 (nisab del oro y el paso de un año completo).",
    zakatDisclaimer = "Es un cálculo aproximado. No incluye el ganado, las cosechas, las acciones ni los detalles del comercio; consulta a un imam con conocimiento sobre tu caso.",
    charitiesTitle = "Organizaciones verificadas",
    charitiesEmpty = "Aún no se han añadido organizaciones verificadas. Cada organización aparece aquí solo con su número de registro y la fecha en que se verificó.",
    charityReg = "Registro",
    charityVerified = { date, who -> "Verificado el $date por $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zakat"
            "sadaqah" -> "Sadaqa"
            "mosque" -> "Mezquitas"
            "orphans" -> "Huérfanos"
            "water" -> "Pozos de agua"
            "food" -> "Alimentos"
            "education" -> "Educación"
            else -> "General"
        }
    },
    charityDonate = "Donar en el sitio web de la organización",
    charityWebsite = "Sitio web",
    supportApp = "Apoyar a la propia aplicación será posible a través de Google Play cuando se publique.",
)
