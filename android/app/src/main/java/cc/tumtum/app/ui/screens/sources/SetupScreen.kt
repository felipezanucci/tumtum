package cc.tumtum.app.ui.screens.sources

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.ble.BleDevice
import cc.tumtum.app.data.ble.BleEvent
import cc.tumtum.app.data.ble.BleHrSource
import cc.tumtum.app.data.ble.BlePermissions
import cc.tumtum.app.data.ble.BleScanner
import cc.tumtum.app.data.repo.SourceMeasurement
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * The setup step after permissions: bring the sensor (25/09, rebuilt from
 * Felipe's test on a new account).
 *
 * The old step was the end-of-night source picker run on the last 24 hours.
 * On a new account it opened with the previous person's Polar H10 already
 * "paired", no way to search for one's own, the after-a-night line "Não
 * achamos batida nessa janela" where there was no window, no word that the
 * step was done, and a way out ("Fazer isso depois") that claimed something
 * was left undone when it was not. Felipe: "tem que aparecer um campo de
 * procurar o dispositivo… depois de conectado, pronto, conexão feita, e um
 * botão pra ir pra home."
 *
 * Three states, each saying only what is true:
 * - **nothing paired**: Pink "Procurar dispositivo", quiet "Fazer isso depois";
 *   a watch in Health Connect stays a second, quieter road;
 * - **searching**: what was found, and an honest line when nothing is —
 *   Bluetooth off, no permission, or simply nothing yet;
 * - **paired**: the app connects and waits for a beat. Only a beat earns
 *   "Pronto, conexão feita", with the live number as the proof; until then
 *   the sensor is "pareado", which is all the phone knows.
 */
@Composable
fun SetupScreen(nav: NavHostController) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var searching by remember { mutableStateOf(false) }

    fun goHome() {
        scope.launch {
            container.prefs.setOnboarded()
            nav.navigate(Routes.Feed) { popUpTo(0) { inclusive = true } }
        }
    }

    val state = user ?: return
    val address = state.bleAddress
    val name = state.bleName

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        BackArrow(onClick = { nav.popBackStack() })
        Spacer(Modifier.height(34.dp))

        when {
            searching -> SearchingStep(
                onPick = { device ->
                    searching = false
                    scope.launch { container.prefs.setSensor(device.address, device.name) }
                },
                onStop = { searching = false },
            )

            address != null -> PairedStep(
                address = address,
                name = name ?: stringResource(R.string.setup_sensor_fallback),
                onHome = { goHome() },
                onChange = {
                    scope.launch {
                        container.prefs.clearSensor()
                        searching = true
                    }
                },
            )

            else -> {
                Text(stringResource(R.string.setup_title), style = TTType.TitleSmall, color = TT.Ink)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.setup_body), style = TTType.Body, color = TT.Gray45)
                Spacer(Modifier.height(28.dp))
                TTButton(stringResource(R.string.setup_search), TTButtonStyle.Rose, onClick = { searching = true })
                Spacer(Modifier.height(10.dp))
                TTButton(stringResource(R.string.sources_skip), TTButtonStyle.Outline, onClick = { goHome() })
                Spacer(Modifier.height(40.dp))
                WatchRoad(
                    onUse = { pkg, label ->
                        scope.launch {
                            container.prefs.setSource(pkg, label)
                            goHome()
                        }
                    },
                )
            }
        }
    }
}

