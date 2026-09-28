package cc.tumtum.app.domain

import java.text.Normalizer
import java.util.Locale

/**
 * The @ of an account, as the phone handles it (28/09).
 *
 * Until 28/09 the @ lived only on the phone: the sign-up checked it against
 * a list of six names written into the app and said "disponível" to every
 * other one, and Configurações promised "o @ é fixo" over a name nothing
 * reserved. Two accounts held @fezanu. The server now owns the @ — unique,
 * case-insensitive, chosen once (`backend/app/services/usernames.py`) — and
 * this object is only what the phone can know on its own: which characters
 * a field keeps, which lengths cannot be one, and what the line under the
 * field may say about the state it is in.
 *
 * The rule that shaped [status]: **the line never says "disponível" without
 * the server's answer for the name on screen.** An answer for a name the
 * person has since changed, a check still waiting, a request that failed —
 * each has its own sentence, never the one that means "it is yours".
 *
 * Pure, so it is tested without a device.
 */
object Username {
    const val MIN_LENGTH = 3
    const val MAX_LENGTH = 20

    /** How long the typing must pause before the server is asked. */
    const val DEBOUNCE_MS = 400L

    /** What a field keeps of a keystroke, and whether it had to drop something to keep it. */
    data class Edit(val value: String, val dropped: Boolean)

    /**
     * The field filters as typed: lower case, accents taken off their letters
     * ("João" keeps "joao"), and only a–z, 0–9 and _. A leading @ is how the
     * name reads, not a character of it, and goes without a word. Anything
     * else dropped is reported in [Edit.dropped], so the screen can say which
     * characters the @ does not take — a keystroke that vanishes silently
     * reads as a broken keyboard.
     */
    fun edit(raw: String): Edit {
        val plain = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase(Locale.ROOT)
        val kept = plain.filter(::allowed)
        val dropped = plain.any { !allowed(it) && it != '@' }
        return Edit(kept, dropped)
    }

    /** What the phone can tell without asking: too short or too long. Reserved and taken are the server's. */
    enum class Problem { TOO_SHORT, TOO_LONG, BAD_CHARS }

    fun problem(name: String): Problem? = when {
        name.length < MIN_LENGTH -> Problem.TOO_SHORT
        name.length > MAX_LENGTH -> Problem.TOO_LONG
        !name.all(::allowed) -> Problem.BAD_CHARS
        else -> null
    }

    /** The server's answer about one name — the name it was asked for, so a stale answer is never read as current. */
    sealed interface Check {
        val name: String

        data class Available(override val name: String) : Check

        /** [reason] is the server's own sentence ("Esse @ já tem dono. Tenta outro."), or null if it gave none. */
        data class Unavailable(override val name: String, val reason: String?) : Check

        /** No answer: offline, a timeout, a server error. Never read as either of the above. */
        data class Failed(override val name: String) : Check
    }

    /** What the line under the field says. */
    sealed interface Status {
        /** Nothing typed: nothing claimed. */
        data object Empty : Status
        data class Local(val problem: Problem) : Status
        /** The debounce is running or the request is out. */
        data object Checking : Status
        data object Available : Status
        data class Unavailable(val reason: String?) : Status
        data object Failed : Status
    }

    /**
     * The line for [name] given the last answer [check]. An answer about any
     * other name is no answer at all — the person typed since — so it reads
     * as [Status.Checking] until the answer for this name arrives.
     */
    fun status(name: String, check: Check?): Status {
        if (name.isEmpty()) return Status.Empty
        problem(name)?.let { return Status.Local(it) }
        if (check == null || check.name != name) return Status.Checking
        return when (check) {
            is Check.Available -> Status.Available
            is Check.Unavailable -> Status.Unavailable(check.reason)
            is Check.Failed -> Status.Failed
        }
    }

    /** Only a confirmed answer for this very name lets an account be made with it, or an @ be saved. */
    fun confirmed(name: String, check: Check?): Boolean = status(name, check) == Status.Available

    /**
     * Whether a refusal from the server is about the @, so the screen shows it
     * under the @ field and not as a generic error. The server's sentences
     * about the @ are the only ones that open with it ("O @ precisa…",
     * "Esse @ já tem dono…"); no other refusal of the account's does.
     */
    fun isAboutUsername(detail: String?): Boolean {
        val t = detail?.trimStart() ?: return false
        return t.startsWith("O @") || t.startsWith("Esse @")
    }

    private fun allowed(c: Char): Boolean = c in 'a'..'z' || c in '0'..'9' || c == '_'

    private val COMBINING_MARKS = Regex("\\p{M}+")
}
