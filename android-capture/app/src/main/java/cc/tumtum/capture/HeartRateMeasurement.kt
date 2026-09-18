package cc.tumtum.capture

/**
 * One notification from the standard Heart Rate Measurement characteristic
 * (0x2A37), decoded.
 */
data class HeartRateMeasurement(
    val bpm: Int,
    /** Every R-R interval carried by this notification, in milliseconds. */
    val rrIntervalsMs: List<Int>,
    /** true/false when the sensor reports contact, null when it does not. */
    val contact: Boolean?,
)

/**
 * Decode a Heart Rate Measurement notification.
 *
 * A direct port of the TypeScript the web app has been capturing with — the
 * one that recorded 1,504 readings in 1,504 seconds on a real phone at a real
 * sensor. The layout is fixed by the Bluetooth SIG and every field after the
 * flags byte is optional, so the offset has to be walked rather than assumed.
 *
 * Returns null for anything that is not a usable reading, which the caller
 * drops: a notification arriving mid-reconnect is normal and not worth
 * recording.
 */
object HeartRateParser {

    /** Physiological bounds — the same the backend enforces on ingestion. */
    private const val MIN_BPM = 30
    private const val MAX_BPM = 250

    /** The backend stores one R-R per point and constrains it to this range. */
    private const val MIN_RR_MS = 200
    private const val MAX_RR_MS = 2000

    fun parse(data: ByteArray): HeartRateMeasurement? {
        if (data.size < 2) return null

        val flags = data[0].toInt() and 0xFF
        val is16Bit = flags and 0x01 != 0
        val contactSupported = flags and 0x04 != 0
        val contactDetected = flags and 0x02 != 0
        val hasEnergy = flags and 0x08 != 0
        val hasRr = flags and 0x10 != 0

        var offset = 1
        val bpm: Int
        if (is16Bit) {
            if (data.size < offset + 2) return null
            bpm = readUint16(data, offset)
            offset += 2
        } else {
            bpm = data[offset].toInt() and 0xFF
            offset += 1
        }

        // Energy expended is two bytes we do not use, but they sit between the
        // rate and the intervals, so they still have to be stepped over.
        if (hasEnergy) offset += 2

        val intervals = mutableListOf<Int>()
        if (hasRr) {
            while (offset + 1 < data.size) {
                // Reported in 1/1024 of a second, not milliseconds.
                val rounded = Math.round(readUint16(data, offset) * 1000.0 / 1024.0).toInt()
                if (rounded in MIN_RR_MS..MAX_RR_MS) intervals.add(rounded)
                offset += 2
            }
        }

        if (bpm < MIN_BPM || bpm > MAX_BPM) return null

        return HeartRateMeasurement(
            bpm = bpm,
            rrIntervalsMs = intervals,
            contact = if (contactSupported) contactDetected else null,
        )
    }

    /** Little-endian, as the specification requires. */
    private fun readUint16(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
}
