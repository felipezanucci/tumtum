package cc.tumtum.capture

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * Correct an event without leaving the app.
 *
 * "Não consegui editar o evento" — 2026-08-27, minutes after the events
 * screen shipped with selection only. The one correction that cannot wait for
 * a laptop is this one: the hours of the night you are already standing in.
 *
 * Times are picked from lists, never a clock face — the dial the system time
 * picker draws on this Samsung is a poor way to say "22:00" and worse in a
 * hurry, which is why the site's form made the same trade. An end earlier
 * than the start is named for what it means (the small hours of the next
 * day) rather than asking for a second date an under-24h event cannot need.
 */
class EventEditActivity : Activity() {

    private lateinit var api: TumtumApi
    private lateinit var stateView: TextView
    private lateinit var form: LinearLayout
    private lateinit var nameField: EditText
    private lateinit var dateButton: TextView
    private lateinit var startHour: Spinner
    private lateinit var startMinute: Spinner
    private lateinit var endHour: Spinner
    private lateinit var endMinute: Spinner
    private lateinit var midnightHint: TextView
    private lateinit var venueField: EditText
    private lateinit var cityField: EditText
    private lateinit var saveError: TextView
    private lateinit var saveButton: Button

    private val io = Executors.newSingleThreadExecutor()

    private lateinit var eventId: String

