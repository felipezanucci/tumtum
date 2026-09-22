package cc.tumtum.app.data.repo

import cc.tumtum.app.data.db.EventEntity
import cc.tumtum.app.data.db.MarkEntity
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
import cc.tumtum.app.domain.HrSource
import cc.tumtum.app.domain.Moment
import cc.tumtum.app.domain.MomentsSource
import cc.tumtum.app.domain.UploadState
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
    private val capture get() = db.captureDao()

    /** Margem da janela do evento (§7): 30min de cada lado. Fora disso, nenhuma query. */
    private val margin: Duration = Duration.ofMinutes(30)

    val activeEvent: Flow<EventSession?> = db.eventDao().active().map { e ->
        e?.let {
            EventSession(
                it.id, it.name, it.venue, Instant.ofEpochMilli(it.startAt), it.endAt?.let(Instant::ofEpochMilli),
                serverEventId = it.serverEventId, eventType = it.eventType,
            )
        }
    }

    suspend fun startEvent(
        name: String,
        venue: String,
        eventType: String = "concert",
        serverEventId: String? = null,
    ): Long =
        db.eventDao().insert(
            EventEntity(
                name = name.trim(), venue = venue.trim(), startAt = Instant.now().toEpochMilli(),
                eventType = eventType, serverEventId = serverEventId,
            ),
        )

    /** One tap during the capture: the goal, the song, the moment — with the clock of the tap (Etapa 3). */
    suspend fun addMark(eventId: Long, label: String, entryType: String, at: Instant = Instant.now()): Long =
        db.markDao().insert(MarkEntity(eventId = eventId, at = at.toEpochMilli(), label = label, entryType = entryType))

    fun marksCount(eventId: Long): Flow<Int> = db.markDao().countFor(eventId)

    /**
     * The person names the moment (§5.6 of the 19/09 research): saved on the
     * phone at once, and offered to the event's timeline as a mark, so the
     * server names it the same way on the next analysis. Nobody knows the
     * cause better than the person who was there.
     */
    suspend fun nameMoment(nightId: Long, momentId: Long, at: Instant, label: String) {
        val clean = label.trim()
        db.nightDao().setMomentLabel(momentId, clean.ifBlank { null })
        val night = db.nightDao().nightRow(nightId) ?: return
        if (clean.isNotBlank()) addMark(night.eventId, clean, "highlight", at)
    }

    /** Undo the last tap. False when the mark was already on the server — then it stays, honestly. */
    suspend fun removeMark(id: Long): Boolean = db.markDao().deleteUnsynced(id) == 1

    suspend fun closeEvent(eventId: Long, at: Instant = Instant.now()) {
        db.eventDao().close(eventId, at.toEpochMilli())
    }

    /** Amostras da fonte BLE ao vivo dentro da janela, no formato comum do pipeline (§2). */
    private suspend fun bleSamplesIn(eventId: Long, start: Instant, end: Instant): List<HrSample> =
        capture.samplesBetween(eventId, start.toEpochMilli(), end.toEpochMilli())
            .map { HrSample(Instant.ofEpochMilli(it.wallClockMs), it.bpm) }

    /** Snapshot ao vivo: sensor BLE quando presente; senão, lote retroativo do Health Connect. */
    suspend fun liveSnapshot(event: EventSession): LiveSnapshot {
        val start = event.startAt.minus(margin)
        val now = Instant.now()
        val ble = bleSamplesIn(event.id, start, now)
        if (ble.isNotEmpty()) {
            val latest = ble.last()
            return LiveSnapshot(
                currentBpm = latest.takeIf { Duration.between(it.time, now) <= Duration.ofSeconds(15) }?.bpm,
                peakBpm = ble.maxOf { it.bpm },
                momentCount = NightAnalyzer.moments(ble).size,
                bestSourceLabel = HealthConnectSource.sourceLabel(HrSource.ID_BLE),
                coveragePct = NightAnalyzer.coveragePct(ble, event.startAt, now),
            )
        }
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

    /**
     * Mede densidade por fonte na janela fechada do evento (b4, §7), agregando
     * as fontes ativas: Health Connect + o sensor BLE ao vivo, no mesmo pipeline (§2).
     *
     * A margem de 30 min existe para o relógio, cuja gravação pode ter começado
     * antes de o evento ser marcado. Com a cinta, a noite é a captura: começa e
     * termina no relógio de parede do próprio evento. No ensaio de 18/09 a
     * margem virou "30 MIN SEM DADO" e "10% da noite coberta" em cima de uma
     * captura sem um segundo de buraco.
     */
    suspend fun measureSources(event: EventSession, end: Instant = Instant.now()): SourceMeasurement {
        val closedAt = (event.endAt ?: end).coerceAtMost(Instant.now())
        val ble = bleSamplesIn(event.id, event.startAt.minus(margin), closedAt.plus(margin))
        val windowStart = if (ble.isNotEmpty()) event.startAt else event.startAt.minus(margin)
        val windowEnd = if (ble.isNotEmpty()) closedAt else closedAt.plus(margin).coerceAtMost(Instant.now())
        val bySource = health.readWindowBySource(windowStart, windowEnd).toMutableMap()
        if (ble.isNotEmpty()) bySource[HrSource.ID_BLE] = ble.filter { it.time >= windowStart && it.time <= windowEnd }
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
    suspend fun saveNight(
        event: EventSession,
        measurement: SourceMeasurement,
        sourcePackage: String,
        revealAt: Instant? = null,
    ): Long? {
        val samples = measurement.bySource[sourcePackage].orEmpty()
        if (samples.isEmpty()) return null
        val moments = NightAnalyzer.moments(samples)
        val peak = samples.maxBy { it.bpm }
        val eventRow = db.eventDao().byId(event.id)
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
                clockOffsetStartMs = eventRow?.clockOffsetStartMs,
                clockOffsetEndMs = eventRow?.clockOffsetEndMs,
                revealAt = revealAt?.toEpochMilli(),
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

    /** The nights whose card was shared (the skin is saved on Compartilhar): what a public profile shows. */
    fun galleryNights(): Flow<List<GalleryNight>> = db.nightDao().published().map { list -> list.map { it.toGallery() } }

    /**
     * Every night, card or not: what the person's own gallery shows. Until
     * 18/09 the gallery listed only published nights, so a night just
     * captured was nowhere until a card was chosen — the list was making a
     * claim ("2 noites") that the phone's own data contradicted.
     */
    fun allGalleryNights(): Flow<List<GalleryNight>> = db.nightDao().allNights().map { list -> list.map { it.toGallery() } }

    private fun NightEntity.toGallery() = GalleryNight(
        nightId = id,
        label = eventName.uppercase(),
        dateLabel = DATE_FMT.format(Instant.ofEpochMilli(startAt).atZone(ZoneId.systemDefault())),
        peakBpm = peakBpm,
        skin = skin?.let { Skin.valueOf(it) } ?: Skin.PINK,
        published = published,
        photoPath = photoPath,
    )

    /** The card went out with this skin — and, on the black one, with this photo behind it (or none). */
    suspend fun publish(nightId: Long, skin: Skin, photoPath: String? = null) {
        db.nightDao().publish(nightId, skin.name, photoPath)
    }

    /** Apagar conta apaga noites, momentos e reações — irreversível (§7). */
    suspend fun wipeAll() {
        db.nightDao().deleteAllMoments()
        db.nightDao().deleteAllSamples()
        db.nightDao().deleteAll()
        db.eventDao().deleteAll()
        db.markDao().deleteAll()
        capture.deleteAllSamples()
        capture.deleteAllRr()
        capture.deleteAllMotion()
        capture.deleteAllConnectionEvents()
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
            revealAt = night.revealAt?.let(Instant::ofEpochMilli),
            samples = domainSamples,
            gaps = if (domainSamples.isEmpty()) listOf(Gap(start, end)) else NightAnalyzer.gaps(domainSamples, start, end),
            moments = moments.sortedByDescending { it.bpm }
                .map { Moment(it.bpm, Instant.ofEpochMilli(it.at), it.durationSec, it.isPeak, it.label, id = it.id) },
            serverSessionId = night.serverSessionId,
            uploadState = runCatching { UploadState.valueOf(night.uploadState) }.getOrDefault(UploadState.PENDING),
            uploadError = night.uploadError,
            momentsSource = runCatching { MomentsSource.valueOf(night.momentsSource) }.getOrDefault(MomentsSource.LOCAL),
            photoPath = night.photoPath,
        )
    }

    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yy")
    }
}
