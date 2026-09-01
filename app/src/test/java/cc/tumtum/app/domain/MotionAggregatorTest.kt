package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MotionAggregatorTest {

    @Test
    fun `janela fecha ao cruzar 1s com media e desvio certos`() {
        val agg = MotionAggregator(windowMs = 1_000)
        // 4 leituras dentro do 1º segundo: 1, 3, 1, 3 → média 2, desvio 1
        assertNull(agg.add(0, 1_000_000, 1.0))
        assertNull(agg.add(250, 1_000_250, 3.0))
        assertNull(agg.add(500, 1_000_500, 1.0))
        assertNull(agg.add(750, 1_000_750, 3.0))
        val w = agg.add(1_000, 1_001_000, 9.9)
        assertNotNull(w)
        assertEquals(2.0, w!!.magMean, 1e-9)
        assertEquals(1.0, w.magStd, 1e-9)
        assertEquals(4, w.sampleCount)
        assertEquals(0, w.elapsedRealtimeMs)
        assertEquals(1_000_000, w.wallClockMs)
    }

    @Test
    fun `flush fecha a janela parcial no fim da sessao`() {
        val agg = MotionAggregator()
        agg.add(0, 500, 5.0)
        agg.add(100, 600, 5.0)
        val w = agg.flush()
        assertNotNull(w)
        assertEquals(5.0, w!!.magMean, 1e-9)
        assertEquals(0.0, w.magStd, 1e-9)
        assertEquals(2, w.sampleCount)
        assertNull(agg.flush())
    }

    @Test
    fun `leitura que fecha a janela entra na proxima`() {
        val agg = MotionAggregator(windowMs = 1_000)
        agg.add(0, 0, 1.0)
        val closed = agg.add(1_200, 1_200, 7.0)
        assertEquals(1, closed!!.sampleCount)
        val next = agg.flush()!!
        assertEquals(7.0, next.magMean, 1e-9)
        assertEquals(1_200, next.elapsedRealtimeMs)
    }
}
