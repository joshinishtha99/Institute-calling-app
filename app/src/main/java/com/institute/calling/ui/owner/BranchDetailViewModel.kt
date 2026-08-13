package com.institute.calling.ui.owner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.institute.calling.domain.model.BranchSummary
import com.institute.calling.domain.repository.CallingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BranchDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val summary: BranchSummary? = null,
)

@HiltViewModel
class BranchDetailViewModel @Inject constructor(
    private val repository: CallingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val branchId: String = savedStateHandle.get<String>("branchId").orEmpty()

    private val _state = MutableStateFlow(BranchDetailUiState())
    val state: StateFlow<BranchDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val summary = repository.getBranchSummary(branchId, null)
                _state.update { it.copy(loading = false, summary = summary) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Couldn't load this branch.") }
            }
        }
    }
}
