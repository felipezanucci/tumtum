package cc.tumtum.capture

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/**
 * Timestamps come back from the backend as ISO 8601 and have to become millis.
 *
 * Read with java.time rather than SimpleDateFormat because the fractional
 * second is not a fixed width: Postgres hands back microseconds, this app
 * writes milliseconds, and a pattern pinned to `SSS` mis-reads everything that
 * is not exactly three digits — quietly, by a factor of a thousand.
 *
 * The offset is not guaranteed either. A `timestamptz` column comes back
 * carrying one, but a value that lost its timezone somewhere upstream would
 * fail an offset-only parse. That case is read as UTC instead of discarded,
 * because UTC is what a naive timestamp means everywhere in this project.
 */
object Iso8601 {

    /** Millis since the epoch, or null when the text is not a timestamp at all. */
    fun toMillis(text: String?): Long? {
        val trimmed = text?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        return try {
            OffsetDateTime.parse(trimmed).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            try {
                LocalDateTime.parse(trimmed).toInstant(ZoneOffset.UTC).toEpochMilli()
            } catch (e2: DateTimeParseException) {
                null
            }
        }
    }
}
