package cc.tumtum.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.components.PlateText
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

private data class ObPage(val headline: Int, val plate: Int, val body: Int)

/** b1 — Onboarding. Rosa de tela cheia, o único lugar onde a marca grita. */
@Composable
fun OnboardingScreen(nav: NavHostController) {
    val pages = listOf(
        ObPage(R.string.ob1_headline, R.string.ob1_plate, R.string.ob1_body),
        ObPage(R.string.ob2_headline, R.string.ob2_plate, R.string.ob2_body),
        ObPage(R.string.ob3_headline, R.string.ob3_plate, R.string.ob3_body),
    )
    val pagerState = rememberPagerState { pages.size }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Rose)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 32.dp, end = 32.dp, top = 26.dp, bottom = 30.dp),
    ) {
        Wordmark(width = 110.dp)
        HorizontalPager(pagerState, Modifier.weight(1f).fillMaxWidth()) { i ->
            val page = pages[i]
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(stringResource(page.headline), style = TTType.Shout, color = TT.Ink)
                Spacer(Modifier.height(14.dp))
                PlateText(
                    stringResource(page.plate),
                    background = TT.Ink,
                    contentColor = TT.Acid,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    stringResource(page.body),
                    style = TTType.Body,
                    color = TT.Ink.copy(alpha = 0.75f),
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }
        }
        Row(Modifier.padding(bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pages.indices.forEach { i ->
                Box(
                    Modifier
                        .size(
                            width = if (i == pagerState.currentPage) 22.dp else 10.dp,
                            height = 4.dp,
                        )
                        .background(if (i == pagerState.currentPage) TT.Ink else TT.Ink.copy(alpha = 0.3f)),
                )
            }
        }
        TTButton(stringResource(R.string.ob_start), TTButtonStyle.Ink, onClick = { nav.navigate(Routes.Account) })
        Spacer(Modifier.height(10.dp))
        val interaction = remember { MutableInteractionSource() }
        Text(
            stringResource(R.string.ob_have_account),
            style = TTType.Button.copy(fontSize = 14.sp),
            color = TT.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interaction, indication = null) { nav.navigate(Routes.Account) }
                .padding(10.dp),
        )
    }
}
