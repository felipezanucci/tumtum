package cc.tumtum.app.ui.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cc.tumtum.app.R
import cc.tumtum.app.domain.Username
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * The server's last answer about the @ being typed (28/09), shared by the
 * sign-up and Configurações.
 *
 * Until 28/09 the sign-up said "disponível" to every name that was not one
 * of six written into the app, and two accounts held @fezanu. This holds
 * only what the server said, and for which name: [Username.status] reads an
 * answer about another name as no answer.
 */
class UsernameChecker {
    var check by mutableStateOf<Username.Check?>(null)
        private set
    var attempt by mutableIntStateOf(0)
        private set

    /** Ask again (after a failed check): the line reads "Conferindo…" until the new answer, never the old failure. */
    fun retry() {
        check = null
        attempt++
    }

    /**
     * The server refused [name] where it counted — the sign-up's code, the
     * account's creation, the PATCH that saves it. Said under the field, in
     * the server's words, until the name changes.
     */
    fun refused(name: String, reason: String) {
        check = Username.Check.Unavailable(name, reason)
    }

    internal fun answer(value: Username.Check) {
        check = value
    }
}

/**
 * Asks the server about [name] once the typing pauses ([Username.DEBOUNCE_MS]).
 * A name the phone already knows cannot be one is not sent. No answer is a
 * [Username.Check.Failed], never an assumed yes.
 */
@Composable
fun rememberUsernameChecker(name: String): UsernameChecker {
    val api = appContainer().api
    val checker = remember { UsernameChecker() }
    LaunchedEffect(name, checker.attempt) {
        if (Username.problem(name) != null) return@LaunchedEffect
        delay(Username.DEBOUNCE_MS)
        val answer = try {
            val a = api.checkUsername(name)
            if (a.available) Username.Check.Available(name) else Username.Check.Unavailable(name, a.reason)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Username.Check.Failed(name)
        }
        checker.answer(answer)
    }
    return checker
}

/**
 * The @ field and the line under it. The field keeps only what an @ can be
 * ([Username.edit]); a character it had to drop is said, because a keystroke
 * that vanishes reads as a broken keyboard. The line is recomputed from the
 * state every time it is drawn — the name on screen and the answer for it.
 */
@Composable
fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    checker: UsernameChecker,
    modifier: Modifier = Modifier,
) {
    var dropped by remember { mutableStateOf(false) }
    Column(modifier) {
        TTField(
            stringResource(R.string.account_username_label),
            value,
            { raw ->
                val edit = Username.edit(raw)
                dropped = edit.dropped
                onValueChange(edit.value)
            },
            keyboardType = KeyboardType.Ascii,
        )
        if (dropped) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.username_bad_chars), style = TTType.Footnote, color = TT.Gray70)
        }
        val status = Username.status(value, checker.check)
        usernameStatusText(status)?.let { line ->
            Spacer(Modifier.height(6.dp))
            Text(
                line,
                style = TTType.Footnote,
                color = when (status) {
                    Username.Status.Available -> TT.Ink
                    is Username.Status.Unavailable -> TT.Rose
                    else -> TT.Gray45
                },
            )
        }
    }
}

/** The sentence for [status]; null when there is nothing to claim (an empty field). */
@Composable
fun usernameStatusText(status: Username.Status): String? = when (status) {
    Username.Status.Empty -> null
    is Username.Status.Local -> stringResource(problemText(status.problem))
    Username.Status.Checking -> stringResource(R.string.username_checking)
    Username.Status.Available -> stringResource(R.string.account_username_free)
    is Username.Status.Unavailable -> status.reason ?: stringResource(R.string.username_unavailable)
    Username.Status.Failed -> stringResource(R.string.username_check_failed)
}

fun problemText(problem: Username.Problem): Int = when (problem) {
    Username.Problem.TOO_SHORT -> R.string.username_too_short
    Username.Problem.TOO_LONG -> R.string.username_too_long
    Username.Problem.BAD_CHARS -> R.string.username_bad_chars
}

/**
 * What a declined button says when the @ is what is missing — the sign-up's
 * and Configurações' alike. A failed check is asked again on the same tap, and
 * the sentence says so.
 */
fun usernameDeclined(status: Username.Status, checker: UsernameChecker, context: android.content.Context): String =
    when (status) {
        Username.Status.Empty -> context.getString(R.string.username_too_short)
        is Username.Status.Local -> context.getString(problemText(status.problem))
        Username.Status.Checking -> context.getString(R.string.form_username_checking)
        is Username.Status.Unavailable -> status.reason ?: context.getString(R.string.username_unavailable)
        Username.Status.Failed -> {
            checker.retry()
            context.getString(R.string.form_username_unchecked)
        }
        // Not reached: an available @ does not decline the button.
        Username.Status.Available -> context.getString(R.string.account_username_free)
    }
