package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant

/**
 * The label under the curve for the time nobody measured (02/10). "1 MIN SEM
 * DADO" over a 93-second dropout said less than happened: the minutes were
 * rounded down and the seconds lost. The label says the whole time, over
 * every gap of the night, and sits under the biggest one.
 */
object GapLabel {
    /** How the seconds are said: whole minutes, whole seconds, or both. */
    sealed interface Words {
        data class Minutes(val minutes: Int) : Words
        data class Seconds(val seconds: Int) : Words
        data class MinutesSeconds(val minutes: Int, val seconds: Int) : Words
    }

    fun totalSeconds(gaps: List<Gap>): Long =
        gaps.sumOf { Duration.between(it.start, it.end).seconds.coerceAtLeast(0) }

    fun words(seconds: Long): Words {
        val m = (seconds / 60).toInt()
        val s = (seconds % 60).toInt()
        return when {
            m == 0 -> Words.Seconds(s)
            s == 0 -> Words.Minutes(m)
            else -> Words.MinutesSeconds(m, s)
        }
    }

    /**
     * Where the label goes, as the fraction of the axis under the middle of
     * the biggest gap — null when there is no gap worth a word (under a
     * second in all).
     */
    fun anchor(gaps: List<Gap>, windowStart: Instant, windowEnd: Instant): Float? {
        val biggest = gaps.maxByOrNull { Duration.between(it.start, it.end) } ?: return null
        val total = Duration.between(windowStart, windowEnd).toMillis().coerceAtLeast(1)
        val mid = Duration.between(windowStart, biggest.start).toMillis() +
            Duration.between(biggest.start, biggest.end).toMillis() / 2
        return (mid.toFloat() / total).coerceIn(0f, 1f)
    }
}
