package cc.tumtum.app.export

import cc.tumtum.app.domain.DeviceName

/**
 * The numbers an export's session.json states about itself (28/09, item 32),
 * pure so a test holds them to the rows the file carries.
 */
object ExportCounts {

    /**
     * Seconds of the night with at least one beat in them, over the seconds
     * the night lasted — the question the coverage answers: of the time
     * elapsed, how much has a heart in it. 122 beats in 250 s with a
     * two-minute hole is about half, never the 35% the old figure printed.
     */
    fun coveragePct(beatTimesMs: List<Long>, startMs: Long, endMs: Long): Int {
        val elapsedSec = (endMs - startMs + 999) / 1000
        if (elapsedSec <= 0) return 0
        val covered = beatTimesMs
            .filter { it in startMs..endMs }
            .map { (it - startMs) / 1000 }
            .distinct()
            .size
        return ((covered * 100L) / elapsedSec).toInt().coerceIn(0, 100)
    }

    /**
     * A connection log line as it may leave the phone: "device=Polar H10
     * 19B38E3F" loses the serial, as every other place a sensor's name goes.
     */
    fun withoutSerial(detail: String): String {
        val key = "device="
        val at = detail.indexOf(key)
        if (at < 0) return detail
        val name = detail.substring(at + key.length)
        return detail.substring(0, at + key.length) + (DeviceName.model(name) ?: "")
    }
}
