package cc.tumtum.app.domain

import java.time.LocalDate
import java.time.Period

/**
 * TumTum is for adults (LGPD remediation, 26/09): the legal opinion treats
 * heart-rate data of a minor as a separate, heavier regime, and the product
 * has none of what that would need. The server is the authority and refuses
 * under 18 with its own sentence; this is the same arithmetic on the phone,
 * so the screen can say it before a round trip.
 */
object BirthDate {
    const val MINIMUM_AGE = 18

    /** Whole years lived by [today]. A birthday today counts. */
    fun age(birth: LocalDate, today: LocalDate): Int = Period.between(birth, today).years

    fun isAdult(birth: LocalDate, today: LocalDate): Boolean =
        !birth.isAfter(today) && age(birth, today) >= MINIMUM_AGE

    /** Where the wheel opens when nothing was chosen yet: a plausible adult, never a real guess. */
    fun wheelStart(today: LocalDate): LocalDate = LocalDate.of(today.year - 25, 1, 1)
}
