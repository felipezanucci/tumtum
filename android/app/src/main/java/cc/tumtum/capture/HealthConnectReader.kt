package cc.tumtum.capture

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

/**
 * The one door to Health Connect, kept thin on purpose.
 *
 * Everything here is a question — is it installed, may we read, what did the
 * watch write between these two instants. The answers feed the import screen,
 * which is where honesty about them lives. Nothing is cached: availability
 * and permission can both change while the app is backgrounded (the person
 * installs the provider, or revokes us in settings), and a stale yes is the
 * kind of small lie this project hunts.
 *
 * One limit worth remembering (plan, Etapa 2): Health Connect only serves
 * data from up to 30 days before the day permission was granted. Irrelevant
 * for last night's show; real for somebody importing an old festival.
 */
object HealthConnectReader {

    /** Read heart rate, and nothing else. The permission list is the promise. */
    val PERMISSIONS: Set<String> =
        setOf(HealthPermission.getReadPermission(HeartRateRecord::class))

    /**
     * Three states, each owed its own sentence on screen (plan, task 2.1):
     * Android 14+ ships the provider built in; 13 and below need the app
     * from the Play Store; some devices cannot run it at all.
     */
    enum class Status { NOT_SUPPORTED, NEEDS_UPDATE, READY }

    fun status(context: Context): Status =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> Status.READY
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Status.NEEDS_UPDATE
            else -> Status.NOT_SUPPORTED
        }

    suspend fun hasPermission(context: Context): Boolean =
        HealthConnectClient.getOrCreate(context)
            .permissionController
            .getGrantedPermissions()
            .containsAll(PERMISSIONS)

    data class Reading(val timeMillis: Long, val bpm: Int)

    /**
     * Every heart-rate sample between the two instants, oldest first.
     *
     * A HeartRateRecord is a series, not a point — one record can carry a
     * whole workout of samples — so records are unpacked and each sample is
     * bounds-checked individually: records overlapping the window edges
     * carry samples outside it.
     */
    suspend fun readHeartRate(
        context: Context,
        startMillis: Long,
        endMillis: Long,
    ): List<Reading> {
        val client = HealthConnectClient.getOrCreate(context)
        val filter = TimeRangeFilter.between(
            Instant.ofEpochMilli(startMillis),
            Instant.ofEpochMilli(endMillis),
        )
        val readings = mutableListOf<Reading>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = filter,
                    pageSize = 1000,
                    pageToken = pageToken,
                )
            )
            for (record in response.records) {
                for (sample in record.samples) {
                    val at = sample.time.toEpochMilli()
                    if (at in startMillis..endMillis) {
                        readings += Reading(at, sample.beatsPerMinute.toInt())
                    }
                }
            }
            pageToken = response.pageToken
        } while (pageToken != null)
        return readings.sortedBy { it.timeMillis }
    }
}
