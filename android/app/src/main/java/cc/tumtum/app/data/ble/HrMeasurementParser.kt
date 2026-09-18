package cc.tumtum.app.data.ble

/**
 * Uma leitura do characteristic Heart Rate Measurement (0x2A37), como recebida.
 * A amostra é persistida crua (§1.4) — nenhuma suavização acontece aqui.
 */
data class HrMeasurement(
    val bpm: Int,
    /** bits 1–2 das flags: 0/1 = sem suporte, 2 = sem contato, 3 = contato ok. */
    val contactStatus: Int,
    val energyExpended: Int?,
    /** Intervalos RR em milissegundos (unidade BLE: 1/1024s). Podem vir vários por pacote. */
    val rrIntervalsMs: List<Double>,
)

/**
 * Parser completo do Heart Rate Measurement (spec GATT 0x2A37):
 *  - bit 0: formato do valor (0 = UINT8, 1 = UINT16 little-endian)
 *  - bits 1–2: status do contato com a pele
 *  - bit 3: Energy Expended presente (2 bytes, pulados antes dos RR)
 *  - bit 4: intervalos RR presentes (UINT16 em 1/1024s, todos até o fim do array)
 */
object HrMeasurementParser {

    const val CONTACT_NOT_SUPPORTED = 0
    const val CONTACT_NOT_DETECTED = 2
    const val CONTACT_DETECTED = 3

    fun parse(data: ByteArray): HrMeasurement? {
        if (data.isEmpty()) return null
        val flags = data[0].toInt() and 0xFF
        val uint16 = flags and 0x01 != 0
        val contactStatus = (flags shr 1) and 0x03
        val hasEnergy = flags and 0x08 != 0
        val hasRr = flags and 0x10 != 0

        var i = 1
        val bpm: Int
        if (uint16) {
            if (data.size < i + 2) return null
            bpm = u16(data, i)
            i += 2
        } else {
            if (data.size < i + 1) return null
            bpm = data[i].toInt() and 0xFF
            i += 1
        }

        var energy: Int? = null
        if (hasEnergy) {
            // Energy Expended vem ANTES dos RR — pular mesmo que não seja usado.
            if (data.size >= i + 2) energy = u16(data, i)
            i += 2
        }

        val rr = mutableListOf<Double>()
        if (hasRr) {
            while (data.size >= i + 2) {
                rr += (u16(data, i)) * 1000.0 / 1024.0
                i += 2
            }
        }

        return HrMeasurement(bpm = bpm, contactStatus = contactStatus, energyExpended = energy, rrIntervalsMs = rr)
    }

    private fun u16(data: ByteArray, i: Int): Int =
        (data[i].toInt() and 0xFF) or ((data[i + 1].toInt() and 0xFF) shl 8)
}
