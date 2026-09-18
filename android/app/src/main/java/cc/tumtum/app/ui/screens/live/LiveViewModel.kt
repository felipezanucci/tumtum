package cc.tumtum.app.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.tumtum.app.AppContainer
import cc.tumtum.app.data.repo.LiveSnapshot
import cc.tumtum.app.domain.EventSession
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * a2 — Captura ao vivo. O app está só marcando a janela: nada de sensor em
 * tempo real, só um lote retroativo por minuto para o estado calmo (§2).
 */
class LiveViewModel(private val container: AppContainer) : ViewModel() {

    val activeEvent: StateFlow<EventSession?> = container.nights.activeEvent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _snapshot = MutableStateFlow<LiveSnapshot?>(null)
    val snapshot: StateFlow<LiveSnapshot?> = _snapshot

    private val _now = MutableStateFlow(Instant.now())
    val now: StateFlow<Instant> = _now

    private val _ending = MutableStateFlow(false)
    val ending: StateFlow<Boolean> = _ending

    private val _permissionRevoked = MutableStateFlow(false)
    val permissionRevoked: StateFlow<Boolean> = _permissionRevoked

    init {
        // Cronômetro (1s) — só relógio de parede, nenhuma leitura.
        viewModelScope.launch {
            while (true) {
                _now.value = Instant.now()
                delay(1_000)
            }
        }
        // Lote retroativo por minuto, apenas com evento ativo.
        viewModelScope.launch {
            while (true) {
                val event = activeEvent.value
                if (event != null) {
                    _permissionRevoked.value = !container.health.hasPermission()
                    if (!_permissionRevoked.value) {
                        _snapshot.value = container.nights.liveSnapshot(event)
                    }
                }
                delay(60_000)
            }
        }
    }

    /** Cria o evento e, com sensor pareado, deixa a sessão de captura registrada (§4.3). */
    suspend fun startEvent(name: String, venue: String, eventType: String = "concert", serverEventId: String? = null): Long {
        val eventId = container.nights.startEvent(name, venue, eventType, serverEventId)
        _snapshot.value = null
        if (container.prefs.state.first().sensorPaired) {
            container.prefs.setActiveCapture(eventId)
        }
        return eventId
    }

    /** What the last tap did, so the screen can say it: stored at [at], or repeated (nothing stored). */
    data class MarkFeedback(val id: Long?, val label: String, val at: Instant, val repeated: Boolean)

    private val _lastMark = MutableStateFlow<MarkFeedback?>(null)
    val lastMark: StateFlow<MarkFeedback?> = _lastMark

    /**
     * One tap during the capture (Etapa 3): the goal, the song, the moment.
     * The clock of the tap is the whole point — it becomes a timeline entry
     * on the server and names the moment the detector finds around it.
     *
     * A second tap of the same kind within [REPEAT_WINDOW] is the same
     * moment, not a new one: the rehearsal of 18/09 produced 31 marks from
     * one person pressing until something visibly changed. Nothing is stored
     * for it, and the screen says so.
     */
    fun mark(label: String, entryType: String) {
        val event = activeEvent.value ?: return
        val now = Instant.now()
        val last = _lastMark.value
        if (last != null && last.label == label && Duration.between(last.at, now) < REPEAT_WINDOW) {
            _lastMark.value = last.copy(repeated = true)
            return
        }
        viewModelScope.launch {
            val id = container.nights.addMark(event.id, label, entryType, now)
            _lastMark.value = MarkFeedback(id, label, now, repeated = false)
        }
    }

    /** Undo the last tap. If the server already has it, it stays and the line keeps saying so. */
    fun undoLastMark() {
        val last = _lastMark.value ?: return
        val id = last.id ?: return
        viewModelScope.launch {
            if (container.nights.removeMark(id)) _lastMark.value = null
        }
    }

    private companion object {
        val REPEAT_WINDOW: Duration = Duration.ofSeconds(10)
    }

    /**
     * Encerrar a noite: fecha a janela, mede densidade por fonte e deixa a
     * decisão visível em b4 (§7) — nada é escolhido escondido.
     */
    fun endNight(onMeasured: () -> Unit) {
        val event = activeEvent.value ?: return
        if (_ending.value) return
        _ending.value = true
        viewModelScope.launch {
            val endedAt = Instant.now()
            container.nights.closeEvent(event.id, endedAt)
            val closed = event.copy(endAt = endedAt)
            container.endNight.event = closed
            container.endNight.measurement = container.nights.measureSources(closed, endedAt)
            _ending.value = false
            onMeasured()
        }
    }
}