    /** `YYYY-MM-DD`, exactly as the API speaks it. */
    private var date: String = ""
    private var saving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_edit)
        api = TumtumApi(applicationContext)

        stateView = findViewById(R.id.state)
        form = findViewById(R.id.form)
        nameField = findViewById(R.id.name)
        dateButton = findViewById(R.id.dateButton)
        startHour = findViewById(R.id.startHour)
        startMinute = findViewById(R.id.startMinute)
        endHour = findViewById(R.id.endHour)
        endMinute = findViewById(R.id.endMinute)
        midnightHint = findViewById(R.id.midnightHint)
        venueField = findViewById(R.id.venue)
        cityField = findViewById(R.id.city)
        saveError = findViewById(R.id.saveError)
        saveButton = findViewById(R.id.save)

        eventId = intent.getStringExtra(EXTRA_EVENT).orEmpty()

        dateButton.setOnClickListener { pickDate() }
        saveButton.setOnClickListener { save() }
        Chrome.wire(this, api)

        load()
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    private fun load() {
        if (eventId.isEmpty()) {
            showState(getString(R.string.edit_load_failed, "sem evento"))
            return
        }
        showState(getString(R.string.loading))
        io.execute {
            val result = runCatching { api.getEvent(eventId) }
            runOnUiThread {
                result
                    .onSuccess { fill(it) }
                    .onFailure { showState(getString(R.string.edit_load_failed, it.message)) }
            }
        }
    }

    private fun fill(event: EventDetail) {
        stateView.visibility = View.GONE
        form.visibility = View.VISIBLE

        nameField.setText(event.name)
        venueField.setText(event.venue.orEmpty())
        cityField.setText(event.city.orEmpty())
        date = event.date
        renderDate()

        bindTime(startHour, startMinute, event.startTime)
        bindTime(endHour, endMinute, event.endTime)
    }

    /**
     * An hour list and a minute list, seeded from "HH:MM[:SS]".
     *
     * Minutes come in fives — shows are not scheduled at 22:07 — but a stored
     * minute off the step is added to the list rather than shown as "--",
     * which would claim the event has no time at all.
     */
    private fun bindTime(hourSpinner: Spinner, minuteSpinner: Spinner, value: String?) {
        val parts = value?.split(":").orEmpty()
        val hour = parts.getOrNull(0)?.padStart(2, '0')
        val minute = parts.getOrNull(1)?.padStart(2, '0')

        val hours = mutableListOf(getString(R.string.no_time))
        hours += (0..23).map { it.toString().padStart(2, '0') }

        val minutes = mutableListOf(getString(R.string.no_time))
        minutes += (0..55 step 5).map { it.toString().padStart(2, '0') }
        if (minute != null && minute !in minutes) minutes.add(1, minute)

        hourSpinner.adapter = adapter(hours)
        minuteSpinner.adapter = adapter(minutes)
        if (hour != null) hourSpinner.setSelection(hours.indexOf(hour).coerceAtLeast(0))
        if (minute != null) minuteSpinner.setSelection(minutes.indexOf(minute).coerceAtLeast(0))

        val watcher = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                renderMidnightHint()
            }

            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }
        hourSpinner.onItemSelectedListener = watcher
        minuteSpinner.onItemSelectedListener = watcher
    }

    private fun adapter(values: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, R.layout.spinner_item, values).also {
            it.setDropDownViewResource(R.layout.spinner_item)
        }

    /** "HH:MM:00", or null while either half still reads "--". */
    private fun timeOf(hourSpinner: Spinner, minuteSpinner: Spinner): String? {
        val hour = hourSpinner.selectedItem?.toString() ?: return null
        if (hour == getString(R.string.no_time)) return null
        val minute = minuteSpinner.selectedItem?.toString()
            ?.takeIf { it != getString(R.string.no_time) } ?: "00"
        return "$hour:$minute:00"
    }

    /** The hint that 03:00 after 22:00 means the small hours of the next day. */
    private fun renderMidnightHint() {
        val start = timeOf(startHour, startMinute)
        val end = timeOf(endHour, endMinute)
        midnightHint.visibility =
            if (start != null && end != null && end < start) View.VISIBLE else View.GONE
    }

    private fun pickDate() {
        val parts = date.split("-").mapNotNull { it.toIntOrNull() }
        val (year, month, day) = if (parts.size == 3) parts else listOf(2026, 1, 1)
        DatePickerDialog(
            this,
            { _, y, m, d ->
                date = "%04d-%02d-%02d".format(y, m + 1, d)
                renderDate()
            },
            year,
            month - 1,
            day,
        ).show()
    }

    private fun renderDate() {
        val parts = date.split("-")
        dateButton.text =
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
    }

    private fun save() {
        if (saving) return
        val name = nameField.text.toString().trim()
        if (name.isEmpty()) {
            showSaveError(getString(R.string.edit_needs_name))
            return
        }

        saving = true
        saveError.visibility = View.GONE
        saveButton.isEnabled = false
        saveButton.text = getString(R.string.saving)

        // Absent keys stay untouched server-side, so only real values go out.
        val changes = JSONObject().put("name", name).put("date", date)
        venueField.text.toString().trim().takeIf { it.isNotEmpty() }
            ?.let { changes.put("venue", it) }
        cityField.text.toString().trim().takeIf { it.isNotEmpty() }
            ?.let { changes.put("city", it) }
        timeOf(startHour, startMinute)?.let { changes.put("start_time", it) }
        timeOf(endHour, endMinute)?.let { changes.put("end_time", it) }

        io.execute {
            val result = runCatching { api.updateEvent(eventId, changes) }
            runOnUiThread {
                saving = false
                saveButton.isEnabled = true
                saveButton.text = getString(R.string.save)
                result
                    // Straight back to the list, which reloads on resume — the
                    // corrected event on screen is the save confirmation.
                    .onSuccess { finish() }
                    .onFailure { error ->
                        if (error is TumtumApi.ApiException && error.code == 401) {
                            api.signOut()
                            showSaveError(getString(R.string.nights_signed_out))
                        } else {
                            showSaveError(getString(R.string.edit_save_failed, error.message))
                        }
                    }
            }
        }
    }

    private fun showSaveError(text: String) {
        saveError.text = text
        saveError.visibility = View.VISIBLE
    }

    private fun showState(text: String) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
        form.visibility = View.GONE
    }

    companion object {
        private const val EXTRA_EVENT = "event_id"

        fun open(context: Context, eventId: String) {
            context.startActivity(
                Intent(context, EventEditActivity::class.java).putExtra(EXTRA_EVENT, eventId)
            )
        }
    }
}
