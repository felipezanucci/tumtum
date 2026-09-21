package cc.tumtum.app.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class NightWithData(
    @Embedded val night: NightEntity,
    @Relation(parentColumn = "id", entityColumn = "nightId")
    val samples: List<SampleEntity>,
    @Relation(parentColumn = "id", entityColumn = "nightId")
    val moments: List<MomentEntity>,
)

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity): Long

    @Query("UPDATE events SET endAt = :endAt WHERE id = :id")
    suspend fun close(id: Long, endAt: Long)

    @Query("SELECT * FROM events WHERE endAt IS NULL ORDER BY startAt DESC LIMIT 1")
    fun active(): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun byId(id: Long): EventEntity?

    /** Offset wallClock − elapsedRealtime no início (§8): só o primeiro registro vale. */
    @Query("UPDATE events SET clockOffsetStartMs = :offsetMs WHERE id = :id AND clockOffsetStartMs IS NULL")
    suspend fun setClockOffsetStart(id: Long, offsetMs: Long)

    @Query("UPDATE events SET clockOffsetEndMs = :offsetMs WHERE id = :id")
    suspend fun setClockOffsetEnd(id: Long, offsetMs: Long)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("UPDATE events SET serverEventId = :serverEventId WHERE id = :id")
    suspend fun setServerEventId(id: Long, serverEventId: String)
}

@Dao
interface MarkDao {
    @Insert
    suspend fun insert(mark: MarkEntity): Long

    @Query("SELECT * FROM marks WHERE eventId = :eventId AND synced = 0 ORDER BY at")
    suspend fun unsyncedFor(eventId: Long): List<MarkEntity>

    @Query("UPDATE marks SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT COUNT(*) FROM marks WHERE eventId = :eventId")
    fun countFor(eventId: Long): Flow<Int>

    /** Undo: only a mark the server has not seen yet can go. Returns rows removed (0 or 1). */
    @Query("DELETE FROM marks WHERE id = :id AND synced = 0")
    suspend fun deleteUnsynced(id: Long): Int

    @Query("DELETE FROM marks")
    suspend fun deleteAll()
}

@Dao
interface NightDao {
    @Insert
    suspend fun insert(night: NightEntity): Long

    @Insert
    suspend fun insertSamples(samples: List<SampleEntity>)

    @Insert
    suspend fun insertMoments(moments: List<MomentEntity>)

    @Transaction
    @Query("SELECT * FROM nights ORDER BY startAt DESC")
    fun nightsWithData(): Flow<List<NightWithData>>

    @Transaction
    @Query("SELECT * FROM nights WHERE id = :id")
    fun nightWithData(id: Long): Flow<NightWithData?>

    @Query("SELECT * FROM nights WHERE published = 1 ORDER BY startAt DESC")
    fun published(): Flow<List<NightEntity>>

    @Query("SELECT * FROM nights ORDER BY startAt DESC")
    fun allNights(): Flow<List<NightEntity>>

    /** The card went out: the night keeps its skin and, on the black one, the photo behind it. */
    @Query("UPDATE nights SET skin = :skin, published = 1, photoPath = :photoPath WHERE id = :id")
    suspend fun publish(id: Long, skin: String, photoPath: String?)

    // --- The server's side of a night (Etapa 2) ---

    @Query("SELECT * FROM nights WHERE id = :id")
    suspend fun nightRow(id: Long): NightEntity?

    @Query("SELECT * FROM samples WHERE nightId = :nightId ORDER BY time")
    suspend fun samplesOf(nightId: Long): List<SampleEntity>

    @Query("SELECT * FROM nights WHERE uploadState != 'ANALYSED' ORDER BY startAt DESC")
    suspend fun pendingUpload(): List<NightEntity>

    /** Nights still sealed by the reveal lock — their reminders are set again after a reboot. */
    @Query("SELECT * FROM nights WHERE revealAt IS NOT NULL AND revealAt > :now")
    suspend fun lockedAfter(now: Long): List<NightEntity>

    @Query("SELECT * FROM moments WHERE nightId = :nightId")
    suspend fun momentsOf(nightId: Long): List<MomentEntity>

    @Query("UPDATE moments SET label = :label WHERE id = :id")
    suspend fun setMomentLabel(id: Long, label: String?)

    @Query("UPDATE nights SET serverSessionId = :serverSessionId WHERE id = :id")
    suspend fun setServerSessionId(id: Long, serverSessionId: String)

    @Query("UPDATE nights SET uploadState = :state, uploadError = :error WHERE id = :id")
    suspend fun setUploadState(id: Long, state: String, error: String?)

    @Query("DELETE FROM moments WHERE nightId = :nightId")
    suspend fun deleteMomentsOf(nightId: Long)

    @Query("UPDATE nights SET momentsSource = :source, momentCount = :count WHERE id = :id")
    suspend fun setMomentsSource(id: Long, source: String, count: Int)

    /** The server's moments replace the phone's, in one transaction, and the night says so. */
    @Transaction
    suspend fun replaceMoments(nightId: Long, moments: List<MomentEntity>) {
        deleteMomentsOf(nightId)
        insertMoments(moments)
        setMomentsSource(nightId, "SERVER", moments.size)
    }

    @Query("DELETE FROM nights")
    suspend fun deleteAll()

    @Query("DELETE FROM samples")
    suspend fun deleteAllSamples()

    @Query("DELETE FROM moments")
    suspend fun deleteAllMoments()
}
