package cc.tumtum.app.data.repo

import cc.tumtum.app.data.db.EventEntity
import cc.tumtum.app.data.db.MomentEntity
import cc.tumtum.app.data.db.NightEntity
import cc.tumtum.app.data.db.NightWithData
import cc.tumtum.app.data.db.SampleEntity
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.data.health.HealthConnectSource
import cc.tumtum.app.domain.EventSession
import cc.tumtum.app.domain.Gap
import cc.tumtum.app.domain.GalleryNight
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.Moment
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.NightAnalyzer
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.domain.WatchSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Medição de fontes na janela de um evento (b4). */
data class SourceMeasurement(
    val windowStart: Instant,
    val windowEnd: Instant,
    val bySource: Map<String, List<HrSample>>,
    val sources: List<WatchSource>,
)

/** Snapshot da captura ao vivo — sempre resultado de leitura em lote, nunca sensor. */
data class LiveSnapshot(
    val currentBpm: Int?,
    val peakBpm: Int?,
    val momentCount: Int,
    val bestSourceLabel: String?,
    val coveragePct: Int,
)

class NightRepository(
    private val db: TumTumDatabase,
    private val health: HealthConnectSource,
) {
    /** Margem da janela do evento (§7): 30min de cada lado. Fora disso, nenhuma query. */
    private val margin: Duration = Duration.ofMinutes(30)

    val activeEvent: Flow<EventSession?> = db.eventDao().active().map { e ->
        e?.let { EventSession(it.id, it.name, it.venue, Instant.ofEpochMilli(it.startAt), it.endAt?.let(Instant::ofEpochMilli)) }
    }

    suspend fun startEvent(name: String, venue: String): Long =
        db.eventDao().insert(EventEntity(name = name.trim(), venue = venue.trim(), startAt = Instant.now().toEpochMilli()))

    suspend fun closeEvent(eventId: Long, at: Instant = Instant.now()) {
        db.eventDao().close(eventId, at.toEpochMilli())
    }

    /** Snapshot ao vivo: lote retroativo do início da janela até agora. */
    suspend fun liveSnapshot(event: EventSession): LiveSnapshot {
        val start = event.startAt.minus(margin)
        val now = Instant.now()
        val bySource = health.readWindowBySource(start, now)
        val best = health.sourceDensities(bySource, start, now).firstOrNull { it.hasData }
        val samples = best?.let { bySource[it.packageName] }.orEmpty()
        val latest = samples.lastOrNull()
        val current = latest?.takeIf { Duration.between(it.time, now) <= Duration.ofMinutes(5) }?.bpm
        val moments = if (samples.isEmpty()) emptyList() else NightAnalyzer.moments(samples)
        return LiveSnapshot(
            currentBpm = current,
            peakBpm = samples.maxOfOrNull { it.bpm },
            momentCount = moments.size,
            bestSourceLabel = best?.label,
            coveragePct = best?.coveragePct ?: 0,
        )
    }

    /** Mede densidade por fonte na janela fechada do evento (b4, §7). */
    suspend fun measureSources(event: EventSession, end: Instant = Instant.now()): SourceMeasurement {
        val windowStart = event.startAt.minus(margin)
        val windowEnd = (event.endAt ?: end).plus(margin).coerceAtMost(Instant.now())
        val bySource = health.readWindowBySource(windowStart, windowEnd)
        return SourceMeasurement(
            windowStart = windowStart,
            windowEnd = windowEnd,
            bySource = bySource,
            sources = health.sourceDensities(bySource, windowStart, windowEnd),
        )
    }

    /**
     * Salva a noite com a fonte escolhida. Retorna null quando a fonte não tem
     * amostra nenhuma — "Não achamos batida nessa janela."
     */
    suspend fun saveNight(event: EventSession, measurement: SourceMeasurement, sourcePackage: String): Long? {
        val samples = measurement.bySource[sourcePackage].orEmpty()
        if (samples.isEmpty()) return null
        val moments = NightAnalyzer.moments(samples)
        val peak = samples.maxBy { it.bpm }
        val nightId = db.nightDao().insert(
            NightEntity(
                eventId = event.id,
                eventName = event.name,
                venue = event.venue,
                startAt = measurement.windowStart.toEpochMilli(),
                endAt = measurement.windowEnd.toEpochMilli(),
                peakBpm = peak.bpm,
                peakAt = peak.time.toEpochMilli(),
                coveragePct = NightAnalyzer.coveragePct(samples, measurement.windowStart, measurement.windowEnd),
                momentCount = moments.size,
                sourcePackage = sourcePackage,
                sourceLabel = HealthConnectSource.sourceLabel(sourcePackage),
            ),
        )
        db.nightDao().insertSamples(samples.map { SampleEntity(nightId = nightId, time = it.time.toEpochMilli(), bpm = it.bpm) })
        db.nightDao().insertMoments(
            moments.map { MomentEntity(nightId = nightId, bpm = it.bpm, at = it.at.toEpochMilli(), durationSec = it.durationSec, isPeak = it.isPeak) },
        )
        return nightId
    }

    fun nights(): Flow<List<Night>> = db.nightDao().nightsWithData().map { list -> list.map { it.toDomain() } }

    fun night(id: Long): Flow<Night?> = db.nightDao().nightWithData(id).map { it?.toDomain() }

    fun galleryNights(): Flow<List<GalleryNight>> = db.nightDao().published().map { list ->
        list.map { n ->
            GalleryNight(
                nightId = n.id,
                label = n.eventName.uppercase(),
                dateLabel = DATE_FMT.format(Instant.ofEpochMilli(n.startAt).atZone(ZoneId.systemDefault())),
                peakBpm = n.peakBpm,
                skin = n.skin?.let { Skin.valueOf(it) } ?: Skin.PINK,
            )
        }
    }

    suspend fun publish(nightId: Long, skin: Skin) {
        db.nightDao().publish(nightId, skin.name)
    }

    /** Apagar conta apaga noites, momentos e reações — irreversível (§7). */
    suspend fun wipeAll() {
        db.nightDao().deleteAllMoments()
        db.nightDao().deleteAllSamples()
        db.nightDao().deleteAll()
        db.eventDao().deleteAll()
    }

    private fun NightWithData.toDomain(): Night {
        val start = Instant.ofEpochMilli(night.startAt)
        val end = Instant.ofEpochMilli(night.endAt)
        val domainSamples = samples.sortedBy { it.time }.map { HrSample(Instant.ofEpochMilli(it.time), it.bpm) }
        return Night(
            id = night.id,
            eventName = night.eventName,
            venue = night.venue,
            date = start,
            startAt = start,
            endAt = end,
            peakBpm = night.peakBpm,
            peakAt = Instant.ofEpochMilli(night.peakAt),
            coveragePct = night.coveragePct,
            momentCount = night.momentCount,
            sourcePackage = night.sourcePackage,
            sourceLabel = night.sourceLabel,
            skin = night.skin?.let { Skin.valueOf(it) },
            published = night.published,
            samples = domainSamples,
            gaps = if (domainSamples.isEmpty()) listOf(Gap(start, end)) else NightAnalyzer.gaps(domainSamples, start, end),
            moments = moments.sortedByDescending { it.bpm }
                .map { Moment(it.bpm, Instant.ofEpochMilli(it.at), it.durationSec, it.isPeak) },
        )
    }

    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yy")
    }
}
