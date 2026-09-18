package cc.tumtum.app.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.HrSource
import cc.tumtum.app.domain.NightAnalyzer
import cc.tumtum.app.domain.SourceState
import cc.tumtum.app.domain.WatchSource
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Health Connect, fonte de FC em lote (§2): leitura retroativa, só dentro da
 * janela do evento (±30min, §7). Sem SDK proprietário. Coexiste com a fonte
 * BLE ao vivo sob a mesma abstração HrSource.
 */
class HealthConnectSource(private val context: Context) : HrSource {

    override val id: String = HrSource.ID_HEALTH_CONNECT

    private val _state = MutableStateFlow(SourceState.IDLE)
    override val state: StateFlow<SourceState> = _state

    /** Fonte de repositório: start/stop só marcam a sessão — a leitura é em lote. */
    override suspend fun start(sessionId: Long) {
        _state.value = if (isAvailable && hasPermission()) SourceState.ACTIVE else SourceState.DEGRADED
    }

    override suspend fun stop() {
        _state.value = SourceState.STOPPED
    }

    val permissions: Set<String> = setOf(HealthPermission.getReadPermission(HeartRateRecord::class))

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    val isAvailable: Boolean get() = sdkStatus() == HealthConnectClient.SDK_AVAILABLE

    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun hasPermission(): Boolean {
        if (!isAvailable) return false
        return client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    /**
     * Lê todas as amostras de FC da janela, agrupadas por origem (app da fonte).
     * Fora da janela do evento, este método simplesmente não é chamado.
     */
    suspend fun readWindowBySource(start: Instant, end: Instant): Map<String, List<HrSample>> {
        if (!isAvailable || !hasPermission()) return emptyMap()
        val out = mutableMapOf<String, MutableList<HrSample>>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                    pageSize = 1000,
                    pageToken = pageToken,
                ),
            )
            response.records.forEach { record ->
                val origin = record.metadata.dataOrigin.packageName
                val list = out.getOrPut(origin) { mutableListOf() }
                record.samples.forEach { s ->
                    if (!s.time.isBefore(start) && !s.time.isAfter(end)) {
                        list += HrSample(time = s.time, bpm = s.beatsPerMinute.toInt())
                    }
                }
            }
            pageToken = response.pageToken
        } while (pageToken != null)
        return out.mapValues { (_, v) -> v.sortedBy { it.time } }
    }

    /**
     * Densidade real por fonte na janela (b4): % de cobertura + intervalo mediano.
     * A melhor fonte é a de maior cobertura; a decisão aparece, nunca é escondida (§7).
     */
    fun sourceDensities(
        bySource: Map<String, List<HrSample>>,
        start: Instant,
        end: Instant,
    ): List<WatchSource> {
        val measured = bySource.map { (pkg, samples) ->
            WatchSource(
                packageName = pkg,
                label = sourceLabel(pkg),
                coveragePct = NightAnalyzer.coveragePct(samples, start, end),
                medianIntervalSec = NightAnalyzer.medianIntervalSec(samples),
                hasData = samples.isNotEmpty(),
                isBest = false,
            )
        }.sortedByDescending { it.coveragePct }
        val bestPkg = measured.firstOrNull { it.hasData }?.packageName
        return measured.map { it.copy(isBest = it.packageName == bestPkg) }
    }

    companion object {
        private val KNOWN_SOURCES = mapOf(
            "com.sec.android.app.shealth" to "Samsung Health",
            "com.google.android.apps.fitness" to "Google Fit",
            "com.polar.polarflow" to "Polar Flow",
            "com.garmin.android.apps.connectmobile" to "Garmin Connect",
            "com.fitbit.FitbitMobile" to "Fitbit",
            "com.xiaomi.wearable" to "Mi Fitness",
            "com.huami.watch.hmwatchmanager" to "Zepp",
            "com.huawei.health" to "Huawei Health",
            "nl.appyhapps.healthsync" to "Health Sync",
            "com.google.android.apps.healthdata" to "Health Connect",
            HrSource.ID_BLE to "Sensor ao vivo",
        )

        fun sourceLabel(packageName: String): String =
            KNOWN_SOURCES[packageName]
                ?: packageName.substringAfterLast('.')
                    .replaceFirstChar { it.uppercase() }
    }
}
