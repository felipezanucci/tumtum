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
import cc.tumtum.app.domain.OnSkinTracker
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
 * The setup step after permissions: **the watch first** (25/09, b196 round).
 *
 * b196 rebuilt this step around a sensor search, from Felipe's 25/09 test on
 * a new account (the previous person's Polar pre-paired, no search, no word
 * that anything was done). The same day he set the order: *"o usuário final
 * mesmo, praticamente todo mundo vai usar só relógio. Dificilmente alguém vai
 * usar um sensor como o Polar."* So the watch leads and the strap is the
 * quiet second road; the pilot's strap phones are set up by the operator.
 *
 * States, each saying only what is true:
 * - **nothing chosen**: the watches that wrote heart rate to Health Connect in
 *   the last 24 hours, the first one in Pink; when there is none, what makes
 *   a watch show up, and a way to look again. Under it, "Tem uma cinta de
 *   peito?" and the sensor search;
 * - **watch chosen**: "Pronto, relógio conectado", the way home, and Trocar;
 * - **searching** a sensor: what was found, or why nothing is there yet;
 * - **sensor paired**: the app connects and waits for a beat that proves a
 *   person — one with an R-R interval ([OnSkinTracker]). Only that earns
 *   "Pronto, conexão feita"; a strap on the table is said to be one.
 *
 * On black since the b198 round (Felipe: *"deixa essa tela um pouco mais
 * colorida"*): the brand's digital default. Pink is the one thing to do on
 * each state and nothing else is coloured — b201 marked the strap road in
 * Toxic Yellow, and in a hand Felipe sent it back to white (*"deixa só o
 * botão em rosa"*). The permission screen before it stays white and quiet:
 * consent is where the brand chooses trust over fun.
 */
@Composable
fun SetupScreen(nav: NavHostController, startSearching: Boolean = false) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    // Configurações opens this screen already searching (Routes.SensorSearch):
    // one road to a strap, whichever door it starts from.
    var searching by remember { mutableStateOf(startSearching) }

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
            .background(TT.Ink)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        BackArrow(onClick = { nav.popBackStack() }, onDark = true)
        Spacer(Modifier.height(34.dp))

        when {
            searching -> SearchingStep(
                onPick = { device ->
                    searching = false
                    scope.launch { container.prefs.setSensor(device.address, device.name) }
                },
                // From Configurações, stopping goes back where the person came from.
                onStop = { if (startSearching) nav.popBackStack() else searching = false },
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

            state.sourcePackage != null -> {
                Text(stringResource(R.string.setup_watch_ready_title), style = TTType.TitleSmall, color = TT.Rose)
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.setup_watch_ready_body, state.sourceLabel.orEmpty()),
                    style = TTType.Body,
                    color = TT.Gray45,
                )
                Spacer(Modifier.height(30.dp))
                TTButton(stringResource(R.string.setup_home), TTButtonStyle.Rose, onClick = { goHome() })
                Spacer(Modifier.height(10.dp))
                TTButton(
                    stringResource(R.string.setup_watch_change),
                    TTButtonStyle.OutlineOnDark,
                    onClick = { scope.launch { container.prefs.clearSource() } },
                )
            }

            else -> {
                Text(stringResource(R.string.setup_title), style = TTType.TitleSmall, color = TT.Paper)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.setup_body), style = TTType.Body, color = TT.Gray45)
                Spacer(Modifier.height(24.dp))
                WatchChoices(
                    onUse = { pkg, label -> scope.launch { container.prefs.setSource(pkg, label) } },
                )
                Spacer(Modifier.height(10.dp))
                TTButton(stringResource(R.string.sources_skip), TTButtonStyle.OutlineOnDark, onClick = { goHome() })
                Spacer(Modifier.height(40.dp))
                Text(stringResource(R.string.setup_strap_section), style = TTType.Meta, color = TT.Paper)
                Spacer(Modifier.height(10.dp))
                TTButton(stringResource(R.string.setup_strap_search), TTButtonStyle.OutlineOnDark, onClick = { searching = true })
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

    Text(stringResource(R.string.setup_searching_title), style = TTType.TitleSmall, color = TT.Paper)
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
        SensorDeviceRow(device, onPick, onDark = true)
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
    TTButton(stringResource(R.string.setup_stop), TTButtonStyle.OutlineOnDark, onClick = onStop)
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
    // When the sensor last said it feels no skin (25/09: a Polar on the table
    // sent numbers, and this screen called them a beat).
    val offSkin = remember(address) { MutableStateFlow<Long?>(null) }
    val offSkinAt by offSkin.collectAsStateWithLifecycle()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val since = remember(address) { System.currentTimeMillis() }

    // A connection of our own, only while this screen is up — the proof that
    // the sensor answers, not a capture. Nothing is stored.
    DisposableEffect(address) {
        if (!BlePermissions.granted(context)) return@DisposableEffect onDispose { }
        // "Pronto" needs proof of a person (25/09, night 18): a Polar off the
        // skin repeats its last number, then sends 0. A beat with an R-R is the
        // proof; a number alone is not.
        val tracker = OnSkinTracker()
        val source = BleHrSource(context.applicationContext) { event ->
            if (event is BleEvent.Sample) {
                val m = event.measurement
                val isBeat = tracker.accept(m.bpm, m.contactStatus, m.rrIntervalsMs.isNotEmpty())
                if (!isBeat) {
                    offSkin.value = System.currentTimeMillis()
                } else if (tracker.proven) {
                    beat.value = m.bpm to System.currentTimeMillis()
                }
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
    val noSkin = offSkinAt?.let { off -> current == null || off > current.second } == true
    if (noSkin) {
        Text(stringResource(R.string.setup_paired_title), style = TTType.TitleSmall, color = TT.Paper)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.setup_no_contact, name), style = TTType.Body, color = TT.Gray45)
    } else if (current != null) {
        Text(stringResource(R.string.setup_ready_title), style = TTType.TitleSmall, color = TT.Rose)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.setup_ready_body, name), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (fresh) "${current.first}" else "--", style = TTType.HeroSmall, color = TT.Paper)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (fresh) R.string.setup_beating_now else R.string.setup_beat_paused),
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
    } else {
        Text(stringResource(R.string.setup_paired_title), style = TTType.TitleSmall, color = TT.Paper)
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
    TTButton(stringResource(R.string.setup_change), TTButtonStyle.OutlineOnDark, onClick = onChange)
}

