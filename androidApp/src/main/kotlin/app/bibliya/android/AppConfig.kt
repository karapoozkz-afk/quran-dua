package app.bibliya.android

/**
 * Everything the owner fills in before publishing. These are the only blanks in the app;
 * RELEASE.md lists them with the Play Console fields they must match.
 */
object AppConfig {
    /** Shown in "Contact the developer" and must equal the email in the Play Console listing. */
    const val SUPPORT_EMAIL = "ВСТАВЬТЕ_EMAIL@example.com"

    /** Public privacy policy page; the same address goes into Play Console → App content → Privacy policy. */
    const val PRIVACY_POLICY_URL = "https://github.com/karapoozkz-afk/quran-dua/blob/bible/docs/privacy-policy.md"

    /** True once SUPPORT_EMAIL is filled in; until then the feedback row stays hidden. */
    val hasSupportEmail: Boolean get() = !SUPPORT_EMAIL.startsWith("ВСТАВЬТЕ")
}
