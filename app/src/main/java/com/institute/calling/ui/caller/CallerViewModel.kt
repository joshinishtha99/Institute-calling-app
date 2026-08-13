package com.institute.calling.ui.caller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.data.phone.CallController
import com.institute.calling.domain.PhoneNumber
import com.institute.calling.domain.model.AuthUser
import com.institute.calling.domain.model.CallRecord
import com.institute.calling.domain.model.Disposition
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The phase of the caller screen. */
enum class CallPhase { IDLE, CALLING, CONNECTED, DISPOSITION }

data class CallerUiState(
    val number: String = "",
    val phase: CallPhase = CallPhase.IDLE,
    val elapsedSeconds: Int = 0,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val selectedDisposition: Disposition? = null,
    val notes: String = "",
    val callsToday: Int = 0,
    val saving: Boolean = false,
) {
    val canCall: Boolean get() = PhoneNumber.isValid(number)
    val hasInvalidNumber: Boolean get() = number.isNotBlank() && !PhoneNumber.isValid(number)
    val canSave: Boolean get() = selectedDisposition != null && !saving
}

@HiltViewModel
class CallerViewModel @Inject constructor(
    private val repository: CallingRepository,
    private val callController: CallController,
) : ViewModel() {

    private val _state = MutableStateFlow(CallerUiState())
    val state: StateFlow<CallerUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        // Real call lifecycle from the device.
        callController.onCallStarted = { start ->
            _state.update { it.copy(phase = CallPhase.CONNECTED, startTimeMillis = start) }
            startTicker()
        }
        callController.onCallEnded = { start, end ->
            timerJob?.cancel()
            _state.update {
                if (it.phase == CallPhase.DISPOSITION) it
                else it.copy(
                    phase = CallPhase.DISPOSITION,
                    startTimeMillis = if (start == 0L) end else start,
                    endTimeMillis = end,
                )
            }
        }
    }

    fun onDigit(d: String) = _state.update {
        if (it.number.filter { c -> c.isDigit() }.length >= 13) it
        else it.copy(number = it.number + d)
    }

    fun onBackspace() = _state.update { it.copy(number = it.number.dropLast(1)) }

    fun onPasteSample() = _state.update { it.copy(number = "+91 98765 43210") }

    /** Place a real call. Permissions must already be granted (the screen requests them). */
    fun onCall() {
        val dial = PhoneNumber.normalizeIndianMobile(_state.value.number) ?: return
        // Stamp the start time now as a fallback. If the device reports the exact
        // connect time (OFF_HOOK), onCallStarted overrides this with the real value.
        // This guarantees a saved call always has a valid, current start time.
        val now = System.currentTimeMillis()
        _state.update { it.copy(phase = CallPhase.CALLING, startTimeMillis = now, endTimeMillis = 0L, elapsedSeconds = 0) }
        callController.placeCall(dial)
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var sec = 0
            while (true) {
                delay(1000)
                sec++
                _state.update { it.copy(elapsedSeconds = sec) }
            }
        }
    }

    /** Manual fallback if the state listener misses the end (rare, device-dependent). */
    fun onEndCall() {
        timerJob?.cancel()
        val now = System.currentTimeMillis()
        _state.update {
            if (it.phase == CallPhase.DISPOSITION) it
            else it.copy(
                phase = CallPhase.DISPOSITION,
                startTimeMillis = if (it.startTimeMillis == 0L) now else it.startTimeMillis,
                endTimeMillis = now,
            )
        }
    }

    fun onSelectDisposition(d: Disposition) = _state.update { it.copy(selectedDisposition = d) }

    fun onNotesChange(text: String) = _state.update { it.copy(notes = text) }

    fun onSave(user: AuthUser) {
        val s = _state.value
        val disposition = s.selectedDisposition ?: return
        val branchId = user.branchId ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            try {
                repository.logCall(
                    CallRecord(
                        callerId = user.id,
                        branchId = branchId,
                        phoneNumber = PhoneNumber.normalizeIndianMobile(s.number) ?: s.number,
                        startTimeMillis = s.startTimeMillis,
                        endTimeMillis = s.endTimeMillis,
                        disposition = disposition,
                        notes = s.notes,
                    ),
                )
                _events.emit("${disposition.label} · saved")
                _state.update { CallerUiState(callsToday = it.callsToday + 1) }
            } catch (e: Exception) {
                _events.emit("Couldn't save — check connection and try again")
                _state.update { it.copy(saving = false) }
            }
        }
    }
}