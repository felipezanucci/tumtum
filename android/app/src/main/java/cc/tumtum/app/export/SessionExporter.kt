package cc.tumtum.app.export

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.domain.DeviceName
import cc.tumtum.app.domain.HrSource
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * §9 — a extração é manual, executada às 2h da manhã por um operador cansado.
 * Gera um ZIP com samples/rr/motion/connection_events em CSV + session.json,
 * compartilhado via share sheet. Nada de rede: o app não tem INTERNET.
 */
class SessionExporter(
    private val context: Context,
    private val db: TumTumDatabase,
    private val prefs: UserPrefs,
) {

    suspend fun exportNight(nightId: Long): File = withContext(Dispatchers.IO) {
        val night = db.nightDao().nightWithData(nightId).first()
            ?: error("noite $nightId não existe")
        val n = night.night
        val eventId = n.eventId
        val user = prefs.state.first()

        val bleSamples = db.captureDao().samplesForEvent(eventId)
        val rr = db.captureDao().rrForEvent(eventId)
        val motion = db.captureDao().motionForEvent(eventId)
        val connEvents = db.captureDao().connectionEventsForEvent(eventId)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val participant = user.participantId ?: "P00"
        // The file name says which night, and nothing about whose (26/09): a
        // ZIP forwarded or left in a Downloads folder should not name anyone.
        val zipFile = File(dir, "tumtum-night$nightId.zip")

        // Every figure in session.json is counted from the rows actually
        // written (28/09, item 32). The export of 27/09 said "0 samples" and
        // "35% coverage" over 122 beats in 250 s: it read the counts off the
        // raw tables, which had been emptied when the night was saved, and
        // the coverage off the night's row, measured another way.
        var sampleRows = 0
        var bleRows = 0
        var rrRows = 0
        var motionRows = 0
        var connectionRows = 0
        ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
            zip.writeEntry("samples.csv") { sb ->
                sb.appendLine("nightId,wallClockMs,elapsedRealtimeMs,bpm,sourceId,contactStatus")
                bleSamples.forEach {
                    sb.appendLine("$nightId,${it.wallClockMs},${it.elapsedRealtimeMs},${it.bpm},${HrSource.ID_BLE},${it.contactStatus}")
                    sampleRows++
                    bleRows++
                }
                // The raw BLE rows go once a night is saved (26/09); then, as for
                // a Health Connect night, the night's own beats are what there is.
                if (n.sourcePackage != HrSource.ID_BLE || bleSamples.isEmpty()) {
                    // A fonte escolhida foi Health Connect: amostras da noite, sem carimbo monotônico.
                    night.samples.forEach {
                        sb.appendLine("$nightId,${it.time},,${it.bpm},${csv(n.sourcePackage)},")
                        sampleRows++
                        if (n.sourcePackage == HrSource.ID_BLE) bleRows++
                    }
                }
            }
            // R-R and motion exist until the night is saved (26/09): an export
            // made during the capture carries them; one made after, the headers.
            zip.writeEntry("rr.csv") { sb ->
                sb.appendLine("nightId,wallClockMs,elapsedRealtimeMs,rrMs")
                rr.forEach {
                    sb.appendLine("$nightId,${it.wallClockMs},${it.elapsedRealtimeMs},${it.rrMs}")
                    rrRows++
                }
            }
            zip.writeEntry("motion.csv") { sb ->
                sb.appendLine("nightId,wallClockMs,elapsedRealtimeMs,magMean,magStd")
                motion.forEach {
                    sb.appendLine("$nightId,${it.wallClockMs},${it.elapsedRealtimeMs},${it.magMean},${it.magStd}")
                    motionRows++
                }
            }
            // Kept with the night since 28/09: every connect, drop, reconnect
            // attempt, silence and return, with the wall clock and the signal.
            zip.writeEntry("connection_events.csv") { sb ->
                sb.appendLine("nightId,wallClockMs,elapsedRealtimeMs,type,detail,rssi")
                connEvents.forEach {
                    val detail = ExportCounts.withoutSerial(it.detail)
                    sb.appendLine("$nightId,${it.wallClockMs},${it.elapsedRealtimeMs},${csv(it.type)},${csv(detail)},${it.rssi ?: ""}")
                    connectionRows++
                }
            }
            val beatTimes = night.samples.map { it.time }
            zip.writeEntry("session.json") { sb ->
                sb.append(
                    JSONObject()
                        .put("participantId", participant)
                        .put("nightId", nightId)
                        .put("eventId", eventId)
                        .put("eventName", n.eventName)
                        .put("venue", n.venue)
                        .put("startMs", n.startAt)
                        .put("endMs", n.endAt)
                        .put("clockOffsetStartMs", n.clockOffsetStartMs ?: JSONObject.NULL)
                        .put("clockOffsetEndMs", n.clockOffsetEndMs ?: JSONObject.NULL)
                        // Seconds with at least one beat over the seconds of the night.
                        .put("coveragePct", ExportCounts.coveragePct(beatTimes, n.startAt, n.endAt))
                        .put("chosenSource", n.sourcePackage)
                        .put("sampleCount", sampleRows)
                        .put("bleSampleCount", bleRows)
                        .put("rawBleSamples", bleSamples.isNotEmpty())
                        .put("rrCount", rrRows)
                        .put("motionCount", motionRows)
                        .put("connectionEventCount", connectionRows)
                        // The model, never the strap's own serial (28/09): this file leaves the phone.
                        .put("sensorName", DeviceName.model(user.bleName) ?: JSONObject.NULL)
                        .put("deviceManufacturer", Build.MANUFACTURER)
                        .put("deviceModel", Build.MODEL)
                        .put("androidRelease", Build.VERSION.RELEASE)
                        .put("androidSdk", Build.VERSION.SDK_INT)
                        .put("appVersion", appVersion())
                        .toString(2),
                )
            }
        }
        zipFile
    }

    fun shareIntent(zip: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zip)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .setType("application/zip")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            zip.name,
        )
    }

    private fun appVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

    /** Campo CSV seguro: vírgula/aspas/linha nova viram campo entre aspas. */
    private fun csv(raw: String): String =
        if (raw.any { it == ',' || it == '"' || it == '\n' }) "\"${raw.replace("\"", "\"\"")}\"" else raw

    private inline fun ZipOutputStream.writeEntry(name: String, block: (StringBuilder) -> Unit) {
        putNextEntry(ZipEntry(name))
        val sb = StringBuilder()
        block(sb)
        write(sb.toString().toByteArray(Charsets.UTF_8))
        closeEntry()
    }
}
