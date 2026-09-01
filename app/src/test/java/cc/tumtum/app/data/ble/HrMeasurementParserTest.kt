package cc.tumtum.app.data.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HrMeasurementParserTest {

    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    @Test
    fun `uint8 sem flags`() {
        val m = HrMeasurementParser.parse(bytes(0x00, 142))!!
        assertEquals(142, m.bpm)
        assertEquals(HrMeasurementParser.CONTACT_NOT_SUPPORTED, m.contactStatus)
        assertNull(m.energyExpended)
        assertEquals(0, m.rrIntervalsMs.size)
    }

    @Test
    fun `uint16 little endian`() {
        // flags 0x01, valor 0x0121 = 289 (acima de 255, só cabe em UINT16)
        val m = HrMeasurementParser.parse(bytes(0x01, 0x21, 0x01))!!
        assertEquals(289, m.bpm)
    }

    @Test
    fun `contato detectado e nao detectado`() {
        assertEquals(HrMeasurementParser.CONTACT_DETECTED, HrMeasurementParser.parse(bytes(0x06, 90))!!.contactStatus)
        assertEquals(HrMeasurementParser.CONTACT_NOT_DETECTED, HrMeasurementParser.parse(bytes(0x04, 90))!!.contactStatus)
    }

    @Test
    fun `energy expended e pulado antes dos rr`() {
        // flags: uint8 + energy (0x08) + rr (0x10) = 0x18
        // bpm=120, energy=0x0102, um RR de 1024 (= exatamente 1000ms)
        val m = HrMeasurementParser.parse(bytes(0x18, 120, 0x02, 0x01, 0x00, 0x04))!!
        assertEquals(120, m.bpm)
        assertEquals(0x0102, m.energyExpended)
        assertEquals(1, m.rrIntervalsMs.size)
        assertEquals(1000.0, m.rrIntervalsMs[0], 0.0001)
    }

    @Test
    fun `multiplos rr no mesmo pacote convertidos para ms`() {
        // flags: uint8 + rr = 0x10; bpm=88; RRs crus: 512, 1024, 768
        val m = HrMeasurementParser.parse(bytes(0x10, 88, 0x00, 0x02, 0x00, 0x04, 0x00, 0x03))!!
        assertEquals(3, m.rrIntervalsMs.size)
        assertEquals(500.0, m.rrIntervalsMs[0], 0.0001)
        assertEquals(1000.0, m.rrIntervalsMs[1], 0.0001)
        assertEquals(750.0, m.rrIntervalsMs[2], 0.0001)
    }

    @Test
    fun `rr de 800ms tipico de polar`() {
        // 819 cru * 1000/1024 = 799.8046875 ms
        val m = HrMeasurementParser.parse(bytes(0x10, 75, 0x33, 0x03))!!
        assertEquals(799.8046875, m.rrIntervalsMs[0], 0.0001)
    }

    @Test
    fun `pacote truncado nao explode`() {
        assertNull(HrMeasurementParser.parse(ByteArray(0)))
        assertNull(HrMeasurementParser.parse(bytes(0x01, 0x50))) // uint16 sem segundo byte
        // RR truncado no meio: lê o que dá, ignora o resto
        val m = HrMeasurementParser.parse(bytes(0x10, 88, 0x00))!!
        assertEquals(0, m.rrIntervalsMs.size)
    }
}
