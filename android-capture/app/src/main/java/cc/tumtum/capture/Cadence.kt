package cc.tumtum.capture

/**
 * What a watch actually recorded, measured rather than assumed.
 *
 * The whole Health Connect path lives or dies on sampling density
 * (docs/health-connect-plan.md, section 2): the detector was calibrated
 * against ~1 reading per second, and what a watch writes depends on a
 * setting its owner chose and on what its vendor app passes along. So the
 * import screen never promises moments from a count alone — it measures the
 * cadence and says what this data can honestly support.
 *
 * Median gap and slot coverage together, because either alone can lie: a
 * night of dense clusters separated by dead half-hours has a lovely median,
 * and a metronomic reading every 9 seconds covers many slots while starving
 * the detector's 5-second smoothing window.
 */
data class Cadence(
    val count: Int,
    val spanSeconds: Long,
    val medianGapMillis: Long,
    /** Fraction (0..1) of 5-second slots across the span holding a reading. */
    val coverage: Double,
) {
    /**
     * Dense enough for peak detection: the plan's gate of at least one
     * reading every 5 s, sustained. "Sustained" is the coverage clause.
     */
    val denseEnough: Boolean
        get() = count >= 2 && medianGapMillis <= 5_000 && coverage >= 0.8

    /** `4h32`-style span for the report line. */
    val spanLabel: String
        get() {
            val hours = spanSeconds / 3600
            val minutes = (spanSeconds % 3600) / 60
            return if (hours > 0) "${hours}h${minutes.toString().padStart(2, '0')}" else "$minutes min"
        }

    companion object {
        private const val SLOT_MILLIS = 5_000L

        /** Null when there is nothing to measure — no readings is a fact, not a cadence. */
        fun of(timesMillis: List<Long>): Cadence? {
            if (timesMillis.isEmpty()) return null
            val sorted = timesMillis.sorted()
            val spanMillis = sorted.last() - sorted.first()
            if (sorted.size == 1) {
                return Cadence(count = 1, spanSeconds = 0, medianGapMillis = 0, coverage = 0.0)
            }

            val gaps = sorted.zipWithNext { a, b -> b - a }.sorted()
            val median = gaps[gaps.size / 2]

            val slots = spanMillis / SLOT_MILLIS + 1
            val covered = sorted.mapTo(HashSet()) { (it - sorted.first()) / SLOT_MILLIS }.size
            return Cadence(
                count = sorted.size,
                spanSeconds = spanMillis / 1000,
                medianGapMillis = median,
                coverage = covered.toDouble() / slots,
            )
        }
    }
}
