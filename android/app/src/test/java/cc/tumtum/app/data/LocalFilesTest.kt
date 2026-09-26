package cc.tumtum.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalFilesTest {

    @Test
    fun `only files older than an hour are stale`() {
        val now = 10_000_000L
        val files = mapOf(
            "old.png" to now - LocalFiles.SHARE_MAX_AGE_MS - 1,
            "exactly-an-hour.png" to now - LocalFiles.SHARE_MAX_AGE_MS,
            "fresh.mp4" to now - 1_000,
        )
        assertEquals(setOf("old.png"), LocalFiles.stale(files, now))
    }
}
