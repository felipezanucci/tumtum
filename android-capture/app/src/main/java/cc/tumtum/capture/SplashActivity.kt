package cc.tumtum.capture

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity

/**
 * Two seconds of heartbeat before the app.
 *
 * Felipe's call, 2026-08-27: every open of TumTum starts with the wordmark
 * beating. "Every open" here means every cold start — bringing a task that is
 * already alive back to the foreground does not replay it, which is exactly
 * right for someone flipping back to a running capture at a show.
 *
 * The film is a guest, never a gate. Anything that goes wrong with it — a
 * codec quirk, a corrupted resource, a device that will not play it — walks
 * straight into the app instead of trapping somebody at a black screen. And a
 * tap skips it: two seconds is an arrival, not a toll.
 */
class SplashActivity : ComponentActivity() {

    private var proceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val video = findViewById<VideoView>(R.id.splashVideo)
        // Never grab audio focus: the film is effectively silent, and ducking
        // somebody's music for a splash screen would be the app being rude
        // about its own arrival.
        video.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        video.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.splash}"))
        video.setOnCompletionListener { proceed() }
        video.setOnErrorListener { _, _, _ ->
            proceed()
            true
        }
        findViewById<android.view.View>(R.id.splashRoot).setOnClickListener { proceed() }
        video.start()
    }

    @Deprecated("Deprecated in Android; back during a splash means skip, not exit.")
    override fun onBackPressed() {
        proceed()
    }

    /**
     * Leaving mid-film ends this screen rather than pausing it. There is no
     * state worth resuming in a two-second video, and a half-played splash
     * waiting behind the home screen is a stale claim about what is opening.
     */
    override fun onStop() {
        super.onStop()
        if (!proceeded) finish()
    }

    private fun proceed() {
        if (proceeded) return
        proceeded = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
