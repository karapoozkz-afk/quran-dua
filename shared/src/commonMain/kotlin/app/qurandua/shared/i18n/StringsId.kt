package app.qurandua.shared.i18n

import app.qurandua.shared.model.Grade
import app.qurandua.shared.zakat.Madhhab

val IdStrings = EnStrings.copy(
    translitScript = "Huruf transliterasi",
    translitScriptName = { code ->
        when (code) {
            "ru" -> "Sirilik"
            "en" -> "Latin"
            "es" -> "Spanyol"
            else -> "Otomatis (sesuai bahasa)"
        }
    },
    translitQuranNote = "Transliterasi ayat Al-Qur'an hanya tersedia dalam huruf Latin (Tanzil.net). Tidak ada abjad yang dapat menuliskan bunyi Arab dengan tepat, jadi pelajari bacaannya dengan audio.",
    appName = "Quran & Doa",
    navHome = "Beranda",
    navQuran = "Al-Qur'an",
    navDuas = "Doa",
    navSaved = "Tersimpan",
    navMore = "Lainnya",
    whatToRead = "Apa yang sebaiknya dibaca?",
    searchHint = "Ceritakan keadaan Anda: “saya punya utang”, “ayah saya meninggal”…",
    searchExamples = listOf("doa pelunas utang", "untuk almarhum ayah", "susah tidur", "sebelum bepergian", "cemas dan gelisah", "suami sakit"),
    situations = "Situasi kehidupan",
    continueReading = "Lanjutkan membaca",
    surahAyah = { s, a -> "Surah $s, ayat $a" },
    allDuas = "Semua doa",
    noResults = "Tidak ditemukan. Coba jelaskan dengan kata lain atau pilih situasi dari daftar.",
    resultsSituations = "Situasi",
    resultsDuas = "Doa",
    resultsAyahs = "Ayat Al-Qur'an",
    source = "Sumber",
    repeatTimes = { n -> "Ulangi $n kali" },
    openInQuran = "Buka di Al-Qur'an",
    share = "Bagikan",
    copy = "Salin",
    copied = "Disalin",
    listen = "Dengarkan",
    cancel = "Batal",
    audioDownload = "Audio Al-Qur'an",
    downloadingSurah = { n -> "Mengunduh surah $n" },
    audioSaved = { s -> "Tersimpan di ponsel: $s" },
    downloadAll = { s -> "Unduh seluruh Al-Qur'an ($s)" },
    downloadSurah = { s -> "Unduh surah ini ($s)" },
    surahSaved = "Surah tersimpan, bisa diputar tanpa internet",
    deleteAudio = "Hapus audio tersimpan",
    audioHint = "Yang sudah didengar disimpan di ponsel selamanya dan tidak diunduh lagi. Unduhan besar sebaiknya lewat Wi‑Fi.",
    downloadFailed = { n -> "$n ayat gagal diunduh. Ketuk lagi untuk melanjutkan." },
    downloadProgress = { d, t -> "$d dari $t ayat" },
    megabytes = "MB",
    gigabytes = "GB",
    listenSurah = "Dengarkan seluruh surah",
    ttsNote = "Suara ponsel (text-to-speech), bukan rekaman qari. Hanya panduan pelafalan: cocokkan dengan teks Arab.",
    noArabicVoice = "Ponsel ini tidak memiliki suara bahasa Arab. Anda dapat memasangnya di pengaturan text-to-speech.",
    voiceSettings = "Pengaturan suara",
    stop = "Berhenti",
    addFavorite = "Simpan",
    removeFavorite = "Hapus dari tersimpan",
    bookmark = "Penanda",
    ayahsCount = { n -> "$n ayat" },
    meccan = "Makkiyah",
    medinan = "Madaniyah",
    loadingQuran = "Menyiapkan teks Al-Qur'an (hanya saat pertama kali dibuka)…",
    savedDuas = "Doa tersimpan",
    savedAyahs = "Penanda Al-Qur'an",
    savedEmpty = "Doa dan ayat yang Anda simpan akan muncul di sini.",
    settings = "Pengaturan",
    language = "Bahasa",
    showTranslit = "Tampilkan transliterasi",
    showTranslation = "Tampilkan terjemahan",
    arabicSize = "Ukuran teks Arab",
    theme = "Tema",
    themeSystem = "Ikuti sistem",
    themeLight = "Terang",
    themeDark = "Gelap",
    donate = "Sedekah & dukungan",
    donateTitle = "Sedekah jariyah",
    donateBody = "Aplikasi ini gratis dan tanpa iklan. Donasi untuk program amal diterima melalui situs lembaga amal yang terverifikasi — tombol di bawah membuka halamannya di browser.",
    donateCharity = "Buka halaman donasi",
    donateSoon = "Halaman donasi akan segera tersedia.",
    about = "Tentang",
    aboutBody = "Setiap materi mencantumkan sumber dan derajat keabsahannya, dan perbedaan pendapat ulama ditandai. Aplikasi ini tidak mengeluarkan fatwa: untuk pertanyaan pribadi, tanyakan kepada ustaz atau imam yang berilmu.",
    sourcesTitle = "Sumber teks",
    sourcesBody = "Teks Arab Al-Qur'an: The Noble Qur'an Encyclopedia (quranenc.com). Terjemahan: Kementerian Agama RI (Indonesia, via quranenc.com), Saheeh International (Inggris), Elmir Kuliev (Rusia); transliterasi: Tanzil.net. Dataset: quran-json (CC BY-SA 4.0). Audio: EveryAyah.com, qari Mishary Rashid Alafasy. Teks berbahasa Indonesia masih menunggu pemeriksaan ahli.",
    back = "Kembali",
    gradeLabel = { g ->
        when (g) {
            Grade.QURAN -> "Al-Qur'an"
            Grade.SAHIH -> "Hadis sahih"
            Grade.HASAN -> "Hadis hasan"
            Grade.ATHAR -> "Sahabat / ulama"
            Grade.DAIF -> "Hadis dha'if (lemah)"
            Grade.DISPUTED -> "Ulama berbeda pendapat"
            Grade.NOSOURCE -> "Tanpa sumber"
        }
    },
    gradeExplain = { g ->
        when (g) {
            Grade.QURAN -> "Teks dari Al-Qur'an."
            Grade.SAHIH -> "Hadis ini dinilai sahih (otentik)."
            Grade.HASAN -> "Hadis ini dinilai hasan (baik) dan dapat dijadikan dalil."
            Grade.ATHAR -> "Ini bukan hadis Nabi ﷺ, melainkan perkataan sahabat atau ulama."
            Grade.DAIF -> "Hadis lemah: tidak boleh dinisbatkan kepada Sunnah."
            Grade.DISPUTED -> "Ulama berbeda pendapat tentang hal ini. Jangan menyajikannya sebagai Sunnah yang pasti."
            Grade.NOSOURCE -> "Saran populer yang tidak memiliki sumber dalam Al-Qur'an maupun Sunnah yang sahih. Membaca Al-Qur'an dan berdoa selalu boleh, tetapi hasil yang dijanjikan dan jumlah pengulangannya tidak memiliki dasar."
        }
    },
    zakat = "Kalkulator zakat",
    zakatIntro = "Zakat wajib bila harta tetap mencapai nisab selama satu tahun hijriah (haul). Masukkan semua jumlah dalam satu mata uang.",
    zakatMadhhab = "Mazhab",
    madhhabName = { m ->
        when (m) {
            Madhhab.HANAFI -> "Hanafi"
            Madhhab.MALIKI -> "Maliki"
            Madhhab.SHAFII -> "Syafi'i"
            Madhhab.HANBALI -> "Hanbali"
        }
    },
    zakatNisabBasis = "Nisab berdasarkan",
    nisabGold = "Emas (85 g)",
    nisabSilver = "Perak (595 g)",
    zakatNisabNote = "Nisab perak lebih rendah, sehingga lebih banyak orang yang membayar zakat dan lebih bermanfaat bagi fakir miskin; nisab emas dipakai banyak ulama kontemporer untuk uang. Jika lembaga keagamaan di negara Anda menetapkan nisab, ikutilah.",
    zakatJewelryRule = "Hitung perhiasan yang dipakai",
    zakatDebtRule = "Kurangi utang yang jatuh tempo sekarang",
    zakatRulesHint = "Disesuaikan dengan pendapat mazhab Anda; setiap pilihan dapat diubah.",
    zakatCash = "Uang tunai dan saldo bank",
    zakatGold = "Emas, gram (batangan, koin, tabungan)",
    zakatGoldJewelry = "Perhiasan emas yang dipakai, gram",
    zakatSilver = "Perak, gram",
    zakatSilverJewelry = "Perhiasan perak yang dipakai, gram",
    zakatTrade = "Barang dagangan (nilai pasar)",
    zakatReceivables = "Piutang yang Anda harapkan kembali",
    zakatDebts = "Utang yang harus dibayar sekarang",
    zakatGoldPrice = "Harga 1 g emas",
    zakatSilverPrice = "Harga 1 g perak",
    zakatAssets = "Harta wajib zakat",
    zakatDeductions = "Utang yang dikurangkan",
    zakatNet = "Bersih",
    zakatNisab = "Nisab",
    zakatDue = "Zakat yang dibayar (2,5%)",
    zakatNotDue = "Di bawah nisab: tidak wajib zakat atas harta ini.",
    zakatNeedPrice = "Masukkan harga per gram logam yang dipilih untuk nisab.",
    zakatRecipientsTitle = "Siapa yang berhak menerima zakat",
    zakatRecipients = "Orang fakir, orang miskin, amil zakat, mualaf yang dilunakkan hatinya, untuk memerdekakan hamba sahaya, orang yang berutang, di jalan Allah, dan ibnu sabil (orang yang dalam perjalanan) (QS 9:60).",
    zakatSources = "Sumber: QS 9:60, 9:103; HR. Bukhari 1447 (tidak ada zakat pada perak di bawah lima uqiyah); HR. Abu Dawud 1573 (nisab emas dan berlalunya satu tahun).",
    zakatDisclaimer = "Ini adalah perkiraan. Zakat hewan ternak, hasil pertanian, saham, dan rincian usaha tidak tercakup — tanyakan kepada ustaz atau imam yang berilmu untuk kasus Anda.",
    charitiesTitle = "Lembaga amal terverifikasi",
    charitiesEmpty = "Belum ada lembaga amal terverifikasi yang ditambahkan. Lembaga hanya muncul di sini beserta nomor registrasi dan tanggal pemeriksaannya.",
    charityReg = "Registrasi",
    charityVerified = { date, who -> "Diperiksa $date oleh $who" },
    charityPurpose = { p ->
        when (p) {
            "zakat" -> "Zakat"
            "sadaqah" -> "Sedekah"
            "mosque" -> "Masjid"
            "orphans" -> "Anak yatim"
            "water" -> "Sumur air"
            "food" -> "Pangan"
            "education" -> "Pendidikan"
            else -> "Umum"
        }
    },
    charityDonate = "Donasi di situs lembaga amal",
    charityWebsite = "Situs web",
    supportApp = "Dukungan untuk aplikasi ini sendiri akan tersedia melalui Google Play setelah dipublikasikan.",
)