/**
 * The watches that wrote heart rate to Health Connect in the last 24 hours,
 * each with its own "Usar", the first in Pink. When there is none, the one
 * thing that makes a watch show up, and a way to look again.
 */
@Composable
private fun WatchChoices(onUse: (String, String) -> Unit) {
    val container = appContainer()
    var tick by remember { mutableStateOf(0) }
    val reading by produceState<Pair<Boolean, SourceMeasurement?>?>(null, tick) {
        value = null
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
    val r = reading
    if (r == null) {
        Text(stringResource(R.string.setup_watch_looking), style = TTType.Footnote, color = TT.Gray45)
        return
    }
    val withData = r.second?.sources.orEmpty().filter { it.hasData }
    when {
        !r.first -> Text(stringResource(R.string.setup_watch_no_permission), style = TTType.Body, color = TT.Paper)
        withData.isEmpty() -> {
            Text(stringResource(R.string.setup_watch_none), style = TTType.Body, color = TT.Paper)
            Spacer(Modifier.height(14.dp))
            // No watch yet: looking again is the one thing to do, so it is the Pink one.
            TTButton(stringResource(R.string.setup_search_again), TTButtonStyle.Rose, onClick = { tick++ })
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            withData.forEachIndexed { i, source ->
                SourceCard(source = source, selected = false, setupMode = true, onClick = {}, onDark = true)
                TTButton(
                    stringResource(R.string.sources_use, source.label),
                    if (i == 0) TTButtonStyle.Rose else TTButtonStyle.OutlineOnDark,
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
