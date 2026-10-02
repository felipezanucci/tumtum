package cc.tumtum.app.ui.screens.login

import androidx.navigation.NavHostController
import cc.tumtum.app.AppContainer
import cc.tumtum.app.data.prefs.Account
import cc.tumtum.app.data.repo.afterSignIn
import cc.tumtum.app.data.repo.consentGateNeeded
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.screens.account.AuthErrors
import kotlinx.coroutines.flow.first

/**
 * Everything after the server said yes to a sign-in: the profile filled from
 * the server, the phone's own pieces kept only for the same account, the
 * consent gate, and the screen to land on. One function since 02/10, when
 * "esqueci a senha" moved into the app and became the second way in — two
 * copies of this would drift, and the drift would be a person landing
 * somewhere different depending on how they got their password back.
 *
 * [onboarded]: this phone already went through the first-run screens.
 * [typedEmail]: the address as typed, for when /me cannot be reached.
 */
internal suspend fun completeSignIn(
    container: AppContainer,
    nav: NavHostController,
    onboarded: Boolean,
    typedEmail: String,
) {
    // The name and — since 28/09 — the @ are the server's;
    // the tribes and the photo are this phone's, kept only
    // when this phone's profile is the account signing in.
    // A different account starts from its own (24/09: test 7
    // showed Felipe's photo and @ over another account).
    val me = runCatching { container.api.me() }.getOrNull()
    val signedInEmail = me?.email ?: typedEmail
    // Read after /me: it may just have claimed this phone's
    // pending @, or heard it refused.
    val existing = container.prefs.state.first().account?.takeIf { it.belongsTo(signedInEmail) }
    val account = Account(
        name = me?.name?.ifBlank { null } ?: existing?.name ?: AuthErrors.handleFrom(typedEmail)
            .replaceFirstChar { it.uppercase() },
        // The server's @, or none: an @ made up from the
        // address was never reserved for anybody (28/09).
        // Without an answer, whatever the phone knew stands.
        username = if (me != null) me.username else existing?.username,
        email = signedInEmail,
        tribes = existing?.tribes ?: emptySet(),
        pendingUsername = existing?.pendingUsername?.takeIf { me?.username == null },
        pendingRefused = existing?.pendingRefused == true,
    )
    if (existing != null) {
        container.prefs.createAccount(account)
    } else {
        container.prefs.replaceAccount(account)
    }
    container.afterSignIn()
    // The consent gate (26/09): an account without a birth date, without the
    // Terms, or whose yes was given under words that have since changed
    // passes it first (02/10). Otherwise straight to the feed (#57, 23/09),
    // with the back stack cleared so "back" does not return to the form —
    // **whether or not this phone saw the account before**. Until 02/10 a
    // first sign-in on a phone opened the whole consent screen again, which
    // read as the app forgetting what the account had already said.
    val gate = container.consentGateNeeded() == true
    if (gate) {
        nav.navigate(Routes.consent()) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
        return
    }
    if (!onboarded) container.prefs.setOnboarded()
    // A phone that has never read this person's watch still needs the Health
    // Connect dialog, when reading is on: said now, not skipped silently.
    val needsPermission = !onboarded &&
        container.prefs.state.first().granted(cc.tumtum.app.domain.ConsentText.READ_HEART_RATE) == true &&
        container.health.isAvailable && !container.health.hasPermission()
    nav.navigate(if (needsPermission) Routes.Permission else Routes.Feed) {
        popUpTo(nav.graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
