package cc.tumtum.app.domain

import cc.tumtum.app.data.ble.SkinContact

/** One reading as the sensor sent it, before anyone decides it was a beat. */
data class RawReading(val timeMs: Long, val bpm: Int, val contactStatus: Int?, val hasRr: Boolean)

/**
 * Which readings are beats (25/09, measured). b198 filtered on the skin
 * contact flag and failed in a hand: a Polar H10 never sets it (it reports
 * "not supported" in every packet, worn or not). What it does instead, read
 * from night 18's export, second by second, after the strap came off:
 *
 *  - it kept sending the last value (71) for about nine seconds, with no R-R;
 *  - then it sent 0, for seventeen seconds, with no R-R;
 *  - then it disconnected on its own.
 *
 * Worn, every reading carried at least one R-R interval — the time between two
 * real beats. So the R-R is the proof of skin: from a sensor that sends it, a
 * run of [NO_RR_RUN] or more readings without one is a sensor repeating
 * itself, not a heart. A single reading without R-R stays: at 40 bpm a beat
 * lands every second and a half, so a one-second packet sometimes carries none,
 * but three in a row would need a heart under 20.
 *
 * A sensor that never sends R-R cannot be checked this way and is believed, as
 * long as the number is one a heart can have. A sensor that does flag "no
 * contact" is believed too. Nothing here deletes anything: the raw rows stay
 * in the database and in the export; only the reading of them changes.
 */
object BeatFilter {
    /** The server's accepted range, and the range of a human heart at a show. */
    const val MIN_BPM = 30
    const val MAX_BPM = 250

    /** Consecutive readings without R-R, from a sensor that sends it, that mean "off the skin". */
    const val NO_RR_RUN = 3

    /** In-range readings, from a sensor that has never sent R-R, before the setup screen believes it. */
    const val NO_RR_PROOF = 20

    fun plausible(bpm: Int): Boolean = bpm in MIN_BPM..MAX_BPM

    /** The readings that were beats, in time order. */
    fun beats(readings: List<RawReading>): List<RawReading> {
        val sorted = readings.sortedBy { it.timeMs }
        val rrSensor = sorted.any { it.hasRr }
        val repeated = BooleanArray(sorted.size)
        if (rrSensor) {
            var i = 0
            while (i < sorted.size) {
                if (sorted[i].hasRr) {
                    i++
                    continue
                }
                var j = i
                while (j < sorted.size && !sorted[j].hasRr) j++
                if (j - i >= NO_RR_RUN) for (k in i until j) repeated[k] = true
                i = j
            }
        }
        return sorted.filterIndexed { i, r ->
            !repeated[i] && plausible(r.bpm) && SkinContact.counts(r.contactStatus)
        }
    }
}

/**
 * [BeatFilter] one reading at a time, for the screens that answer while the
 * sensor is still talking: the capture's number and the setup's "Pronto". It
 * cannot take back the first readings of a run the way the night's filter
 * does, so the number lingers two seconds before it turns into "—". Not
 * thread-safe: feed it from one thread.
 */
class OnSkinTracker {
    private var sawRr = false
    private var noRrRun = 0
    private var inRangeWithoutRr = 0

    /** True when this reading is a beat. */
    fun accept(bpm: Int, contactStatus: Int?, hasRr: Boolean): Boolean {
        if (hasRr) {
            sawRr = true
            noRrRun = 0
        } else {
            noRrRun++
        }
        val beat = SkinContact.counts(contactStatus) &&
            BeatFilter.plausible(bpm) &&
            !(sawRr && noRrRun >= BeatFilter.NO_RR_RUN)
        if (beat && !sawRr) inRangeWithoutRr++
        return beat
    }

    /**
     * Whether the sensor has shown it is on a person: one beat with an R-R, or
     * a long run of plausible numbers from a sensor that never sends R-R.
     */
    val proven: Boolean get() = sawRr || inRangeWithoutRr >= BeatFilter.NO_RR_PROOF
}
