package cc.tumtum.app.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthDateTest {
    private val today = LocalDate.of(2026, 9, 26)

    @Test
    fun `eighteen today is an adult`() {
        assertTrue(BirthDate.isAdult(LocalDate.of(2008, 9, 26), today))
    }

    @Test
    fun `eighteen tomorrow is not yet`() {
        assertFalse(BirthDate.isAdult(LocalDate.of(2008, 9, 27), today))
        assertEquals(17, BirthDate.age(LocalDate.of(2008, 9, 27), today))
    }

    @Test
    fun `a leap-day birthday turns eighteen on the first of March`() {
        val birth = LocalDate.of(2008, 2, 29)
        assertFalse(BirthDate.isAdult(birth, LocalDate.of(2026, 2, 28)))
        assertTrue(BirthDate.isAdult(birth, LocalDate.of(2026, 3, 1)))
    }

    @Test
    fun `a date in the future is nobody's birth`() {
        assertFalse(BirthDate.isAdult(LocalDate.of(2030, 1, 1), today))
    }

    @Test
    fun `the wheel opens on an adult's year`() {
        assertTrue(BirthDate.isAdult(BirthDate.wheelStart(today), today))
    }
}
