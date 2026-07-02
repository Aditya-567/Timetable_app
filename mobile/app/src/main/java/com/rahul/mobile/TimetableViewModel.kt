package com.rahul.mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahul.mobile.data.TimetableRepository
import com.rahul.mobile.data.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimetableViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(loading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val data = repository.loadTimetable()
                

            _uiState.value = UiState(
                loading = false,
                items = data.items,
                events = data.events,
                sections = listOf("All") + data.sections,
               weather = data.weather, 
                error = null,
                lastSync = repository.formatLastSync()
            )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load timetable"
                )
            }
        }
    }
}
