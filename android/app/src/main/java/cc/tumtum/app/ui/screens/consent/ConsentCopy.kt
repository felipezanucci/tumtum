package cc.tumtum.app.ui.screens.consent

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ConsentSnapshot
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.ui.components.serverDeadline
import java.time.Instant
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * The sentences of one purpose: what it is, what it does, what stops when it
 * is turned off — and, for a key that starts off and is worth turning on,
 * why ([why], 28/09).
 */
data class PurposeCopy(
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val stops: Int,
    @StringRes val why: Int? = null,
    /** Under a key that cannot be turned off here (the Terms, 02/10): how it is withdrawn. */
    @StringRes val locked: Int? = null,
)

/**
 * The words for each consent key (26/09). The keys are [ConsentText]'s; the
 * words are `strings_consent.xml`'s — generated from
 * `shared/consent/consent-text.json`, the one text every client shows
 * (02/10) — and any change to them is a new [ConsentText.VERSION].
 */
object ConsentCopy {
    fun of(purpose: String): PurposeCopy? = when (purpose) {
        ConsentText.TERMS -> PurposeCopy(
            R.string.consent_terms_title, R.string.consent_terms_body, R.string.consent_terms_stops,
            locked = R.string.consent_terms_locked,
        )
        ConsentText.READ_HEART_RATE -> PurposeCopy(R.string.consent_read_title, R.string.consent_read_body, R.string.consent_read_stops)
        ConsentText.KEEP_NIGHT -> PurposeCopy(
            R.string.consent_keep_title, R.string.consent_keep_body, R.string.consent_keep_stops,
            why = R.string.consent_keep_why,
        )
        ConsentText.CROWD_STATS -> PurposeCopy(R.string.consent_crowd_title, R.string.consent_crowd_body, R.string.consent_crowd_stops)
        ConsentText.ARTIST_COMPARE -> PurposeCopy(R.string.consent_artist_title, R.string.consent_artist_body, R.string.consent_artist_stops)
        ConsentText.IMPROVE_DETECTION -> PurposeCopy(R.string.consent_improve_title, R.string.consent_improve_body, R.string.consent_improve_stops)
        ConsentText.MARKETING -> PurposeCopy(R.string.consent_marketing_title, R.string.consent_marketing_body, R.string.consent_marketing_stops)
        else -> null
    }
}

/**
 * What a switch that is off says under it (28/09, items 4 and 23): what
 * stopped, in words — the same on the consent screen and in Configurações,
 * and it stays while the switch is off, not only right after the tap.
 *
 * "Guardar a noite" says when the nights leave the server: 24 hours after the
 * revocation ([entry]'s `revoked_at`), as a date and an hour when the server
 * gave one; once that has passed, or when nothing was ever kept, that they
 * are only on this phone. [wasGranted] is for a switch turned off on this
 * screen and not yet sent, which has no `revoked_at` of its own yet.
 */
@Composable
fun consentOffNote(
    purpose: String,
    entry: ConsentSnapshot.Entry?,
    wasGranted: Boolean = entry?.grantedAt != null,
    now: Instant = Instant.now(),
): String? {
    if (purpose == ConsentText.KEEP_NIGHT) {
        val deadline = entry?.revokedAt?.plus(ConsentText.SERVER_DELETION_DELAY)
        return when {
            deadline != null && deadline.isAfter(now) -> stringResource(R.string.settings_keep_off_until, serverDeadline(deadline, now))
            deadline != null -> stringResource(R.string.settings_keep_off_local)
            wasGranted -> stringResource(R.string.settings_keep_off)
            else -> stringResource(R.string.settings_keep_off_local)
        }
    }
    return ConsentCopy.of(purpose)?.let { stringResource(it.stops) }
}

