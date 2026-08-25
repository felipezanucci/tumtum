package cc.tumtum.capture

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * The whole app, for now: proof that the project builds, the manifest is
 * accepted, and the foreground service starts.
 *
 * Deliberately dependency-free. Everything here comes from the Android
 * framework, so there is no version matrix to get wrong — which matters while
 * this is being written without a device to compile against.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.status).text = "Esqueleto instalado."
    }
}
