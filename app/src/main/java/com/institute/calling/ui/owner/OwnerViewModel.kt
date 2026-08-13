package com.institute.calling.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.BranchSummary
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val total: Int = 0,
    val branches: List<BranchSummary> = emptyList(),
)

@HiltViewModel
class OwnerViewModel @Inject constructor(
    private val repository: CallingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerUiState())
    val state: StateFlow<OwnerUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                repository.refreshStructure()
                val branches = repository.observeCities().first().flatMap { it.branches }
                val summaries = branches.map { repository.getBranchSummary(it.id, null) }
                _state.update {
                    it.copy(
                        loading = false,
                        branches = summaries,
                        total = summaries.sumOf { s -> s.total },
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Couldn't load branch data. Pull to retry.") }
            }
        }
    }
}
