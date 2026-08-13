package com.institute.calling.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.AuthUser
import com.institute.calling.domain.model.Branch
import com.institute.calling.domain.model.City
import com.institute.calling.domain.model.Owner
import com.institute.calling.domain.model.Role
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the login/session flow. */
data class SessionState(
    val cities: List<City> = emptyList(),
    val owners: List<Owner> = emptyList(),
    val loadingStructure: Boolean = true,
    val structureError: String? = null,
    val selectedCity: City? = null,
    val selectedBranch: Branch? = null,
    val pendingCallerId: String? = null,
    val pendingCallerName: String? = null,
    val pendingOwnerId: String? = null,
    val pendingOwnerName: String? = null,
    val pendingRole: Role? = null,
    val authenticating: Boolean = false,
    val authError: String? = null,
    val currentUser: AuthUser? = null,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: CallingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val citiesFlow = repository.observeCities()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            citiesFlow.collect { cities ->
                _state.update { it.copy(cities = cities) }
            }
        }
        loadStructure()
    }

    /** Fetch the structure and owners from the backend. */
    fun loadStructure() {
        viewModelScope.launch {
            _state.update { it.copy(loadingStructure = true, structureError = null) }
            try {
                repository.refreshStructure()
                val owners = repository.getOwners()
                _state.update { it.copy(owners = owners, loadingStructure = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingStructure = false,
                        structureError = "Couldn't reach the server. Check it's running and try again.",
                    )
                }
            }
        }
    }

    // --- Employee hierarchy navigation selections ---
    fun selectCity(city: City) = _state.update { it.copy(selectedCity = city, selectedBranch = null) }

    fun selectBranch(branch: Branch) = _state.update { it.copy(selectedBranch = branch) }

    fun selectCaller(callerId: String, callerName: String) = _state.update {
        it.copy(pendingCallerId = callerId, pendingCallerName = callerName, pendingRole = Role.EMPLOYEE, authError = null)
    }

    fun selectOwner(owner: Owner) = _state.update {
        it.copy(pendingOwnerId = owner.id, pendingOwnerName = owner.name, pendingRole = Role.OWNER, authError = null)
    }

    /**
     * Attempt login with the entered PIN. [onSuccess] receives the resolved role
     * so the caller can route to the correct destination.
     */
    fun login(pin: String, onSuccess: (Role) -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(authenticating = true, authError = null) }
            try {
                val user: AuthUser? = when (s.pendingRole) {
                    Role.OWNER -> s.pendingOwnerId?.let { repository.authenticateOwner(it, pin) }
                    Role.EMPLOYEE -> s.pendingCallerId?.let { repository.authenticateCaller(it, pin) }
                    null -> null
                }
                if (user == null) {
                    _state.update { it.copy(authenticating = false, authError = "Login failed. Check your PIN.") }
                } else {
                    _state.update { it.copy(authenticating = false, currentUser = user) }
                    onSuccess(user.role)
                }
            } catch (e: Exception) {
                _state.update { it.copy(authenticating = false, authError = "Couldn't reach the server. Try again.") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.clearSession() }
        _state.update {
            SessionState(cities = it.cities, owners = it.owners, loadingStructure = false)
        }
    }
}
