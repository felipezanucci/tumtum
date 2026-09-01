package cc.tumtum.app.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.GalleryNight
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.components.skinColor
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * b7 — Sua galeria: cada noite guarda a pele escolhida no card.
 * A forma fica, a pele muda — o Mutante Pop funcionando.
 */
@Composable
fun GalleryScreen(nav: NavHostController) {
    val container = appContainer()
    val gallery by container.nights.galleryNights().collectAsStateWithLifecycle(initialValue = emptyList())
    val nights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    val momentTotal = nights.sumOf { it.momentCount }
    val record = nights.maxOfOrNull { it.peakBpm }
    val since = nights.minByOrNull { it.date }?.date

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(TT.Paper).statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Wordmark(width = 92.dp)
                    Avatar(user?.account?.initials ?: "TT", Skin.BLACK)
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.gallery_title), style = TTType.Title, color = TT.Ink)
                Spacer(Modifier.height(4.dp))
                if (since != null) {
                    Text(
                        stringResource(R.string.gallery_subtitle, Fmt.monthName(since)),
                        style = TTType.ItemSub.copy(fontSize = 14.sp),
                        color = TT.Gray45,
                    )
                }
                Row(Modifier.padding(top = 18.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Stat(gallery.size, stringResource(R.string.gallery_nights))
                    Stat(momentTotal, stringResource(R.string.gallery_moments))
                    record?.let { Stat(it, stringResource(R.string.gallery_record)) }
                }
                if (gallery.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.gallery_empty), style = TTType.Body, color = TT.Gray45)
                }
            }
        }
        items(gallery, key = { it.nightId }) { night ->
            GalleryCover(night) { nav.navigate(Routes.reveal(night.nightId)) }
        }
        item(span = { GridItemSpan(2) }) {
            Text(
                stringResource(R.string.gallery_footnote),
                style = TTType.MetaSmall.copy(fontSize = 11.5.sp, letterSpacing = 0.em, fontWeight = FontWeight.Normal, lineHeight = 17.sp),
                color = TT.Gray45,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun Stat(value: Int, label: String) {
    Column {
        Text("$value", style = TTType.NumberMedium, color = TT.Ink)
        Text(label, style = TTType.MetaSmall, color = TT.Gray45)
    }
}

/**
 * Capa 9:14 — número grande + pele da noite. Número preto; no preto, rosa.
 * `compact` é a variação de 3 colunas do perfil (b8).
 */
@Composable
fun GalleryCover(night: GalleryNight, compact: Boolean = false, onClick: () -> Unit) {
    val bg = skinColor(night.skin)
    val num = if (night.skin == Skin.BLACK) TT.Rose else TT.Ink
    val fg = if (night.skin == Skin.BLACK) TT.Paper else TT.Ink
    Column(
        Modifier
            .aspectRatio(9f / 14f)
            .background(bg)
            .let { if (night.skin == Skin.WHITE) it.border(1.dp, TT.Gray10) else it }
            .clickable(onClick = onClick)
            .padding(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            "${night.peakBpm}",
            style = if (compact) {
                TTType.NumberMedium.copy(fontSize = 26.sp, letterSpacing = (-0.04).em, lineHeight = 22.sp)
            } else {
                TTType.NumberLarge.copy(fontSize = 44.sp, letterSpacing = (-0.05).em, lineHeight = 36.sp)
            },
            color = num,
        )
        Spacer(Modifier.height(if (compact) 5.dp else 8.dp))
        Text(
            night.label,
            style = TTType.MetaSmall.copy(fontSize = if (compact) 8.5.sp else 10.5.sp, letterSpacing = 0.em, lineHeight = 13.5.sp),
            color = fg,
        )
        if (!compact) {
            Text(
                night.dateLabel,
                style = TTType.MetaSmall.copy(fontSize = 10.5.sp, letterSpacing = 0.em, fontWeight = FontWeight.Medium),
                color = fg.copy(alpha = 0.6f),
            )
        }
    }
}
