package cc.tumtum.app.export

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The sticker is the card's own block, not the transparent sheet around it (#60). */
class CardStickerTest {

    private val clear = 0x00000000
    private val ink = 0xFF000000.toInt()
    private val faint = 0x01000000

    @Test
    fun `the bounds hold exactly what is not transparent`() {
        val w = 5
        val h = 4
        val pixels = IntArray(w * h) { clear }
        pixels[1 * w + 2] = ink
        pixels[2 * w + 3] = ink

        assertArrayEquals(intArrayOf(2, 1, 4, 3), CardSticker.opaqueBounds(pixels, w, h))
    }

    @Test
    fun `the faintest edge of the gradient counts as card`() {
        // The base under the type fades to transparent; its last visible row
        // belongs to the sticker, or the fade is cut to a hard line.
        val pixels = intArrayOf(clear, faint, clear, clear)

        assertArrayEquals(intArrayOf(1, 0, 2, 1), CardSticker.opaqueBounds(pixels, 4, 1))
    }

    @Test
    fun `a sheet with nothing on it has no bounds`() {
        assertNull(CardSticker.opaqueBounds(IntArray(6) { clear }, 3, 2))
    }
}
