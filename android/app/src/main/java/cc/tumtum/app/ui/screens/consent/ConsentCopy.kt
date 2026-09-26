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
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** The sentences of one purpose: what it is, what it does, and what stops when it is turned off. */
data class PurposeCopy(
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val stops: Int,
)

/**
 * The words for each consent key (26/09). The keys are [ConsentText]'s; the
 * words are strings.xml's, and any change to them is a new
 * [ConsentText.VERSION].
 */
object ConsentCopy {
    fun of(purpose: String): PurposeCopy? = when (purpose) {
        ConsentText.TERMS -> PurposeCopy(R.string.consent_terms_title, R.string.consent_terms_body, R.string.consent_terms_stops)
        ConsentText.READ_HEART_RATE -> PurposeCopy(R.string.consent_read_title, R.string.consent_read_body, R.string.consent_read_stops)
        ConsentText.KEEP_NIGHT -> PurposeCopy(R.string.consent_keep_title, R.string.consent_keep_body, R.string.consent_keep_stops)
        ConsentText.CROWD_STATS -> PurposeCopy(R.string.consent_crowd_title, R.string.consent_crowd_body, R.string.consent_crowd_stops)
        ConsentText.ARTIST_COMPARE -> PurposeCopy(R.string.consent_artist_title, R.string.consent_artist_body, R.string.consent_artist_stops)
        ConsentText.IMPROVE_DETECTION -> PurposeCopy(R.string.consent_improve_title, R.string.consent_improve_body, R.string.consent_improve_stops)
        ConsentText.MARKETING -> PurposeCopy(R.string.consent_marketing_title, R.string.consent_marketing_body, R.string.consent_marketing_stops)
        else -> null
    }
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
                onCheckedChange = { if (!busy) onToggle(it) },
                enabled = !busy,
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
        note?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = TTType.BodySmall, color = if (onDark) TT.Acid else TT.Ink)
        }
    }
}
