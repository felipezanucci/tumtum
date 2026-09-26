package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.R
import cc.tumtum.app.domain.EventTimes
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToInt

/**
 * A time is never typed (Felipe, 21/09): every date or time field in the
 * product is a rolling wheel — "aquele campo onde a pessoa rola as horas".
 *
 * The wheels are ours, drawn in Compose. The first build (b142) inflated
 * Android's own `DatePicker`/`TimePicker` in spinner mode, and on One UI they
 * came up **blank** — the selection dividers drawn, every number invisible.
 * Samsung replaces those widgets and the plain platform theme does not carry
 * what their version reads. Rather than chase an OEM's theme attributes on a
 * device nobody here can compile against, the wheel is a `LazyColumn` that
 * snaps: no OEM in the path, no theme to lose, and it can look like TumTum.
 *
 * The field itself reads like a TTField and says MUDAR, so it is visibly a
 * thing to touch and never a thing to type into.
 */
private val ITEM_HEIGHT = 44.dp
private const val VISIBLE_ROWS = 5

@Composable
fun WheelDateField(label: String, value: LocalDate, onChange: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    PickerField(label, EventTimes.formatDate(value), modifier) { open = true }
    if (open) {
        // The wheels edit a draft; nothing changes until OK.
        var day by remember { mutableIntStateOf(value.dayOfMonth) }
        var month by remember { mutableIntStateOf(value.monthValue) }
        var year by remember { mutableIntStateOf(value.year) }
        val maxDay = remember(month, year) { YearMonth.of(year, month).lengthOfMonth() }
        // 31 → 30 when the month shrinks under a chosen day.
        LaunchedEffect(maxDay) { if (day > maxDay) day = maxDay }
        val thisYear = remember { LocalDate.now().year }
        val years = remember(thisYear) { (thisYear..thisYear + 2).toList() }

        WheelDialog(
            title = label,
            onDismiss = { open = false },
            onOk = {
                onChange(LocalDate.of(year, month, day))
                open = false
            },
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WheelColumn(
                    caption = stringResource(R.string.picker_day),
                    items = (1..maxDay).map { "$it" },
                    index = day - 1,
                    onIndex = { day = it + 1 },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    caption = stringResource(R.string.picker_month),
                    items = MONTHS.toList(),
                    index = month - 1,
                    onIndex = { month = it + 1 },
                    modifier = Modifier.weight(1.2f),
                )
                WheelColumn(
                    caption = stringResource(R.string.picker_year),
                    items = years.map { "$it" },
                    index = years.indexOf(year).coerceAtLeast(0),
                    onIndex = { year = years[it] },
                    modifier = Modifier.weight(1.3f),
                )
            }
        }
    }
}

/**
 * A birth date (26/09): the same wheels, over a century of years instead of
 * the next two, and a field that says "Escolher" until something is chosen —
 * never a date the person did not pick. Typing stays impossible.
 */
@Composable
fun WheelBirthDateField(
    label: String,
    value: LocalDate?,
    onChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }
    PickerField(
        label,
        value?.let { EventTimes.formatDate(it) } ?: stringResource(R.string.picker_choose),
        modifier,
        onDark = onDark,
    ) { open = true }
    if (open) {
        val today = remember { LocalDate.now() }
        val start = value ?: cc.tumtum.app.domain.BirthDate.wheelStart(today)
        var day by remember { mutableIntStateOf(start.dayOfMonth) }
        var month by remember { mutableIntStateOf(start.monthValue) }
        var year by remember { mutableIntStateOf(start.year) }
        val maxDay = remember(month, year) { YearMonth.of(year, month).lengthOfMonth() }
        LaunchedEffect(maxDay) { if (day > maxDay) day = maxDay }
        val years = remember(today) { (today.year - 100..today.year).toList() }

        WheelDialog(
            title = label,
            onDismiss = { open = false },
            onOk = {
                onChange(LocalDate.of(year, month, day.coerceAtMost(maxDay)))
                open = false
            },
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WheelColumn(
                    caption = stringResource(R.string.picker_day),
                    items = (1..maxDay).map { "$it" },
                    index = day - 1,
                    onIndex = { day = it + 1 },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    caption = stringResource(R.string.picker_month),
                    items = MONTHS.toList(),
                    index = month - 1,
                    onIndex = { month = it + 1 },
                    modifier = Modifier.weight(1.2f),
                )
                WheelColumn(
                    caption = stringResource(R.string.picker_year),
                    items = years.map { "$it" },
                    index = years.indexOf(year).coerceAtLeast(0),
                    onIndex = { year = years[it] },
                    modifier = Modifier.weight(1.3f),
                )
            }
        }
    }
}