/** Opens a page of tumtum.cc in the browser. Nothing happens when no browser can take it. */
fun openLink(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/** A mail to the person in charge of data, subject "Privacidade" already filled in. */
fun mailDpo(context: Context) {
    runCatching {
        val uri = Uri.parse("mailto:${ConsentText.DPO_EMAIL}?subject=${Uri.encode(ConsentText.DPO_SUBJECT)}")
        context.startActivity(Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * What every consent screen says before any switch (26/09, the shared
 * contract): health data; the window; how long things are kept; not a
 * medical device; nobody else gets it; where to take it back. Quiet: body
 * type, no colour, no joke.
 */
@Composable
fun ConsentIntro(onDark: Boolean) {
    val color = if (onDark) TT.Gray45 else TT.Gray70
    listOf(
        R.string.consent_intro_health,
        R.string.consent_intro_window,
        R.string.consent_intro_retention,
        R.string.consent_intro_not_medical,
        R.string.consent_intro_nobody,
        R.string.consent_intro_revoke,
    ).forEachIndexed { i, res ->
        if (i > 0) Spacer(Modifier.height(10.dp))
        Text(stringResource(res), style = TTType.Body, color = color)
    }
}

/** Termos · Política de privacidade, each opening tumtum.cc. */
@Composable
fun PolicyLinks(onDark: Boolean) {
    val context = LocalContext.current
    val color = if (onDark) TT.Paper else TT.Ink
    Row {
        LinkText(stringResource(R.string.consent_link_terms), color) { openLink(context, ConsentText.TERMS_URL) }
        Spacer(Modifier.width(18.dp))
        LinkText(stringResource(R.string.consent_link_privacy), color) { openLink(context, ConsentText.PRIVACY_URL) }
    }
}

@Composable
fun LinkText(text: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(
        text,
        style = TTType.BodySmall.copy(textDecoration = TextDecoration.Underline),
        color = color,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
    )
}

/**
 * One purpose: its name and a switch on one line, what it means under it,
 * and — when there is one — a [note] under that (what just stopped, or why
 * the change did not go through). [focused] frames the row the screen was
 * opened for. The switch shows only what the server recorded; while a
 * change is in flight [busy] holds it.
 */
@Composable
fun ConsentRow(
    purpose: String,
    on: Boolean,
    onToggle: (Boolean) -> Unit,
    onDark: Boolean,
    focused: Boolean = false,
    busy: Boolean = false,
    note: String? = null,
    /**
     * A key that stays as it is (02/10): the Terms once accepted — a contract,
     * withdrawn by deleting the account, never by a switch. The site locked
     * it from the first day; the app let Felipe turn it off on b232.
     */
    locked: Boolean = false,
) {
    val copy = ConsentCopy.of(purpose) ?: return
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .let { if (focused) it.border(1.dp, if (onDark) TT.Acid else TT.Ink, shape) else it }
            .padding(horizontal = if (focused) 12.dp else 0.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(copy.title),
                style = TTType.ItemTitle,
                color = if (onDark) TT.Paper else TT.Ink,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = on,
                onCheckedChange = { if (!busy && !locked) onToggle(it) },
                enabled = !busy && !locked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TT.Ink,
                    checkedTrackColor = TT.Rose,
                    checkedBorderColor = TT.Rose,
                    uncheckedThumbColor = if (onDark) TT.Gray45 else TT.Gray55,
                    uncheckedTrackColor = if (onDark) TT.Ink700 else TT.Gray10,
                    uncheckedBorderColor = if (onDark) TT.Ink600 else TT.Gray25,
                    disabledCheckedThumbColor = TT.Ink,
                    disabledCheckedTrackColor = TT.Rose,
                    disabledUncheckedThumbColor = if (onDark) TT.Gray45 else TT.Gray55,
                    disabledUncheckedTrackColor = if (onDark) TT.Ink700 else TT.Gray10,
                ),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(stringResource(copy.body), style = TTType.BodySmall, color = if (onDark) TT.Gray45 else TT.Gray70)
        copy.why?.let {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(it), style = TTType.BodySmall, color = if (onDark) TT.Paper else TT.Ink)
        }
        (note ?: copy.locked?.takeIf { locked }?.let { stringResource(it) })?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = TTType.BodySmall, color = if (onDark) TT.Acid else TT.Ink)
        }
    }
}
