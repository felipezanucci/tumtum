package cc.tumtum.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cc.tumtum.app.R
import cc.tumtum.app.domain.FeedGate

/** Why the feed's door is shut, in the words every door uses (28/09, item 31). */
@Composable
fun feedClosedText(closed: FeedGate.Closed): String = when (closed) {
    FeedGate.Closed.OtherAccount -> stringResource(R.string.feed_post_other_account)
    FeedGate.Closed.NotKept -> stringResource(R.string.feed_post_not_kept)
    FeedGate.Closed.NoEvent -> stringResource(R.string.feed_post_no_event)
    is FeedGate.Closed.FewReadings -> stringResource(R.string.feed_post_few_readings)
}
