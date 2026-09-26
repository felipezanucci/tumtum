package cc.tumtum.app.export

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * A person's video, without what the camera wrote about it (LGPD
 * remediation, 26/09, audit item AL-14).
 *
 * Until then the video behind a Story went out as a byte-for-byte copy: the
 * GPS position and the original date the phone recorded travelled with it to
 * Instagram, Facebook or Snapchat. A remux keeps the picture and the sound
 * exactly as they were — no re-encode, no quality lost — and writes a new
 * MP4 container around them: only the audio and video tracks are copied,
 * `MediaMuxer.setLocation` is never called, and the container's creation
 * time is the muxer's, not the camera's. The orientation is kept, because it
 * is how the picture stands, not a fact about the person.
 *
 * Fails closed: any error returns false (or null) and the caller does not
 * hand the original over instead.
 */
object StripMetadata {
    private const val TAG = "StripMetadata"
    private const val MIN_BUFFER = 4 * 1024 * 1024

    /** Remuxes [source] (the picker's content URI) into [out]. */
    fun remux(context: Context, source: Uri, out: File): Boolean =
        remux(out, rotationOf { it.setDataSource(context, source) }) { it.setDataSource(context, source, null) }

    /** Remuxes the file [input] into [out]. */
    fun remux(input: File, out: File): Boolean =
        remux(out, rotationOf { it.setDataSource(input.absolutePath) }) { it.setDataSource(input.absolutePath) }

    /**
     * Replaces [file] with its stripped copy. Returns the file, or null — and
     * no file at all — when the remux failed.
     */
    fun inPlace(file: File): File? {
        val tmp = File(file.parentFile, file.name + ".clean.mp4")
        val ok = remux(file, tmp)
        file.delete()
        if (!ok || !tmp.renameTo(file)) {
            tmp.delete()
            return null
        }
        return file
    }

    private fun rotationOf(setSource: (MediaMetadataRetriever) -> Unit): Int = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            setSource(retriever)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        } finally {
            retriever.release()
        }
    }.getOrDefault(0)

    private fun remux(out: File, fallbackRotation: Int, setSource: (MediaExtractor) -> Unit): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var started = false
        try {
            setSource(extractor)
            out.delete()
            val m = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = m
            val tracks = HashMap<Int, Int>()
            var bufferSize = MIN_BUFFER
            var rotation: Int? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                // Only picture and sound. Some phones write a separate
                // metadata track (GPS among it); it is left behind.
                if (!mime.startsWith("video/") && !mime.startsWith("audio/")) continue
                if (mime.startsWith("video/") && format.containsKey(MediaFormat.KEY_ROTATION)) {
                    rotation = format.getInteger(MediaFormat.KEY_ROTATION)
                }
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    bufferSize = maxOf(bufferSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
                }
                extractor.selectTrack(i)
                tracks[i] = m.addTrack(format)
            }
            if (tracks.isEmpty()) {
                out.delete()
                return false
            }
            m.setOrientationHint(rotation ?: fallbackRotation)
            m.start()
            started = true

            val buffer = ByteBuffer.allocate(bufferSize)
            val info = MediaCodec.BufferInfo()
            while (true) {
                val track = extractor.sampleTrackIndex
                if (track < 0) break
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                val target = tracks[track]
                if (target != null) {
                    info.offset = 0
                    info.size = size
                    info.presentationTimeUs = extractor.sampleTime
                    info.flags = if ((extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                        MediaCodec.BUFFER_FLAG_KEY_FRAME
                    } else {
                        0
                    }
                    m.writeSampleData(target, buffer, info)
                }
                extractor.advance()
            }
            m.stop()
            started = false
            return out.length() > 0
        } catch (e: Exception) {
            Log.w(TAG, "remux failed", e)
            if (started) runCatching { muxer?.stop() }
            started = false
            out.delete()
            return false
        } finally {
            runCatching { muxer?.release() }
            runCatching { extractor.release() }
        }
    }
}
