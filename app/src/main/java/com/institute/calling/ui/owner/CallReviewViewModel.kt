package com.institute.calling.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.CallLogEntry
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class BranchGroup(
    val branchId: String,
    val branchName: String,
    val total: Int,
    val calls: List<CallLogEntry>,
)

data class CityGroup(
    val cityName: String,
    val total: Int,
    val branches: List<BranchGroup>,
)

/** Which drill-down level is showing. */
enum class ReviewLevel { CITIES, BRANCHES, CALLS }

data class CallReviewUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val dateMillis: Long = todayUtcMillis(),
    val cities: List<CityGroup> = emptyList(),
    val selectedCity: String? = null,
    val selectedBranchId: String? = null,
) {
    val level: ReviewLevel
        get() = when {
            selectedBranchId != null -> ReviewLevel.CALLS
            selectedCity != null -> ReviewLevel.BRANCHES
            else -> ReviewLevel.CITIES
        }

    val currentCity: CityGroup? get() = cities.firstOrNull { it.cityName == selectedCity }
    val currentBranch: BranchGroup? get() = currentCity?.branches?.firstOrNull { it.branchId == selectedBranchId }
    val grandTotal: Int get() = cities.sumOf { it.total }
}

/** Midnight (UTC) of today, in millis — used as the default selected date. */
private fun todayUtcMillis(): Long {
    val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@HiltViewModel
class CallReviewViewModel @Inject constructor(
    private val repository: CallingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CallReviewUiState())
    val state: StateFlow<CallReviewUiState> = _state.asStateFlow()

    init {
        load(_state.value.dateMillis)
    }

    fun onDateSelected(millis: Long) {
        _state.update { it.copy(dateMillis = millis, selectedCity = null, selectedBranchId = null) }
        load(millis)
    }

    fun openCity(cityName: String) = _state.update { it.copy(selectedCity = cityName, selectedBranchId = null) }
    fun openBranch(branchId: String) = _state.update { it.copy(selectedBranchId = branchId) }

    /** Go up one level; returns false if already at the top (so the screen can exit). */
    fun goUp(): Boolean {
        val s = _state.value
        return when {
            s.selectedBranchId != null -> { _state.update { it.copy(selectedBranchId = null) }; true }
            s.selectedCity != null -> { _state.update { it.copy(selectedCity = null) }; true }
            else -> false
        }
    }

    fun reload() = load(_state.value.dateMillis)

    private fun load(millis: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                repository.refreshStructure()
                val structure = repository.observeCities().first()
                val calls = repository.getCalls(ymd(millis))
                val callsByBranch = calls.groupBy { it.branchId }

                val cities = structure.map { city ->
                    val branchGroups = city.branches.map { branch ->
                        val bc = callsByBranch[branch.id].orEmpty()
                        BranchGroup(branch.id, branch.name, bc.size, bc)
                    }.sortedByDescending { it.total }
                    CityGroup(city.name, branchGroups.sumOf { it.total }, branchGroups)
                }.sortedByDescending { it.total }

                _state.update { it.copy(loading = false, cities = cities) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Couldn't load calls. Check the server and retry.") }
            }
        }
    }

    companion object {
        private val ymdFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun ymd(millis: Long): String = ymdFormat.format(Date(millis))
    }
}
