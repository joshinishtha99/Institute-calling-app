package com.institute.calling.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.City
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

data class ManagementUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val submitting: Boolean = false,
    val cities: List<City> = emptyList(),
)

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val repository: CallingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ManagementUiState())
    val state: StateFlow<ManagementUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeCities().collect { cities ->
                _state.update { it.copy(cities = cities) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                repository.refreshStructure()
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Couldn't load. Check the server and retry.") }
            }
        }
    }

    fun addCity(name: String) = submit("City \"$name\" added") { repository.addCity(name) }

    fun addBranch(cityId: String, name: String) = submit("Branch \"$name\" added") {
        repository.addBranch(cityId, name)
    }

    fun addCaller(branchId: String, name: String, pin: String) = submit("Caller \"$name\" added (PIN $pin)") {
        repository.addCaller(branchId, name, pin)
    }

    private fun submit(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                action()
                _events.emit(successMessage)
            } catch (e: Exception) {
                _events.emit("Couldn't save — check the connection and try again")
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }
}
