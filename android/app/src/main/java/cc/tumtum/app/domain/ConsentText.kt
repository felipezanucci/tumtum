package cc.tumtum.app.domain

/**
 * What the person agrees to, one purpose at a time (LGPD remediation, 26/09).
 *
 * The legal opinion leaves consent as the only workable basis for heart-rate
 * data, and consent only counts when it is specific, highlighted, per
 * purpose, recorded and revocable. So there is no "aceito tudo": seven keys,
 * each with its own switch, and the five optional ones start **off** — the
 * app never turns one on for the person.
 *
 * [VERSION] names the text the person was shown. It is the same literal in
 * the backend, the web and here (the shared contract), and it travels with
 * every PUT so the server records which words were agreed to. Change the
 * words in strings.xml → change the version, in all three places.
 *
 * The keys are the server's; the sentences live in strings.xml and are
 * mapped in `ui/screens/consent/ConsentCopy.kt`, so this file stays plain
 * Kotlin that a JVM test can load.
 */
object ConsentText {
    const val VERSION = "2026-09-26"

    const val TERMS = "terms"
    const val READ_HEART_RATE = "read_heart_rate"
    const val KEEP_NIGHT = "keep_night"
    const val CROWD_STATS = "crowd_stats"
    const val ARTIST_COMPARE = "artist_compare"
    const val IMPROVE_DETECTION = "improve_detection"
    const val MARKETING = "marketing"

    /** All seven, in the order the screens show them. */
    val PURPOSES: List<String> = listOf(
        TERMS, READ_HEART_RATE, KEEP_NIGHT, CROWD_STATS, ARTIST_COMPARE, IMPROVE_DETECTION, MARKETING,
    )

    /** The two the core loop needs — each still its own explicit tap, never pre-ticked by us. */
    val CORE: Set<String> = setOf(TERMS, READ_HEART_RATE)

    fun isOptional(purpose: String): Boolean = purpose in PURPOSES && purpose !in CORE

    /** How the choice was made, as the contract spells it. */
    const val MEANS_TAP = "tap"
    const val MEANS_CHECKBOX = "checkbox"

    const val TERMS_URL = "https://tumtum.cc/termos"
    const val PRIVACY_URL = "https://tumtum.cc/privacidade"
    /** Where the person downloads everything the server holds about them. */
    const val DATA_URL = "https://tumtum.cc/perfil"
    const val DPO_EMAIL = "oi@tumtum.cc"
    const val DPO_SUBJECT = "Privacidade"

    /**
     * The switches a screen starts from: exactly what the server says is
     * granted, and **off for everything it does not know about**. A purpose
     * the server never answered is not consent.
     */
    fun startingSwitches(granted: Map<String, Boolean>): Map<String, Boolean> =
        PURPOSES.associateWith { granted[it] == true }
}
