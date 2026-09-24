package cc.tumtum.app.export

/**
 * How the network buttons sit, two to a row, **without a hole** (#61, 24/09).
 *
 * b187 put the networks in pairs and let an odd one keep half the width, so
 * with five apps on the phone WhatsApp stood alone beside an empty cell —
 * which reads as a button that failed to load. The rules, in order:
 *
 *  - two buttons share a row when both labels fit in half of it;
 *  - a label that does not fit in half takes the whole row, rather than
 *    wrapping onto two lines inside a 56 dp button;
 *  - a button left without a partner — the last of an odd count, or the one
 *    just before a full-width label — takes the whole row too.
 *
 * The order is never changed: the rows, read left to right and top to bottom,
 * are the networks in the order given. Pure, so it is tested without a device.
 *
 * @param fitsHalf for each button, whether its label fits in half a row.
 * @return the rows, each a list of one or two indices into [fitsHalf]. A row
 *   of one is drawn at full width.
 */
object ShareGrid {
    fun rows(fitsHalf: List<Boolean>): List<List<Int>> {
        val rows = mutableListOf<List<Int>>()
        var waiting: Int? = null
        fitsHalf.forEachIndexed { i, fits ->
            if (!fits) {
                waiting?.let { rows += listOf(it) }
                waiting = null
                rows += listOf(i)
                return@forEachIndexed
            }
            val partner = waiting
            if (partner == null) {
                waiting = i
            } else {
                rows += listOf(partner, i)
                waiting = null
            }
        }
        waiting?.let { rows += listOf(it) }
        return rows
    }
}
