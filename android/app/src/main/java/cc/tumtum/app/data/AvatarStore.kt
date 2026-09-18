package cc.tumtum.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Foto de perfil: importada do picker, reduzida a 512px e guardada em filesDir.
 * Nome com timestamp para o Compose invalidar o cache quando a foto troca.
 */
object AvatarStore {

    suspend fun import(context: Context, uri: Uri, previousPath: String?): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0) return@runCatching null

                var sample = 1
                while (bounds.outWidth / (sample * 2) >= 512 && bounds.outHeight / (sample * 2) >= 512) {
                    sample *= 2
                }
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    ?: return@runCatching null

                val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
                bitmap.recycle()
                previousPath?.let { File(it).delete() }
                file.absolutePath
            }.getOrNull()
        }
}
