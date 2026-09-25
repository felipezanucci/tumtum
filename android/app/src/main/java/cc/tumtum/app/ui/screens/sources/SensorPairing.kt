package cc.tumtum.app.ui.screens.sources

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.R
import cc.tumtum.app.data.ble.BleDevice
import cc.tumtum.app.data.ble.BlePermissions
import cc.tumtum.app.data.ble.BleScanner
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * §10 — pareamento do sensor BLE: varredura → lista com nome e RSSI →
 * tocar para parear. O endereço fica lembrado e a reconexão é automática.
 */
@Composable
fun SensorSection(prefs: UserPrefs, bleName: String?, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var showScan by remember { mutableStateOf(false) }

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
                    modifier = Modifier.clickable { showScan = true }.padding(4.dp),
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
                onClick = { showScan = true },
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.sensor_footnote), style = TTType.Footnote, color = TT.Gray45)
    }

    if (showScan) {
        SensorScanSheet(
            onDismiss = { showScan = false },
            onPick = { device ->
                showScan = false
                scope.launch { prefs.setSensor(device.address, device.name) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScanSheet(onDismiss: () -> Unit, onPick: (BleDevice) -> Unit) {
    val context = LocalContext.current
    var permitted by remember { mutableStateOf(BlePermissions.granted(context)) }
    val found = remember { mutableStateMapOf<String, BleDevice>() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permitted = BlePermissions.granted(context) }

    LaunchedEffect(Unit) {
        if (!permitted) launcher.launch(BlePermissions.withNotifications())
    }
    // Bluetooth off closes the scan at once; the sheet used to say "ele
    // aparece aqui" over a search that was not running (25/09).
    var attempt by remember { mutableStateOf(0) }
    val bluetoothOn = remember(permitted, attempt) { BleScanner(context).isBluetoothOn() }
    LaunchedEffect(permitted, attempt) {
        if (permitted && bluetoothOn) {
            runCatching { BleScanner(context).scan().collect { device -> found[device.address] = device } }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 40.dp)) {
            Text(stringResource(R.string.sensor_scan_title), style = TTType.TitleSmall, color = TT.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    when {
                        !permitted -> R.string.sensor_scan_needs_permission
                        !bluetoothOn -> R.string.setup_bluetooth_off
                        found.isEmpty() -> R.string.sensor_scan_searching
                        else -> R.string.sensor_scan_tap
                    },
                ),
                style = TTType.Body,
                color = TT.Gray45,
            )
            Spacer(Modifier.height(20.dp))
            val devices = found.values.sortedByDescending { it.rssi }
            devices.forEach { device ->
                SensorDeviceRow(device, onPick)
                Spacer(Modifier.height(10.dp))
            }
            if (!permitted) {
                Spacer(Modifier.height(8.dp))
                TTButton(
                    stringResource(R.string.sensor_scan_grant),
                    TTButtonStyle.Ink,
                    onClick = { launcher.launch(BlePermissions.withNotifications()) },
                )
            } else if (!bluetoothOn) {
                // "Liga ele e procura de novo" needs a way to do the second half here (25/09).
                Spacer(Modifier.height(8.dp))
                TTButton(stringResource(R.string.setup_search_again), TTButtonStyle.Ink, onClick = { attempt++ })
            }
        }
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
