package cc.tumtum.app.export

import android.app.Activity
import android.os.Bundle

/**
 * Where TikTok returns after a share (#60). Share Kit needs an exported
 * activity it can start by name to hand its answer back; the answer carries
 * nothing the app acts on — the person already sees TikTok's own result — so
 * this closes at once and the card screen underneath is what comes back.
 */
class TikTokShareResultActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
