package cc.tumtum.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cc.tumtum.app.R
import cc.tumtum.app.domain.CardCopy

/**
 * The card's two lines, in words (26/09): [CardCopy] decides which sentence
 * the night's numbers can prove, strings.xml holds the sentence.
 */
@Composable
fun cardTitleText(title: CardCopy.Title): String = when (title) {
    is CardCopy.Title.AboveAverage -> {
        val first = stringResource(R.string.card_title_average, title.averageBpm)
        val second = if (title.at != null) {
            stringResource(R.string.card_title_until_at, title.at)
        } else {
            stringResource(R.string.card_title_until_then)
        }
        "$first\n$second"
    }
    is CardCopy.Title.HeartAt -> {
        val first = stringResource(R.string.card_title_heart)
        val second = if (title.at != null) {
            stringResource(R.string.card_title_heart_at, title.at)
        } else {
            stringResource(R.string.card_title_heart_then)
        }
        "$first\n$second"
    }
    CardCopy.Title.NoMoments -> stringResource(R.string.card_title_no_moments)
}
