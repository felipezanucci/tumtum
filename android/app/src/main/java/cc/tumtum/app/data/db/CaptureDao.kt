package cc.tumtum.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CaptureDao {
    @Insert
    suspend fun insertSample(sample: BleSampleEntity)

    @Insert
    suspend fun insertRr(rr: List<RrIntervalEntity>)

    @Insert
    suspend fun insertMotion(motion: MotionEntity)

    @Insert
    suspend fun insertConnectionEvent(event: ConnectionEventEntity)

    @Query("SELECT * FROM ble_samples WHERE eventId = :eventId AND wallClockMs BETWEEN :fromMs AND :toMs ORDER BY wallClockMs")
    suspend fun samplesBetween(eventId: Long, fromMs: Long, toMs: Long): List<BleSampleEntity>

    @Query("SELECT * FROM ble_samples WHERE eventId = :eventId ORDER BY wallClockMs")
    suspend fun samplesForEvent(eventId: Long): List<BleSampleEntity>

    @Query("SELECT * FROM ble_samples WHERE eventId = :eventId ORDER BY wallClockMs DESC LIMIT 1")
    suspend fun lastSample(eventId: Long): BleSampleEntity?

    @Query("SELECT COUNT(*) FROM ble_samples WHERE eventId = :eventId")
    suspend fun sampleCount(eventId: Long): Long

    @Query("SELECT * FROM rr_intervals WHERE eventId = :eventId ORDER BY wallClockMs")
    suspend fun rrForEvent(eventId: Long): List<RrIntervalEntity>

    /** The instants of the readings that carried an R-R (they share the sample's wallClockMs). */
    @Query("SELECT DISTINCT wallClockMs FROM rr_intervals WHERE eventId = :eventId AND wallClockMs BETWEEN :fromMs AND :toMs")
    suspend fun rrTimesBetween(eventId: Long, fromMs: Long, toMs: Long): List<Long>

    @Query("SELECT * FROM motion WHERE eventId = :eventId ORDER BY wallClockMs")
    suspend fun motionForEvent(eventId: Long): List<MotionEntity>

    @Query("SELECT * FROM connection_events WHERE eventId = :eventId ORDER BY wallClockMs")
    suspend fun connectionEventsForEvent(eventId: Long): List<ConnectionEventEntity>

    @Query("DELETE FROM ble_samples")
    suspend fun deleteAllSamples()

    @Query("DELETE FROM rr_intervals")
    suspend fun deleteAllRr()

    @Query("DELETE FROM motion")
    suspend fun deleteAllMotion()

    @Query("DELETE FROM connection_events")
    suspend fun deleteAllConnectionEvents()

    // Local minimisation (26/09): once a night is saved, its event's raw
    // capture goes — the per-packet readings, the R-R intervals, the phone's
    // motion and the connection log. The night keeps its beats (`samples`)
    // and its moments; nothing on screen reads the raw tables after that.
    @Query("DELETE FROM ble_samples WHERE eventId = :eventId")
    suspend fun deleteSamplesOfEvent(eventId: Long)

    @Query("DELETE FROM rr_intervals WHERE eventId = :eventId")
    suspend fun deleteRrOfEvent(eventId: Long)

    @Query("DELETE FROM motion WHERE eventId = :eventId")
    suspend fun deleteMotionOfEvent(eventId: Long)

    @Query("DELETE FROM connection_events WHERE eventId = :eventId")
    suspend fun deleteConnectionEventsOfEvent(eventId: Long)

    // A deleted account's raw capture (25/09): only for events that no longer exist.
    @Query("DELETE FROM ble_samples WHERE eventId IN (:eventIds) AND eventId NOT IN (SELECT id FROM events)")
    suspend fun deleteSamplesOf(eventIds: List<Long>)

    @Query("DELETE FROM rr_intervals WHERE eventId IN (:eventIds) AND eventId NOT IN (SELECT id FROM events)")
    suspend fun deleteRrOf(eventIds: List<Long>)

    @Query("DELETE FROM motion WHERE eventId IN (:eventIds) AND eventId NOT IN (SELECT id FROM events)")
    suspend fun deleteMotionOf(eventIds: List<Long>)

    @Query("DELETE FROM connection_events WHERE eventId IN (:eventIds) AND eventId NOT IN (SELECT id FROM events)")
    suspend fun deleteConnectionEventsOf(eventIds: List<Long>)
}
