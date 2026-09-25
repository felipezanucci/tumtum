package cc.tumtum.app.data.ble

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A Polar H10 on a table sent heart rates (25/09). A reading the sensor marks
 * as "no skin contact" is not a beat; a sensor that cannot tell is believed.
 */
class SkinContactTest {

    @Test
    fun `no contact is not a beat`() {
        assertFalse(SkinContact.counts(2))
    }

    @Test
    fun `contact, or a sensor that cannot tell, is`() {
        assertTrue(SkinContact.counts(3))
        assertTrue(SkinContact.counts(0))
        assertTrue(SkinContact.counts(1))
        assertTrue(SkinContact.counts(null))
    }

    @Test
    fun `the parser reads the contact bits from the flags`() {
        // flags 0b0000_0100 = contact supported, not detected; bpm 72 (UINT8).
        val off = HrMeasurementParser.parse(byteArrayOf(0x04, 72))
        assertFalse(SkinContact.counts(off?.contactStatus))
        // flags 0b0000_0110 = supported and detected.
        val on = HrMeasurementParser.parse(byteArrayOf(0x06, 72))
        assertTrue(SkinContact.counts(on?.contactStatus))
    }
}
