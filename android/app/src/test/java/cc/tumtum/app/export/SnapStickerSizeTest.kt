package cc.tumtum.app.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Snapchat's sticker (24/09): the card at the screen's width, as on the
 * burned video, with its foot above Snapchat's row of friends.
 */
class SnapStickerSizeTest {

    // Felipe's phone, roughly: 384 × 780 dp.
    private val w = 384
    private val h = 780

    @Test
    fun `the card takes the screen's width at its own shape`() {
        val p = ShareTargets.snapStickerPlacement(0.9f, w, h)
        assertEquals(384, p.widthDp)
        assertEquals(345, p.heightDp)
    }

    @Test
    fun `its foot sits above the row of friends`() {
        val p = ShareTargets.snapStickerPlacement(0.9f, w, h)
        val foot = p.posY + p.heightDp.toDouble() / h / 2
        assertEquals(0.76, foot, 0.001)
    }

    @Test
    fun `a card too tall for the room is narrowed, never pushed under the bars`() {
        val p = ShareTargets.snapStickerPlacement(2.5f, w, h)
        val top = p.posY - p.heightDp.toDouble() / h / 2
        assertTrue("top at $top", top >= 0.119)
        assertTrue(p.widthDp < w)
    }

    @Test
    fun `a broken aspect falls back to a square`() {
        val p = ShareTargets.snapStickerPlacement(Float.NaN, w, h)
        assertEquals(p.widthDp, p.heightDp)
    }
}
