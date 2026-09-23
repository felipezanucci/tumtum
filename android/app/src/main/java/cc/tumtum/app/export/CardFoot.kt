package cc.tumtum.app.export

/**
 * The foot of the card (version A2, 22/09): where the event's name goes and
 * how much of it fits. Pure, shared by the renderer and the on-screen
 * preview so the two cannot disagree about the layout.
 */
object CardFoot {
    /**
     * Names longer than this take a row of their own above "bpm às 22h12"
     * and the wordmark. The approved mockup had "ENSAIO3"; a real event is
     * often "SÃO PAULO × VITÓRIA", which beside the rest of the line would be
     * cut to four letters.
     */
    const val MAX_INLINE_CHARS = 9

    fun ownRow(event: String): Boolean = event.trim().length > MAX_INLINE_CHARS

    /**
     * [text] as it fits in [maxWidth], cut with an ellipsis when it does not.
     * Never empty when [text] is not: a cut name still says where.
     */
    fun fit(text: String, maxWidth: Float, measure: (String) -> Float): String {
        if (measure(text) <= maxWidth) return text
        var end = text.length
        while (end > 1 && measure(text.substring(0, end).trimEnd() + "…") > maxWidth) end--
        return text.substring(0, end).trimEnd() + "…"
    }

    /**
     * [text] in at most [maxLines] lines of [maxWidth], broken between words,
     * the last line cut with an ellipsis only if the name is longer still
     * (#61, 23/09). "TESTE - MADONNA - CONFES…" on one row read as broken; a
     * name gets two lines before it loses anything.
     */
    fun wrap(text: String, maxWidth: Float, maxLines: Int = 2, measure: (String) -> Float): List<String> {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return emptyList()
        val lines = mutableListOf<String>()
        var current = ""
        var i = 0
        while (i < words.size) {
            val candidate = if (current.isEmpty()) words[i] else "$current ${words[i]}"
            if (measure(candidate) <= maxWidth || current.isEmpty()) {
                current = candidate
                i++
            } else {
                lines += current
                current = ""
                if (lines.size == maxLines - 1) break
            }
        }
        // Whatever is left belongs to the last line, cut to fit if it must be.
        val rest = (listOf(current) + words.drop(i)).filter { it.isNotEmpty() }.joinToString(" ")
        if (rest.isNotEmpty()) lines += fit(rest, maxWidth, measure)
        return lines.map { fit(it, maxWidth, measure) }
    }
}
