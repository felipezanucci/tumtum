package cc.tumtum.app.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Snap caps a sticker at 300 dp on each side (24/09): asked for 300 wide and
 * ~350 tall, the card ran off the bottom of the preview.
 */
class SnapStickerSizeTest {

    @Test
    fun `a card taller than wide is 300 dp tall and narrower`() {
        val (w, h) = ShareTargets.snapStickerSize(1.16f)
        assertEquals(300, h)
        assertEquals(258, w)
    }

    @Test
    fun `a card wider than tall is 300 dp wide and shorter`() {
        assertEquals(300 to 150, ShareTargets.snapStickerSize(0.5f))
    }

    @Test
    fun `no side ever passes Snap's limit, whatever the shape`() {
        listOf(0.1f, 0.9f, 1f, 1.16f, 1.78f, 3f).forEach { aspect ->
            val (w, h) = ShareTargets.snapStickerSize(aspect)
            assertTrue("$aspect → ${w}x$h", w <= 300 && h <= 300)
        }
    }

    @Test
    fun `a broken aspect falls back to a square`() {
        assertEquals(300 to 300, ShareTargets.snapStickerSize(Float.NaN))
        assertEquals(300 to 300, ShareTargets.snapStickerSize(0f))
    }
}