@Composable
fun WheelTimeField(label: String, value: LocalTime, onChange: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    PickerField(label, EventTimes.formatTime(value), modifier) { open = true }
    if (open) {
        var hour by remember { mutableIntStateOf(value.hour) }
        var minute by remember { mutableIntStateOf(value.minute) }
        WheelDialog(
            title = label,
            onDismiss = { open = false },
            onOk = {
                onChange(LocalTime.of(hour, minute))
                open = false
            },
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WheelColumn(
                    caption = stringResource(R.string.picker_hour),
                    items = (0..23).map { "%02d".format(it) },
                    index = hour,
                    onIndex = { hour = it },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    caption = stringResource(R.string.picker_minute),
                    items = (0..59).map { "%02d".format(it) },
                    index = minute,
                    onIndex = { minute = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private val MONTHS = arrayOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

/**
 * One wheel. Five rows tall, the middle one framed; what stops under the
 * frame is the value. Settling snaps to the nearest row, so a value is never
 * half-chosen — and the snap is ours rather than an experimental API, which
 * keeps this compiling on a Compose BOM nobody here can run.
 */
@Composable
private fun WheelColumn(
    caption: String,
    items: List<String>,
    index: Int,
    onIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = index.coerceIn(0, maxOf(items.lastIndex, 0)))
    val itemPx = with(LocalDensity.current) { ITEM_HEIGHT.toPx() }
    val centred by remember(itemPx, items.size) {
        derivedStateOf {
            val raw = state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset / itemPx
            raw.roundToInt().coerceIn(0, maxOf(items.lastIndex, 0))
        }
    }
    // Settling: snap to the row under the frame and report it.
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            if (state.firstVisibleItemScrollOffset != 0) state.animateScrollToItem(centred)
            if (centred != index) onIndex(centred)
        }
    }
    // The value changed from outside (31 → 30 when the month shrinks): follow it.
    LaunchedEffect(index, items.size) {
        if (!state.isScrollInProgress && index != centred) {
            state.scrollToItem(index.coerceIn(0, maxOf(items.lastIndex, 0)))
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(caption, style = TTType.MetaSmall, color = TT.Gray55)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(ITEM_HEIGHT * VISIBLE_ROWS)) {
            // The frame sits behind the rows, exactly one row tall, centred.
            Box(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(ITEM_HEIGHT)
                    .background(TT.Gray10.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = ITEM_HEIGHT * ((VISIBLE_ROWS - 1) / 2)),
            ) {
                items(items.size) { i ->
                    val isCentred = i == centred
                    Box(Modifier.fillMaxWidth().height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
                        Text(
                            items[i],
                            style = if (isCentred) {
                                TTType.ItemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            } else {
                                TTType.Body.copy(fontSize = 17.sp)
                            },
                            color = if (isCentred) TT.Ink else TT.Gray45,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** The field: the house label, the chosen value, and MUDAR — a control that looks like one. */
@Composable
private fun PickerField(
    label: String,
    value: String,
    modifier: Modifier,
    onDark: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    val text = if (onDark) TT.Paper else TT.Ink
    Column(modifier.fillMaxWidth()) {
        Text(label, style = TTType.Meta, color = if (onDark) TT.Gray45 else TT.Gray70)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, if (onDark) TT.Ink600 else TT.Gray25, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, style = TTType.Body, color = text, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.picker_change), style = TTType.Meta, color = text)
        }
    }
}

@Composable
private fun WheelDialog(title: String, onDismiss: () -> Unit, onOk: () -> Unit, wheels: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        title = { Text(title, style = TTType.Meta, color = TT.Gray70) },
        text = { wheels() },
        confirmButton = {
            Text(
                stringResource(R.string.picker_ok),
                style = TTType.Button.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = TT.Ink,
                modifier = Modifier.clickable(onClick = onOk).padding(12.dp),
            )
        },
        dismissButton = {
            Text(
                stringResource(R.string.picker_cancel),
                style = TTType.Button.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
    )
}
