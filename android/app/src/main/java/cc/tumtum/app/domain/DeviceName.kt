package cc.tumtum.app.domain

/**
 * A sensor's name as it may leave the phone: the model, never the unit
 * (28/09). A Polar strap advertises itself as "Polar H10 19B38E3F" — the last
 * word is that strap's own ID, printed on it and unique to it. The Play Data
 * Safety form says no device ID is collected, and the night's
 * `source_device` and the operator's export both carried it. The screens on
 * the phone may keep the whole name; anything sent or shared gets this.
 */
object DeviceName {

    /** A word that is a unit's ID rather than a model: 4+ hex characters, one of them a digit. */
    private val UNIT_ID = Regex("^[0-9A-Fa-f]{4,}$")

    /** "Polar H10 19B38E3F" → "Polar H10"; "Polar H10" stays; blank or null → null. */
    fun model(name: String?): String? {
        val words = name?.trim()?.split(Regex("[\\s:]+"))?.filter { it.isNotEmpty() }.orEmpty().toMutableList()
        while (words.size > 1 && isUnitId(words.last())) words.removeAt(words.size - 1)
        return words.joinToString(" ").ifBlank { null }
    }

    private fun isUnitId(word: String): Boolean {
        val bare = word.trim(':', '-', '_', '(', ')', '#')
        return UNIT_ID.matches(bare) && bare.any { it.isDigit() }
    }
}
