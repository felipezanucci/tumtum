package cc.tumtum.capture

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.concurrent.Executors

/**
 * The events, inside the app — and the place to say which night you are in.
 *
 * "Não consegui chegar na tela de eventos" was the report that created this:
 * events existed only as a picker on the capture screen and as pages on the
 * site, and the app had no door to either once "Abrir no site" was removed.
 * Tapping an event here is the same choice the capture screen's picker makes,
 * through the same stored key, so the two can never disagree.
 *
 * Correcting an event lives behind each row's "Editar" — its own explicit
 * door, because a second, hidden meaning on the same tap proved unfindable
 * within minutes of this screen shipping. Creating events is what still lives
 * on the site; recorded as an open item.
 */
class EventsActivity : Activity() {

    private lateinit var api: TumtumApi
    private lateinit var stateView: TextView
    private lateinit var retryButton: Button
    private lateinit var list: LinearLayout

    private val io = Executors.newSingleThreadExecutor()
    private var events: List<EventBrief> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        api = TumtumApi(applicationContext)
        stateView = findViewById(R.id.state)
        retryButton = findViewById(R.id.retry)
        list = findViewById(R.id.list)
        retryButton.setOnClickListener { load() }
        Chrome.wire(this, api)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    private fun load() {
        if (!api.signedIn) {
            showState(getString(R.string.nights_signed_out), retry = false)
            list.removeAllViews()
            return
        }
        showState(getString(R.string.loading), retry = false)
        io.execute {
            val result = runCatching { api.listEvents() }
            runOnUiThread {
                result
                    .onSuccess {
                        events = it
                        render()
                    }
                    .onFailure { error ->
                        showState(
                            getString(R.string.events_failed_screen, error.message),
                            retry = true,
                        )
                        list.removeAllViews()
                    }
            }
        }
    }

    private fun render() {
        list.removeAllViews()

        if (events.isEmpty()) {
            showState(getString(R.string.events_empty), retry = false)
            return
        }
        stateView.visibility = View.GONE
        retryButton.visibility = View.GONE

        val chosen = getSharedPreferences("tumtum", MODE_PRIVATE)
            .getString(Chrome.SELECTED_EVENT, null)
        val inflater = LayoutInflater.from(this)

        for (event in events) {
            val row = inflater.inflate(R.layout.event_row, list, false)
            row.findViewById<TextView>(R.id.eventName).text = event.name
            row.findViewById<TextView>(R.id.eventDetail).text =
                listOfNotNull(dayLabel(event.date), event.venue, event.city)
                    .joinToString(" · ")
            row.findViewById<TextView>(R.id.chosen).visibility =
                if (event.id == chosen) View.VISIBLE else View.GONE
            row.setOnClickListener { choose(event, alreadyChosen = event.id == chosen) }
            row.findViewById<TextView>(R.id.editRow).setOnClickListener {
                EventEditActivity.open(this, event.id)
            }
            list.addView(row)
        }
    }

    /**
     * Tapping the chosen event un-chooses it — the only way back to "sem
     * evento" here, and a person poking the row deserves a visible answer
     * either way.
     */
    private fun choose(event: EventBrief, alreadyChosen: Boolean) {
        getSharedPreferences("tumtum", MODE_PRIVATE).edit()
            .putString(Chrome.SELECTED_EVENT, if (alreadyChosen) null else event.id)
            .apply()
        render()
    }

    /** "29/08" from the plain `YYYY-MM-DD` the API stores. */
    private fun dayLabel(date: String): String? {
        val parts = date.split("-")
        return if (parts.size == 3) "${parts[2]}/${parts[1]}" else date.takeIf { it.isNotEmpty() }
    }

    private fun showState(text: String, retry: Boolean) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
        retryButton.visibility = if (retry) View.VISIBLE else View.GONE
    }
}
