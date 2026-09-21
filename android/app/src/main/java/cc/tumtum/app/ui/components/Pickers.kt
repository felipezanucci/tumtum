package cc.tumtum.app.ui.components

import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cc.tumtum.app.R
import cc.tumtum.app.domain.EventTimes
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.LocalDate
import java.time.LocalTime

/**
 * A time is never typed (Felipe, 21/09): every date or time field in the
 * product is the platform's picker — "aquele campo onde a pessoa rola as
 * horas". These are the Android spinners, inflated from two tiny layouts
 * (the attribute that picks the wheel over the calendar only exists in XML),
 * shown in a dialog on tap. The field itself reads like a TTField and says
 * MUDAR, so it is clearly a thing to touch and never a thing to type into.
 * No new dependency: the wheels ship with Android since API 21.
 */
@Composable
fun WheelDateField(label: String, value: LocalDate, onChange: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    PickerField(label, EventTimes.formatDate(value), modifier) { open = true }
    if (open) {
        var pending by remember { mutableStateOf(value) }
        WheelDialog(
            onDismiss = { open = false },
            onOk = {
                onChange(pending)
                open = false
            },
        ) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.wheel_date, null) as DatePicker).apply {
                        init(value.year, value.monthValue - 1, value.dayOfMonth) { _, y, m, d ->
                            pending = LocalDate.of(y, m + 1, d)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun WheelTimeField(label: String, value: LocalTime, onChange: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    PickerField(label, EventTimes.formatTime(value), modifier) { open = true }
    if (open) {
        var pending by remember { mutableStateOf(value) }
        WheelDialog(
            onDismiss = { open = false },
            onOk = {
                onChange(pending)
                open = false
            },
        ) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.wheel_time, null) as TimePicker).apply {
                        setIs24HourView(true)
                        hour = value.hour
                        minute = value.minute
                        setOnTimeChangedListener { _, h, m -> pending = LocalTime.of(h, m) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The field: the house label, the picked value, and MUDAR — a button that looks like one. */
@Composable
private fun PickerField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Column(modifier.fillMaxWidth()) {
        Text(label, style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, TT.Gray25, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, style = TTType.Body, color = TT.Ink, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.picker_change), style = TTType.Meta, color = TT.Ink)
        }
    }
}

@Composable
private fun WheelDialog(onDismiss: () -> Unit, onOk: () -> Unit, wheel: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        text = { wheel() },
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
