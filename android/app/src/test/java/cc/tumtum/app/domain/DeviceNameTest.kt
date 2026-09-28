package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** What a sensor's name becomes before it leaves the phone (28/09): the model, not the unit. */
class DeviceNameTest {

    @Test
    fun `a Polar strap loses its serial`() {
        assertEquals("Polar H10", DeviceName.model("Polar H10 19B38E3F"))
        assertEquals("Polar Sense", DeviceName.model("Polar Sense A1B2C3D4"))
    }

    @Test
    fun `a name with no serial stays as it is`() {
        assertEquals("Polar H10", DeviceName.model("Polar H10"))
        assertEquals("Garmin HRM-Dual", DeviceName.model("Garmin HRM-Dual"))
        assertEquals("Sensor ao vivo", DeviceName.model("Sensor ao vivo"))
    }

    @Test
    fun `other makers' unit numbers go too`() {
        assertEquals("TICKR", DeviceName.model("TICKR 1A2B"))
        assertEquals("HRM-Pro", DeviceName.model("HRM-Pro:123456"))
    }

    @Test
    fun `a model made only of hex keeps its one word`() {
        assertEquals("ABC123", DeviceName.model("ABC123"))
    }

    @Test
    fun `nothing is nothing`() {
        assertNull(DeviceName.model(null))
        assertNull(DeviceName.model("   "))
    }
}
