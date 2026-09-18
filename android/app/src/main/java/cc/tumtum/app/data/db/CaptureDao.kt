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
}
