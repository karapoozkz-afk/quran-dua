package app.qurandua.shared.i18n

/**
 * Texts of the "Support the project" block (Google Play billing). The body says plainly that the
 * money goes to the developer, so nobody mistakes it for zakat or a donation to a charity.
 */
data class SupportStrings(
    val title: String,
    val body: String,
    val thanks: String,
    val pending: String,
    val failed: String,
    val unavailable: String,
)

private val En = SupportStrings(
    title = "Support the project",
    body = "The app is free and has no ads. If it helps you, you can support its development through Google Play. The payment goes to the app's developer: it is not zakat and not a donation to a charity.",
    thanks = "Thank you! May Allah reward you with good.",
    pending = "The payment is waiting for confirmation and will go through on its own.",
    failed = "The payment did not go through. Please try again later.",
    unavailable = "Support is available in the app installed from Google Play.",
)

private val Ru = SupportStrings(
    title = "Поддержать проект",
    body = "Приложение бесплатное и без рекламы. Если оно вам помогает, вы можете поддержать его развитие через Google Play. Платёж уходит разработчику приложения: это не закят и не пожертвование фонду.",
    thanks = "Спасибо! Да вознаградит вас Аллах благом.",
    pending = "Платёж ждёт подтверждения и пройдёт сам.",
    failed = "Платёж не прошёл. Попробуйте позже.",
    unavailable = "Поддержка доступна в приложении, установленном из Google Play.",
)

private val Kk = SupportStrings(
    title = "Жобаны қолдау",
    body = "Қосымша тегін және жарнамасыз. Егер ол сізге пайдалы болса, оның дамуын Google Play арқылы қолдай аласыз. Төлем қосымша әзірлеушісіне түседі: бұл зекет те, қорға садақа да емес.",
    thanks = "Рақмет! Алла сізге жақсылығымен қайтарсын.",
    pending = "Төлем растауды күтуде және өзі өтеді.",
    failed = "Төлем өтпеді. Кейінірек қайталап көріңіз.",
    unavailable = "Қолдау Google Play-ден орнатылған қосымшада қолжетімді.",
)

private val Ky = SupportStrings(
    title = "Долбоорду колдоо",
    body = "Тиркеме акысыз жана жарнамасыз. Эгер ал сизге пайдалуу болсо, анын өнүгүшүн Google Play аркылуу колдой аласыз. Төлөм тиркеменин иштеп чыгуучусуна түшөт: бул зекет да, фондго садака да эмес.",
    thanks = "Рахмат! Алла сизге жакшылык менен кайтарсын.",
    pending = "Төлөм ырастоону күтүүдө жана өзү өтөт.",
    failed = "Төлөм өткөн жок. Кийинчерээк кайра аракет кылыңыз.",
    unavailable = "Колдоо Google Play'ден орнотулган тиркемеде жеткиликтүү.",
)

private val Uz = SupportStrings(
    title = "Loyihani qo‘llab-quvvatlash",
    body = "Ilova bepul va reklamasiz. Agar u sizga foydali bo‘lsa, uning rivojini Google Play orqali qo‘llab-quvvatlashingiz mumkin. To‘lov ilova dasturchisiga tushadi: bu zakot ham, jamg‘armaga xayriya ham emas.",
    thanks = "Rahmat! Alloh sizni yaxshilik bilan mukofotlasin.",
    pending = "To‘lov tasdiqlanishini kutmoqda va o‘zi o‘tadi.",
    failed = "To‘lov o‘tmadi. Keyinroq qayta urinib ko‘ring.",
    unavailable = "Qo‘llab-quvvatlash Google Play’dan o‘rnatilgan ilovada mavjud.",
)

private val Ar = SupportStrings(
    title = "ادعم المشروع",
    body = "التطبيق مجاني وبلا إعلانات. إن كان ينفعك فيمكنك دعم تطويره عبر Google Play. يذهب المبلغ إلى مطوّر التطبيق، فهو ليس زكاة ولا تبرعًا لجمعية خيرية.",
    thanks = "شكرًا لك! جزاك الله خيرًا.",
    pending = "الدفع بانتظار التأكيد وسيتم تلقائيًا.",
    failed = "لم تتم عملية الدفع. حاول لاحقًا.",
    unavailable = "الدعم متاح في التطبيق المثبّت من Google Play.",
)

private val Tr = SupportStrings(
    title = "Projeye destek ol",
    body = "Uygulama ücretsiz ve reklamsızdır. Size faydalı oluyorsa gelişimini Google Play üzerinden destekleyebilirsiniz. Ödeme uygulamanın geliştiricisine gider: zekât ya da bir kuruluşa bağış değildir.",
    thanks = "Teşekkürler! Allah sizi hayırla mükâfatlandırsın.",
    pending = "Ödeme onay bekliyor ve kendiliğinden tamamlanacak.",
    failed = "Ödeme gerçekleşmedi. Lütfen daha sonra tekrar deneyin.",
    unavailable = "Destek, Google Play'den yüklenen uygulamada kullanılabilir.",
)

private val Id = SupportStrings(
    title = "Dukung proyek ini",
    body = "Aplikasi ini gratis dan tanpa iklan. Jika bermanfaat bagi Anda, Anda dapat mendukung pengembangannya melalui Google Play. Pembayaran diterima oleh pengembang aplikasi: ini bukan zakat dan bukan donasi untuk lembaga amal.",
    thanks = "Terima kasih! Semoga Allah membalas Anda dengan kebaikan.",
    pending = "Pembayaran menunggu konfirmasi dan akan selesai dengan sendirinya.",
    failed = "Pembayaran gagal. Silakan coba lagi nanti.",
    unavailable = "Dukungan tersedia di aplikasi yang dipasang dari Google Play.",
)

