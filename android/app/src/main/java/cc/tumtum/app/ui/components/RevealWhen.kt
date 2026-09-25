package cc.tumtum.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cc.tumtum.app.R
import cc.tumtum.app.ui.Fmt
import java.time.Instant

/**
 * When a sealed night opens, with its day (25/09). At 14h20 the screen said
 * "abre às 10h00" — true of tomorrow, read as a morning already gone.
 */
@Composable
fun revealWhen(at: Instant, now: Instant = Instant.now()): String {
    val hour = Fmt.hour(at)
    return when (Fmt.daysFrom(now, at)) {
        0L -> stringResource(R.string.reveal_when_today, hour)
        1L -> stringResource(R.string.reveal_when_tomorrow, hour)
        else -> stringResource(R.string.reveal_when_day, Fmt.dayMonth(at), hour)
    }
}
