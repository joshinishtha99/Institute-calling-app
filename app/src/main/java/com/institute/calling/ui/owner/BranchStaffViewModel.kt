package com.institute.calling.ui.owner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.StaffMember
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BranchStaffUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val submitting: Boolean = false,
    val branchName: String = "",
    val staff: List<StaffMember> = emptyList(),
)

@HiltViewModel
class BranchStaffViewModel @Inject constructor(
    private val repository: CallingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val branchId: String = savedStateHandle.get<String>("branchId").orEmpty()

    private val _state = MutableStateFlow(BranchStaffUiState())
    val state: StateFlow<BranchStaffUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val data = repository.getBranchStaff(branchId)
                _state.update { it.copy(loading = false, branchName = data.branchName, staff = data.staff) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Couldn't load staff. Check the connection and retry.") }
            }
        }
    }

    fun addCaller(name: String, pin: String) = submit("Caller \"$name\" added (PIN $pin)") {
        repository.addCaller(branchId, name, pin)
    }

    fun rename(callerId: String, name: String) = submit("Renamed to \"$name\"") {
        repository.updateCaller(callerId, name = name, pin = null, isActive = null)
    }

    fun resetPin(callerId: String, pin: String) = submit("PIN reset to $pin") {
        repository.updateCaller(callerId, name = null, pin = pin, isActive = null)
    }

    fun setActive(callerId: String, active: Boolean) =
        submit(if (active) "Caller reactivated" else "Caller deactivated") {
            repository.updateCaller(callerId, name = null, pin = null, isActive = active)
        }

    private fun submit(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                action()
                _events.emit(successMessage)
                val data = repository.getBranchStaff(branchId)
                _state.update { it.copy(branchName = data.branchName, staff = data.staff) }
            } catch (e: Exception) {
                _events.emit("Couldn't save — check the connection and try again")
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }
}
