package cc.tumtum.capture

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Why TumTum asks to read your heart rate — the screen Health Connect opens
 * when somebody taps our name in its permission UI.
 *
 * The brand goes quiet here on purpose: on permission, privacy and consent
 * the manual chooses careful over fun. The text states the whole contract —
 * one permission, read only, only the window the person picks, only when
 * they tap, revocable in Health Connect at any time — because this screen is
 * the one place the person looks when deciding whether to trust us.
 */
class PrivacyRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rationale)
    }
}
