package cc.tumtum.app.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NightAnalyzerTest {

    private val t0: Instant = Instant.parse("2026-03-22T20:00:00Z")

    private fun samples(intervalSec: Long, count: Int, bpm: (Int) -> Int, start: Instant = t0): List<HrSample> =
        (0 until count).map { i -> HrSample(start.plusSeconds(i * intervalSec), bpm(i)) }

    @Test
    fun `gap maior que 60s aparece como buraco`() {
        // 10 min de dados, buraco de 52 min, mais 10 min de dados
        val a = samples(5, 120, { 100 })
        val b = samples(5, 120, { 110 }, start = t0.plusSeconds(600 + 52 * 60))
        val windowEnd = b.last().time
        val gaps = NightAnalyzer.gaps(a + b, t0, windowEnd)
        assertEquals(1, gaps.size)
        assertEquals(a.last().time, gaps[0].start)
        assertEquals(b.first().time, gaps[0].end)
    }

    @Test
    fun `uma cinta a 1 Hz tirada por 50 s deixa um buraco`() {
        // b201, 25/09: the strap off for fifty seconds was drawn as a straight line.
        val a = samples(1, 40, { 75 })
        val b = samples(1, 40, { 70 }, start = t0.plusSeconds(90))
        val gaps = NightAnalyzer.gaps(a + b, t0, b.last().time)
        assertEquals(listOf(Gap(a.last().time, b.first().time)), gaps)
        assertEquals(NightAnalyzer.DENSE_GAP_SEC, NightAnalyzer.gapThresholdSec(a + b))
    }

    @Test
    fun `um relogio a 1 por minuto nao vira buraco a cada leitura`() {
        val watch = samples(60, 30, { 80 })
        assertEquals(emptyList<Gap>(), NightAnalyzer.gaps(watch, t0, watch.last().time))
        assertEquals(NightAnalyzer.GAP_THRESHOLD_SEC, NightAnalyzer.gapThresholdSec(watch))
    }

    @Test
    fun `sem amostra nenhuma, a janela inteira e buraco`() {
        val end = t0.plusSeconds(3600)
        val gaps = NightAnalyzer.gaps(emptyList(), t0, end)
        assertEquals(listOf(Gap(t0, end)), gaps)
    }

    @Test
    fun `cobertura nunca conta o buraco`() {
        val a = samples(5, 120, { 100 })                                  // ~10 min cobertos
        val end = t0.plusSeconds(3600)                                    // janela de 1h
        val pct = NightAnalyzer.coveragePct(a, t0, end)
        assertTrue("cobertura $pct deveria ficar perto de 17%", pct in 10..25)
    }

    @Test
    fun `intervalo mediano reflete a densidade da fonte`() {
        assertEquals(2, NightAnalyzer.medianIntervalSec(samples(2, 100, { 100 })))
        assertEquals(60, NightAnalyzer.medianIntervalSec(samples(60, 30, { 100 })))
    }

    // ---- Moments: the server's detector (28/09). Each expectation below was
    // checked against backend/app/services/peak_detection.py on the same data.

    /** A strap at 1 Hz, quiet around 80 with a ±2 bpm wobble, and [bpm] where [override] says. */
    private fun strap(seconds: Int, override: (Int) -> Int?): List<HrSample> =
        samples(1, seconds, { i -> override(i) ?: (80 + (i % 5) - 2) })

    @Test
    fun `a 17-second night is no moment`() {
        // The b-round of 27/09: 71 bpm over a 69 average became a "moment" here
        // and nothing on the server, and the night changed headline on upload.
        val night = samples(1, 17, { listOf(69, 70, 71, 69, 70)[it % 5] })
        assertEquals(emptyList<Moment>(), NightAnalyzer.moments(night))
    }

    @Test
    fun `a song sung start to finish is one moment, with its length`() {
        val night = strap(2400) { i -> if (i in 1500 until 1680) 110 else null }
        val moments = NightAnalyzer.moments(night)
        assertEquals(1, moments.size)
        assertEquals(110, moments[0].bpm)
        assertEquals(179, moments[0].durationSec)
        assertEquals(t0.plusSeconds(1501), moments[0].at)
        assertTrue(moments[0].isPeak)
    }

    @Test
    fun `a wobble a person would not feel is nothing`() {
        assertEquals(emptyList<Moment>(), NightAnalyzer.moments(samples(1, 2400, { 80 + (it % 9) })))
    }

    @Test
    fun `a one-second spike is noise`() {
        assertEquals(emptyList<Moment>(), NightAnalyzer.moments(strap(2400) { i -> if (i == 1200) 180 else null }))
    }

    @Test
    fun `two bursts twenty seconds apart are one moment`() {
        val night = strap(2400) { i -> if (i in 1200 until 1260 || i in 1280 until 1340) 115 else null }
        val moments = NightAnalyzer.moments(night)
        assertEquals(1, moments.size)
        assertEquals(115, moments[0].bpm)
        assertEquals(60, moments[0].durationSec)
    }

    @Test
    fun `a watch at one reading a minute gets the same detector`() {
        val night = samples(60, 70, { if (it in 30 until 40) 110 else 75 })
        val moments = NightAnalyzer.moments(night)
        assertEquals(1, moments.size)
        assertEquals(110, moments[0].bpm)
        assertEquals(540, moments[0].durationSec)
        assertEquals(t0.plusSeconds(1800), moments[0].at)
    }

    @Test
    fun `strongest first, and the highest is the peak`() {
        // A three-minute song at 120 outweighs a one-minute goal at 140; the
        // goal is still the night's peak.
        val night = strap(3600) { i ->
            when (i) {
                in 900 until 1080 -> 120
                in 2400 until 2460 -> 140
                else -> null
            }
        }
        val moments = NightAnalyzer.moments(night)
        assertEquals(listOf(120, 140), moments.map { it.bpm })
        assertEquals(listOf(180, 62), moments.map { it.durationSec })
        assertEquals(listOf(false, true), moments.map { it.isPeak })
    }

    @Test
    fun `the three guards on the z-score`() {
        // A steady stretch: no division by a near-zero spread.
        assertEquals(1.2, NightAnalyzer.zScore(12.0, 0.5), 1e-9)
        assertEquals(0.0, NightAnalyzer.zScore(-12.0, 0.5), 1e-9)
        // Above 30 bpm is always significant, whatever the spread.
        assertEquals(40.0 / 15.0, NightAnalyzer.zScore(40.0, 100.0), 1e-9)
        // Under 10 bpm cannot open a region; under 5 it is nothing.
        assertEquals(NightAnalyzer.Z_EXIT, NightAnalyzer.zScore(8.0, 1.5), 1e-9)
        assertEquals(0.0, NightAnalyzer.zScore(4.0, 1.5), 1e-9)
        assertEquals(6.0, NightAnalyzer.zScore(12.0, 2.0), 1e-9)
    }
}
