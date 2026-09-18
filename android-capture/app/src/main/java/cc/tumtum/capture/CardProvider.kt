package cc.tumtum.capture

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * Serves a just-downloaded card to the share sheet.
 *
 * Another app cannot read our cache directly, and file:// URIs have been
 * banned from intents since Android 7 — a shared file must arrive through a
 * content provider. The androidx FileProvider is the usual answer, but it is
 * the APK's only would-be dependency; this is the same behaviour in thirty
 * lines of framework, read-only, for exactly one directory.
 */
class CardProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val name = uri.lastPathSegment ?: throw SecurityException("Sem arquivo")
        // Only bare names, only from the cards cache: no traversal, no mode
        // but read. This provider exists to hand out one PNG at a time.
        require(!name.contains('/') && !name.contains("..")) { "Nome inválido" }
        val file = File(File(requireNotNull(context).cacheDir, "cards"), name)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String = "image/png"

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?,
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val AUTHORITY = "cc.tumtum.capture.cards"
    }
}
