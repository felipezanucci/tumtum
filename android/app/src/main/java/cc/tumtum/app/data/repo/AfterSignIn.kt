package cc.tumtum.app.data.repo

import cc.tumtum.app.AppContainer
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.service.Reminders

/**
 * What follows every sign-in and every new account (25/09), in one place so
 * the two doors cannot drift apart.
 *
 * - **The reminders follow the account.** The phone now shows the signed-in
 *   account's nights; a sealed night of the previous one is not this
 *   person's to be told about, and its alarm goes.
 * - **A night its owner asked to keep goes up now** — only those (26/09).
 *   A night nobody asked to send stays on the phone, signed in or not.
 */
suspend fun AppContainer.afterSignIn() {
    runCatching { Reminders.rescheduleAll(appContext, this) }
    sync.retryPendingLater()
}

/**
 * Whether this account must pass the consent gate before anything else
 * (26/09, the shared contract): no birth date on the server, the Terms not
 * agreed — or agreed under words that have since changed (02/10): then the
 * person is asked once more, and the new yes is recorded under the new
 * version. Nothing else opens it: signing in again on a phone that was
 * wiped is not a reason (b232, Felipe: "quando eu logo de novo na conta,
 * ele deveria já considerar o que foi salvo"). Null when the server could
 * not be asked — the caller then lets the person in, and the server still
 * refuses whatever needs consent.
 */
suspend fun AppContainer.consentGateNeeded(): Boolean? = runCatching {
    val me = api.me()
    val consents = api.getConsents()
    val terms = consents.consents.firstOrNull { it.purpose == ConsentText.TERMS }
    me.birthDate == null || terms?.granted != true || terms.textVersion != ConsentText.VERSION
}.getOrNull()
