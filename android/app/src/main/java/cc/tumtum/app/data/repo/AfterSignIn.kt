package cc.tumtum.app.data.repo

import cc.tumtum.app.AppContainer
import cc.tumtum.app.service.Reminders

/**
 * What follows every sign-in and every new account (25/09), in one place so
 * the two doors cannot drift apart.
 *
 * - **The reminders follow the account.** The phone now shows the signed-in
 *   account's nights; a sealed night of the previous one is not this
 *   person's to be told about, and its alarm goes.
 * - **A night waiting for an account goes up now.** On 25/09 a night
 *   recorded signed out sat PENDING after sign-in until the next app start,
 *   while the event's feed said "Sua noite não chegou aqui" about it.
 */
suspend fun AppContainer.afterSignIn() {
    runCatching { Reminders.rescheduleAll(appContext, this) }
    sync.retryPendingLater()
}
