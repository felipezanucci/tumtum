package cc.tumtum.app.domain

import kotlin.math.sqrt

/** Uma janela de 1s de movimento fechada: média e desvio da magnitude − gravidade. */
data class MotionWindow(
    val wallClockMs: Long,
    val elapsedRealtimeMs: Long,
    val magMean: Double,
    val magStd: Double,
    val sampleCount: Int,
)

/**
 * Agrega o acelerômetro a 1 Hz (§7): não gravamos a taxa bruta do sensor —
 * seriam centenas de milhares de linhas sem ganho. O que importa é o pico
 * com movimento baixo: a pessoa parada, arrepiada, na música que importa.
 */
class MotionAggregator(private val windowMs: Long = 1_000L) {

    private var windowStartElapsed = -1L
    private var windowStartWall = 0L
    private var sum = 0.0
    private var sumSq = 0.0
    private var count = 0

    /** Alimenta uma leitura; devolve a janela fechada quando o segundo vira, senão null. */
    fun add(elapsedRealtimeMs: Long, wallClockMs: Long, magnitudeMinusGravity: Double): MotionWindow? {
        var closed: MotionWindow? = null
        if (windowStartElapsed < 0) {
            windowStartElapsed = elapsedRealtimeMs
            windowStartWall = wallClockMs
        } else if (elapsedRealtimeMs - windowStartElapsed >= windowMs) {
            closed = flush()
            windowStartElapsed = elapsedRealtimeMs
            windowStartWall = wallClockMs
        }
        sum += magnitudeMinusGravity
        sumSq += magnitudeMinusGravity * magnitudeMinusGravity
        count += 1
        return closed
    }

    /** Fecha a janela corrente (fim de sessão) — nada fica para trás. */
    fun flush(): MotionWindow? {
        if (count == 0 || windowStartElapsed < 0) return null
        val mean = sum / count
        val variance = (sumSq / count - mean * mean).coerceAtLeast(0.0)
        val window = MotionWindow(
            wallClockMs = windowStartWall,
            elapsedRealtimeMs = windowStartElapsed,
            magMean = mean,
            magStd = sqrt(variance),
            sampleCount = count,
        )
        sum = 0.0
        sumSq = 0.0
        count = 0
        return window
    }
}
