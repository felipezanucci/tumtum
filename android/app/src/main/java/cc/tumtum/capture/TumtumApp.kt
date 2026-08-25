package cc.tumtum.capture

import android.app.Application

/**
 * Keeps the reason for a crash so the next launch can show it.
 *
 * This app is written without a device to run it on, so a crash reaching a
 * phone says only "o app apresenta falhas contínuas" — which is the same
 * message for a missing permission, a bad notification and a typo. Storing the
 * cause turns a dead end into a sentence on screen, and the person holding the
 * phone into someone who can report it.
 */
class TumtumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                getSharedPreferences("tumtum", MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", describe(error))
                    .commit() // commit, not apply: the process is about to end.
            }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun describe(error: Throwable): String {
        val top = error.stackTrace.firstOrNull { it.className.startsWith("cc.tumtum") }
            ?: error.stackTrace.firstOrNull()
        return buildString {
            append(error::class.java.simpleName)
            error.message?.let { append(": ").append(it) }
            top?.let { append(" (").append(it.fileName).append(':').append(it.lineNumber).append(')') }
        }
    }
}
