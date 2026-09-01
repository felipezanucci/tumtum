package cc.tumtum.capture

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView

/**
 * The menu in the top right corner, on every screen after sign-in.
 *
 * Until it existed each screen was its own island: the capture screen could
 * reach Minhas noites through one link, and nothing could reach the events.
 * Navigation is one menu, wired identically everywhere, so no screen can
 * quietly become unreachable again — the failure that trapped the whole app
 * behind the upload's success branch on 2026-08-27.
 */
object Chrome {

    /** Where the capture screen keeps the chosen event. One key, shared. */
    const val SELECTED_EVENT = "selected_event_id"

    private const val ITEM_CAPTURE = 1
    private const val ITEM_NIGHTS = 2
    private const val ITEM_EVENTS = 3
    private const val ITEM_WATCH = 4
    private const val ITEM_SIGN_OUT = 5

    /**
     * Attach the menu to a screen's top bar.
     *
     * Safe to call on every render: visibility follows the session, which can
     * die while a screen sits in the background. `onSignedOut` is how the
     * capture screen redraws itself when Sair is chosen while it is already
     * the visible screen and no navigation happens.
     */
    fun wire(activity: Activity, api: TumtumApi, onSignedOut: (() -> Unit)? = null) {
        // The wordmark is the way home from any screen — Felipe's ask, and the
        // convention every app trains people into anyway.
        activity.findViewById<ImageView>(R.id.topLogo)
            ?.setOnClickListener { goHome(activity) }

        val button = activity.findViewById<TextView>(R.id.menuButton) ?: return
        button.visibility = if (api.signedIn) View.VISIBLE else View.GONE
        button.setOnClickListener { anchor ->
            val popup = PopupMenu(activity, anchor)
            popup.menu.add(0, ITEM_CAPTURE, 0, activity.getString(R.string.menu_capture))
            popup.menu.add(0, ITEM_NIGHTS, 1, activity.getString(R.string.menu_nights))
            popup.menu.add(0, ITEM_EVENTS, 2, activity.getString(R.string.menu_events))
            popup.menu.add(0, ITEM_WATCH, 3, activity.getString(R.string.menu_watch))
            popup.menu.add(0, ITEM_SIGN_OUT, 4, activity.getString(R.string.menu_sign_out))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    ITEM_CAPTURE -> goHome(activity)
                    ITEM_NIGHTS ->
                        if (activity !is SessionsActivity) {
                            activity.startActivity(Intent(activity, SessionsActivity::class.java))
                        }
                    ITEM_EVENTS ->
                        if (activity !is EventsActivity) {
                            activity.startActivity(Intent(activity, EventsActivity::class.java))
                        }
                    ITEM_WATCH ->
                        if (activity !is WatchImportActivity) {
                            activity.startActivity(Intent(activity, WatchImportActivity::class.java))
                        }
                    ITEM_SIGN_OUT -> {
                        // Deliberate and immediate: the person who needs this
                        // is standing at a login that will not accept them,
                        // not reading a confirmation dialog. Signing out only
                        // forgets the token — a running capture, and any
                        // unsent readings, stay exactly where they are.
                        api.signOut()
                        onSignedOut?.invoke()
                        goHome(activity)
                    }
                }
                true
            }
            popup.show()
        }
    }

    /** Back to the capture screen, reusing the one already under the stack. */
    fun goHome(activity: Activity) {
        if (activity is MainActivity) return
        activity.startActivity(
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }
}
