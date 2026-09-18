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

    @Query("UPDATE nights SET skin = :skin, published = 1 WHERE id = :id")
    suspend fun publish(id: Long, skin: String)

    @Query("DELETE FROM nights")
    suspend fun deleteAll()

    @Query("DELETE FROM samples")
    suspend fun deleteAllSamples()

    @Query("DELETE FROM moments")
    suspend fun deleteAllMoments()
}
