package cc.tumtum.app.ui.screens.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.R
import cc.tumtum.app.data.ble.BleDevice
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * §10 — the strap in Configurações: the paired sensor, with TROCAR and
 * REMOVER, or a way to connect one.
 *
 * Connecting and changing open **the same screen setup uses**, already
 * searching (25/09, Felipe on b204: *"nos dois caminhos, a experiência tem
 * que ser igual"*). This section had its own bottom sheet, which paired a
 * strap on the tap — no beat waited for, no "sem contato", so a strap on a
 * table could be paired from here with nothing said. There is one road now,
 * and it ends in proof of a person.
 */
@Composable
fun SensorSection(prefs: UserPrefs, bleName: String?, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.sensor_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        if (bleName != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(TT.Acid))
                Text(bleName, style = TTType.BodySmall, color = TT.Ink, modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.sensor_change),
                    style = TTType.Meta.copy(fontSize = 11.sp),
                    color = TT.Gray45,
                    modifier = Modifier.clickable(onClick = onSearch).padding(4.dp),
                )
                Text(
                    stringResource(R.string.sensor_remove),
                    style = TTType.Meta.copy(fontSize = 11.sp),
                    color = TT.Gray45,
                    modifier = Modifier.clickable { scope.launch { prefs.clearSensor() } }.padding(4.dp),
                )
            }
        } else {
            // CTA principal da seção — rosa, como manda o sistema (acento de criação).
            TTButton(
                stringResource(R.string.sensor_connect),
                TTButtonStyle.Rose,
                onClick = onSearch,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.sensor_footnote), style = TTType.Footnote, color = TT.Gray45)
    }
}

/** One sensor found by a search: name, address and signal; a tap picks it. */
@Composable
internal fun SensorDeviceRow(device: BleDevice, onPick: (BleDevice) -> Unit, onDark: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (onDark) TT.Ink600 else TT.Gray10, RoundedCornerShape(12.dp))
            .clickable { onPick(device) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(device.name, style = TTType.ItemTitle.copy(fontSize = 16.sp), color = if (onDark) TT.Paper else TT.Ink)
            Text(device.address, style = TTType.Footnote, color = TT.Gray45)
        }
        Badge("${device.rssi} dBm", hPad = 8.dp, vPad = 4.dp)
    }
}
