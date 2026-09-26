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

    /** The events of deleted nights — only those no remaining night still points to. */
    @Query("DELETE FROM events WHERE id IN (:ids) AND id NOT IN (SELECT eventId FROM nights)")
    suspend fun deleteOrphans(ids: List<Long>)

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

    /** Events that still owe the server a mark, so a retry can find them without a night. */
    @Query("SELECT DISTINCT eventId FROM marks WHERE synced = 0")
    suspend fun eventsWithUnsynced(): List<Long>

    @Query("SELECT COUNT(*) FROM marks WHERE eventId = :eventId")
    fun countFor(eventId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM marks WHERE eventId = :eventId AND entryType = :entryType")
    suspend fun countOfKind(eventId: Long, entryType: String): Int

    /** The match's anchors, as tapped: the screen shows each one's clock on its button. */
    @Query("SELECT * FROM marks WHERE eventId = :eventId AND entryType IN ('kickoff', 'second_half') ORDER BY at")
    fun anchorsFor(eventId: Long): Flow<List<MarkEntity>>

    /** Undo: only a mark the server has not seen yet can go. Returns rows removed (0 or 1). */
    @Query("DELETE FROM marks WHERE id = :id AND synced = 0")
    suspend fun deleteUnsynced(id: Long): Int

    @Query("DELETE FROM marks")
    suspend fun deleteAll()

    @Query("DELETE FROM marks WHERE eventId IN (:eventIds) AND eventId NOT IN (SELECT id FROM events)")
    suspend fun deleteOrphans(eventIds: List<Long>)
}

@Dao
interface NightDao {
    @Insert
    suspend fun insert(night: NightEntity): Long

    @Insert
    suspend fun insertSamples(samples: List<SampleEntity>)

    @Insert
    suspend fun insertMoments(moments: List<MomentEntity>)

    // A night belongs to the account that recorded it (25/09). Every list the
    // person sees is filtered by [viewer], the account whose nights this phone
    // shows; a night with no owner predates the rule and is shown to whoever
    // is here. Another account's nights are hidden, never deleted.

    @Transaction
    @Query("SELECT * FROM nights WHERE ownerUserId IS NULL OR ownerUserId = :viewer ORDER BY startAt DESC")
    fun nightsWithData(viewer: String?): Flow<List<NightWithData>>

    @Transaction
    @Query("SELECT * FROM nights WHERE id = :id")
    fun nightWithData(id: Long): Flow<NightWithData?>

    @Query(
        "SELECT * FROM nights WHERE published = 1 AND (ownerUserId IS NULL OR ownerUserId = :viewer) " +
            "ORDER BY startAt DESC",
    )
    fun published(viewer: String?): Flow<List<NightEntity>>

    @Query("SELECT * FROM nights WHERE ownerUserId IS NULL OR ownerUserId = :viewer ORDER BY startAt DESC")
    fun allNights(viewer: String?): Flow<List<NightEntity>>

    /** Nights on this phone that belong to another account — said, so an empty list is not a lie. */
    @Query("SELECT COUNT(*) FROM nights WHERE ownerUserId IS NOT NULL AND (:viewer IS NULL OR ownerUserId != :viewer)")
    fun hiddenCount(viewer: String?): Flow<Int>

    /** The nights an account deletion takes from this phone: the account's own, and the ownerless ones it was shown. */
    @Query("SELECT * FROM nights WHERE ownerUserId IS NULL OR ownerUserId = :viewer")
    suspend fun nightsOf(viewer: String?): List<NightEntity>

    /**
     * This phone's night at a server event that **never reached the server**
     * (25/09), for the feed that would otherwise say "your night did not
     * arrive" about a night sitting right here.
     */
    @Query(
        "SELECT nights.* FROM nights JOIN events ON events.id = nights.eventId " +
            "WHERE events.serverEventId = :serverEventId AND nights.serverSessionId IS NULL " +
            "AND (nights.ownerUserId IS NULL OR nights.ownerUserId = :viewer) " +
            "ORDER BY nights.startAt DESC LIMIT 1",
    )
    suspend fun unsentNightAt(serverEventId: String, viewer: String?): NightEntity?

    /** The card went out: the night keeps its skin and, on the black one, the photo behind it. */
    @Query("UPDATE nights SET skin = :skin, published = 1, photoPath = :photoPath WHERE id = :id")
    suspend fun publish(id: Long, skin: String, photoPath: String?)

    // --- The server's side of a night (Etapa 2) ---

    @Query("SELECT * FROM nights WHERE id = :id")
    suspend fun nightRow(id: Long): NightEntity?

    /**
     * This phone's night at a server event, if it reached the server — the
     * one a post can be made from. The empty feed uses it to offer that night
     * rather than invite an act it gives no way to perform (22/09).
     */
    @Query(
        "SELECT nights.* FROM nights JOIN events ON events.id = nights.eventId " +
            "WHERE events.serverEventId = :serverEventId AND nights.serverSessionId IS NOT NULL " +
            // Only the signed-in account's night (#58). A night from before the
            // owner was recorded is still offered; the server has the last word.
            "AND (nights.ownerUserId IS NULL OR nights.ownerUserId = :ownerUserId) " +
            "ORDER BY nights.startAt DESC LIMIT 1",
    )
    suspend fun uploadedNightAt(serverEventId: String, ownerUserId: String?): NightEntity?

    @Query("SELECT * FROM samples WHERE nightId = :nightId ORDER BY time")
    suspend fun samplesOf(nightId: Long): List<SampleEntity>

    /** Nights the person asked to keep that are not done yet — the only ones a retry touches (26/09). */
    @Query("SELECT * FROM nights WHERE sendRequested = 1 AND uploadState != 'ANALYSED' ORDER BY startAt DESC")
    suspend fun pendingUpload(): List<NightEntity>

    /** The tap on "Guardar minha noite na TumTum" — or its withdrawal when a consent is missing. */
    @Query("UPDATE nights SET sendRequested = :requested WHERE id = :id")
    suspend fun setSendRequested(id: Long, requested: Boolean)

    /** Nights still sealed by the reveal lock — their reminders are set again after a reboot. */
    @Query("SELECT * FROM nights WHERE revealAt IS NOT NULL AND revealAt > :now")
    suspend fun lockedAfter(now: Long): List<NightEntity>

    @Query("SELECT * FROM moments WHERE nightId = :nightId")
    suspend fun momentsOf(nightId: Long): List<MomentEntity>

    @Query("UPDATE nights SET serverSessionId = :serverSessionId, ownerUserId = :ownerUserId, sentAt = :sentAt WHERE id = :id")
    suspend fun setServerSessionId(id: Long, serverSessionId: String, ownerUserId: String?, sentAt: Long)

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

    @Query("DELETE FROM nights WHERE id IN (:ids)")
    suspend fun deleteNights(ids: List<Long>)

    @Query("DELETE FROM samples WHERE nightId IN (:ids)")
    suspend fun deleteSamplesOfNights(ids: List<Long>)

    @Query("DELETE FROM moments WHERE nightId IN (:ids)")
    suspend fun deleteMomentsOfNights(ids: List<Long>)

    @Query("DELETE FROM samples")
    suspend fun deleteAllSamples()

    @Query("DELETE FROM moments")
    suspend fun deleteAllMoments()
}