/** The search, inline: what was found, or why nothing is there yet. */
@Composable
private fun SearchingStep(onPick: (BleDevice) -> Unit, onStop: () -> Unit) {
    val context = LocalContext.current
    var permitted by remember { mutableStateOf(BlePermissions.granted(context)) }
    var attempt by remember { mutableStateOf(0) }
    val bluetoothOn = remember(attempt, permitted) { BleScanner(context).isBluetoothOn() }
    val found = remember { mutableStateMapOf<String, BleDevice>() }
    var startedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permitted = BlePermissions.granted(context) }

    LaunchedEffect(Unit) {
        if (!permitted) launcher.launch(BlePermissions.withNotifications())
    }
    LaunchedEffect(permitted, attempt, bluetoothOn) {
        startedAt = System.currentTimeMillis()
        if (permitted && bluetoothOn) {
            runCatching { BleScanner(context).scan().collect { device -> found[device.address] = device } }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val quietFor = Duration.ofMillis(now - startedAt)

    Text(stringResource(R.string.setup_searching_title), style = TTType.TitleSmall, color = TT.Ink)
    Spacer(Modifier.height(10.dp))
    Text(
        stringResource(
            when {
                !permitted -> R.string.sensor_scan_needs_permission
                !bluetoothOn -> R.string.setup_bluetooth_off
                found.isNotEmpty() -> R.string.setup_tap
                quietFor.seconds >= NOTHING_YET_SECONDS -> R.string.setup_searching_none
                else -> R.string.setup_searching_body
            },
        ),
        style = TTType.Body,
        color = TT.Gray45,
    )
    Spacer(Modifier.height(22.dp))
    found.values.sortedByDescending { it.rssi }.forEach { device ->
        SensorDeviceRow(device, onPick)
        Spacer(Modifier.height(10.dp))
    }
    Spacer(Modifier.height(12.dp))
    when {
        !permitted -> TTButton(
            stringResource(R.string.sensor_scan_grant),
            TTButtonStyle.Rose,
            onClick = { launcher.launch(BlePermissions.withNotifications()) },
        )
        !bluetoothOn -> TTButton(stringResource(R.string.setup_search_again), TTButtonStyle.Rose, onClick = { attempt++ })
        else -> Unit
    }
    Spacer(Modifier.height(10.dp))
    TTButton(stringResource(R.string.setup_stop), TTButtonStyle.Outline, onClick = onStop)
}

/**
 * A sensor is paired: connect and wait for a beat. The words follow what the
 * phone actually has — a beat is "conexão feita"; an address is "pareado".
 */
@Composable
private fun PairedStep(address: String, name: String, onHome: () -> Unit, onChange: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val beat = remember(address) { MutableStateFlow<Pair<Int, Long>?>(null) }
    val last by beat.collectAsStateWithLifecycle()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val since = remember(address) { System.currentTimeMillis() }

    // A connection of our own, only while this screen is up — the proof that
    // the sensor answers, not a capture. Nothing is stored.
    DisposableEffect(address) {
        if (!BlePermissions.granted(context)) return@DisposableEffect onDispose { }
        val source = BleHrSource(context.applicationContext) { event ->
            if (event is BleEvent.Sample && event.measurement.bpm > 0) {
                beat.value = event.measurement.bpm to System.currentTimeMillis()
            }
        }
        source.address = address
        val job = scope.launch { runCatching { source.start(0L) } }
        onDispose {
            job.cancel()
            source.shutdown()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val current = last
    val fresh = current != null && now - current.second <= FRESH_MS
    if (current != null) {
        Text(stringResource(R.string.setup_ready_title), style = TTType.TitleSmall, color = TT.Ink)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.setup_ready_body, name), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (fresh) "${current.first}" else "--", style = TTType.HeroSmall, color = TT.Ink)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (fresh) R.string.setup_beating_now else R.string.setup_beat_paused),
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
    } else {
        Text(stringResource(R.string.setup_paired_title), style = TTType.TitleSmall, color = TT.Ink)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(
                if (now - since >= NOTHING_YET_SECONDS * 1_000L) R.string.setup_paired_no_beat else R.string.setup_paired_waiting,
                name,
            ),
            style = TTType.Body,
            color = TT.Gray45,
        )
    }
    Spacer(Modifier.height(30.dp))
    TTButton(stringResource(R.string.setup_home), TTButtonStyle.Rose, onClick = onHome)
    Spacer(Modifier.height(10.dp))
    TTButton(stringResource(R.string.setup_change), TTButtonStyle.Outline, onClick = onChange)
}

/**
 * The second road: a watch that writes heart rate to Health Connect. Quieter
 * than the sensor, and said plainly when there is nothing to offer.
 */
@Composable
private fun WatchRoad(onUse: (String, String) -> Unit) {
    val container = appContainer()
    val reading by produceState<Pair<Boolean, SourceMeasurement?>?>(initialValue = null) {
        val granted = runCatching { container.health.hasPermission() }.getOrDefault(false)
        value = if (!granted) {
            false to null
        } else {
            val end = Instant.now()
            val start = end.minus(Duration.ofHours(24))
            val bySource = runCatching { container.health.readWindowBySource(start, end) }.getOrDefault(emptyMap())
            true to SourceMeasurement(start, end, bySource, container.health.sourceDensities(bySource, start, end))
        }
    }
    val r = reading ?: return
    Text(stringResource(R.string.setup_watch_section), style = TTType.Meta, color = TT.Gray70)
    Spacer(Modifier.height(10.dp))
    val withData = r.second?.sources.orEmpty().filter { it.hasData }
    when {
        !r.first -> Text(stringResource(R.string.setup_watch_no_permission), style = TTType.Footnote, color = TT.Gray45)
        withData.isEmpty() -> Text(stringResource(R.string.setup_watch_none), style = TTType.Footnote, color = TT.Gray45)
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            withData.forEach { source ->
                SourceCard(source = source, selected = false, setupMode = true, onClick = {})
                TTButton(
                    stringResource(R.string.sources_use, source.label),
                    TTButtonStyle.Outline,
                    onClick = { onUse(source.packageName, source.label) },
                )
            }
        }
    }
}

/** How long a search or a paired sensor stays silent before the screen says so. */
private const val NOTHING_YET_SECONDS = 15L

/** A beat older than this is not "batendo agora". */
private const val FRESH_MS = 5_000L
