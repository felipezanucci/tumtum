package cc.tumtum.app.data

import android.content.Context
import java.io.File

/**
 * Files the app leaves behind, and when they go (LGPD remediation, 26/09).
 *
 * - `cacheDir/cards` holds every card, sticker and video handed to a share
 *   intent. The receiving app reads it while the person is in it, so a file
 *   cannot go the instant the intent is launched; it goes after an hour,
 *   swept each time a share comes back ([pruneShareCache]).
 * - Deleting the account takes everything local with it ([wipeAccountFiles]):
 *   the share cache, the operator's export ZIPs, the profile photos and the
 *   photos behind cards.
 */
object LocalFiles {

    const val SHARE_MAX_AGE_MS = 60 * 60 * 1000L

    /** The names among [files] (name → last modified, epoch ms) that are older than [maxAgeMs]. Pure, tested. */
    fun stale(files: Map<String, Long>, nowMs: Long, maxAgeMs: Long = SHARE_MAX_AGE_MS): Set<String> =
        files.filterValues { nowMs - it > maxAgeMs }.keys

    /** Deletes the share cache's files older than an hour. Safe to call from any thread; never throws. */
    fun pruneShareCache(context: Context, nowMs: Long = System.currentTimeMillis()) {
        runCatching {
            val dir = File(context.cacheDir, "cards")
            val files = dir.listFiles()?.filter { it.isFile } ?: return
            val old = stale(files.associate { it.name to it.lastModified() }, nowMs)
            files.filter { it.name in old }.forEach { runCatching { it.delete() } }
        }
    }

    /** Everything local that belonged to the account, after the server deleted it. */
    fun wipeAccountFiles(context: Context) {
        runCatching { File(context.cacheDir, "cards").deleteRecursively() }
        runCatching { File(context.cacheDir, "exports").deleteRecursively() }
        runCatching {
            context.filesDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("avatar_") }
                ?.forEach { it.delete() }
        }
        CardPhotoStore.deleteAll(context)
    }
}