private val Ur = SupportStrings(
    title = "منصوبے کی مدد کریں",
    body = "یہ ایپ مفت ہے اور اس میں اشتہارات نہیں ہیں۔ اگر یہ آپ کے کام آتی ہے تو آپ Google Play کے ذریعے اس کی ترقی میں مدد کر سکتے ہیں۔ یہ رقم ایپ کے ڈویلپر کو جاتی ہے: یہ زکوٰۃ یا کسی خیراتی ادارے کو عطیہ نہیں ہے۔",
    thanks = "شکریہ! اللہ آپ کو جزائے خیر دے۔",
    pending = "ادائیگی تصدیق کی منتظر ہے اور خود مکمل ہو جائے گی۔",
    failed = "ادائیگی نہیں ہو سکی۔ بعد میں دوبارہ کوشش کریں۔",
    unavailable = "مدد کا اختیار Google Play سے انسٹال کی گئی ایپ میں دستیاب ہے۔",
)

private val Es = SupportStrings(
    title = "Apoya el proyecto",
    body = "La aplicación es gratuita y sin anuncios. Si te ayuda, puedes apoyar su desarrollo a través de Google Play. El pago va al desarrollador de la aplicación: no es zakat ni una donación a una organización benéfica.",
    thanks = "¡Gracias! Que Allah te recompense con el bien.",
    pending = "El pago está pendiente de confirmación y se completará solo.",
    failed = "El pago no se ha realizado. Inténtalo más tarde.",
    unavailable = "El apoyo está disponible en la aplicación instalada desde Google Play.",
)

private val Az = SupportStrings(
    title = "Layihəni dəstəklə",
    body = "Tətbiq pulsuzdur və reklamsızdır. Sizə faydalıdırsa, onun inkişafını Google Play vasitəsilə dəstəkləyə bilərsiniz. Ödəniş tətbiqin tərtibatçısına gedir: bu, zəkat və ya fonda ianə deyil.",
    thanks = "Təşəkkürlər! Allah sizi xeyirlə mükafatlandırsın.",
    pending = "Ödəniş təsdiq gözləyir və özü tamamlanacaq.",
    failed = "Ödəniş keçmədi. Bir az sonra yenidən cəhd edin.",
    unavailable = "Dəstək Google Play-dən quraşdırılmış tətbiqdə mövcuddur.",
)

private val Tg = SupportStrings(
    title = "Дастгирии лоиҳа",
    body = "Барнома ройгон ва бе реклама аст. Агар он ба шумо фоида расонад, метавонед рушди онро тавассути Google Play дастгирӣ кунед. Пардохт ба таҳиягари барнома меравад: ин на закот ва на хайрия ба бунёд аст.",
    thanks = "Ташаккур! Худованд ба шумо подоши нек диҳад.",
    pending = "Пардохт мунтазири тасдиқ аст ва худ анҷом меёбад.",
    failed = "Пардохт анҷом наёфт. Баъдтар боз кӯшиш кунед.",
    unavailable = "Дастгирӣ дар барномае, ки аз Google Play насб шудааст, дастрас аст.",
)

private val Tt = SupportStrings(
    title = "Проектка ярдәм итү",
    body = "Кушымта бушлай һәм реклама юк. Әгәр ул сезгә файдалы булса, аның үсешенә Google Play аша ярдәм итә аласыз. Түләү кушымта эшләүчесенә керә: бу зәкят та, фондка сәдака да түгел.",
    thanks = "Рәхмәт! Аллаһ сезне игелек белән бүләкләсен.",
    pending = "Түләү раслауны көтә һәм үзе үтәчәк.",
    failed = "Түләү үтмәде. Соңрак кабатлап карагыз.",
    unavailable = "Ярдәм Google Play'дан куелган кушымтада бар.",
)

private val Fr = SupportStrings(
    title = "Soutenir le projet",
    body = "L'application est gratuite et sans publicité. Si elle vous est utile, vous pouvez soutenir son développement via Google Play. Le paiement va au développeur de l'application : ce n'est ni la zakat ni un don à une association.",
    thanks = "Merci ! Qu'Allah vous récompense par le bien.",
    pending = "Le paiement attend sa confirmation et aboutira de lui-même.",
    failed = "Le paiement n'a pas abouti. Réessayez plus tard.",
    unavailable = "Le soutien est disponible dans l'application installée depuis Google Play.",
)

private val De = SupportStrings(
    title = "Das Projekt unterstützen",
    body = "Die App ist kostenlos und werbefrei. Wenn sie Ihnen hilft, können Sie ihre Entwicklung über Google Play unterstützen. Die Zahlung geht an den Entwickler der App: Sie ist weder Zakat noch eine Spende an eine Hilfsorganisation.",
    thanks = "Danke! Möge Allah Sie mit Gutem belohnen.",
    pending = "Die Zahlung wartet auf Bestätigung und wird von selbst abgeschlossen.",
    failed = "Die Zahlung ist fehlgeschlagen. Bitte später erneut versuchen.",
    unavailable = "Die Unterstützung ist in der über Google Play installierten App verfügbar.",
)

fun supportStringsFor(language: String): SupportStrings = when (language) {
    "ru" -> Ru
    "kk" -> Kk
    "ky" -> Ky
    "uz" -> Uz
    "ar" -> Ar
    "tr" -> Tr
    "id" -> Id
    "ur" -> Ur
    "es" -> Es
    "az" -> Az
    "tg" -> Tg
    "tt" -> Tt
    "fr" -> Fr
    "de" -> De
    else -> En
}
