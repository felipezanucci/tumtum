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

    @Test
    fun `o maior momento e o pico`() {
        val quiet = samples(5, 600, { 90 + (it % 7) })
        val spike = HrSample(t0.plusSeconds(2000), 187)
        val moments = NightAnalyzer.moments(quiet + spike)
        assertTrue(moments.isNotEmpty())
        assertEquals(187, moments.first().bpm)
        assertTrue(moments.first().isPeak)
        assertEquals(1, moments.count { it.isPeak })
    }

    @Test
    fun `momentos respeitam separacao minima de 10min`() {
        val all = samples(5, 720, { 100 })  // 1h monótona
        val moments = NightAnalyzer.moments(all)
        for (i in moments.indices) {
            for (j in i + 1 until moments.size) {
                val d = java.time.Duration.between(moments[i].at, moments[j].at).abs()
                assertTrue(d >= java.time.Duration.ofMinutes(10))
            }
        }
    }
}
