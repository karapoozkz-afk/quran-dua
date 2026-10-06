package app.qurandua.shared.model

import kotlinx.serialization.Serializable

/**
 * A verified charity on the donation screen. tools/build_content.py drops any
 * organisation that is missing a field, so the app never shows an unverified one.
 */
@Serializable
data class Charity(
    val id: String,
    val name: Localized,
    /** ISO 3166-1 alpha-2. */
    val country: String,
    val registrationNumber: String,
    val website: String,
    val donateUrl: String,
    /** zakat, sadaqah, mosque, orphans, water, food, education, general */
    val purposes: List<String>,
    /** ISO date when the registration was checked against the official register. */
    val verifiedOn: String,
    val verifiedBy: String,
) {
    val acceptsZakat: Boolean get() = "zakat" in purposes
}
