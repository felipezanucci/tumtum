package cc.tumtum.app.domain

import kotlin.math.roundToInt

/**
 * The card's title, built from facts the night itself proves — a port of the
 * backend's `moment_copy()` (backend/app/services/card_generator.py).
 *
 * Until 26/09 every card this app made said *"EU TAVA TRANQUILO. AÍ VEIO
 * ISSO."*, and a night with no moment said *"UMA NOITE TRANQUILA."*. Both
 * asserted a calm nobody measured: if the night ran high, the largest text on
 * the card was false — the project's oldest bug class printed on a public
 * object. The backend dropped the line for exactly that reason; the phone now
 * follows it, branch for branch:
 *
 * - a rise above the night's own average → *"78 A NOITE INTEIRA. / ATÉ 01H24."*
 * - no average, or no rise above it → *"SEU CORAÇÃO, / ÀS 01H24."*
 * - no moment at all → the night's own average, until the peak's hour:
 *   *"84 A NOITE INTEIRA. / ATÉ 16H48."* — the same line before the night is
 *   saved and after the server has looked at it (28/09). "SEM MOMENTOS NESSA
 *   NOITE." was the server's answer printed as a headline: the reveal changed
 *   title under the person's eyes, and the card said nothing about the night.
 *
 * The words live in strings.xml; this decides which sentence and with which
 * numbers, so it is tested on the JVM without a device.
 */
object CardCopy {

    sealed interface Title {
        /** "%d A NOITE INTEIRA." over "ATÉ %s." — or "ATÉ ESSE MOMENTO." without a time. */
        data class AboveAverage(val averageBpm: Int, val at: String?) : Title

        /** "SEU CORAÇÃO," over "ÀS %s." — or "NAQUELE MOMENTO." without a time. */
        data class HeartAt(val at: String?) : Title
    }

    /**
     * @param momentTime the moment's hour as the card prints it ("01h24"), or
     *   null when the person chose to hide the hour — then the title names no
     *   time either, or the switch would be a lie.
     */
    fun title(peakBpm: Int, averageBpm: Int?, momentTime: String?, hasMoments: Boolean = true): Title {
        val at = momentTime?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()
        // No moment: the night itself is the fact — its average, up to its top.
        if (!hasMoments) {
            return if (averageBpm != null && averageBpm > 0) Title.AboveAverage(averageBpm, at) else Title.HeartAt(at)
        }
        // "até" promises a rise; only when the data keeps it.
        if (averageBpm != null && averageBpm > 0 && peakBpm > averageBpm) {
            return Title.AboveAverage(averageBpm, at)
        }
        return Title.HeartAt(at)
    }

    /** The night's average, rounded — the reference the backend uses (hr_sessions.avg_bpm). */
    fun averageBpm(samples: List<HrSample>): Int? =
        if (samples.isEmpty()) null else samples.map { it.bpm }.average().roundToInt()

    /**
     * The number as the card prints it. Exact, or — when the person turned
     * "bpm exato" off — the ten below it with a plus: 116 → "110+". Down, not
     * to the nearest ten: "120+" over a heart that reached 116 would claim a
     * beat that never happened.
     */
    fun bpmLabel(bpm: Int, exact: Boolean): String =
        if (exact || bpm < 10) "$bpm" else "${(bpm / 10) * 10}+"
}
