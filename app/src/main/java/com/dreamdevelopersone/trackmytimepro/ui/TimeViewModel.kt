package com.dreamdevelopersone.trackmytimepro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamdevelopersone.trackmytimepro.data.TaskRepository
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.delay
import com.dreamdevelopersone.trackmytimepro.util.*
import android.content.Context
import android.net.Uri

enum class FilterType(val label: String) {
    TODAY("Today"), WEEK("Week"), MONTH("Month"), YEAR("Year"), ALL("Lifetime")
}

@HiltViewModel
class TimeViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FilterType.TODAY)
    val selectedFilter = _selectedFilter.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<TaskSession>> = _selectedFilter
        .flatMapLatest { filter ->
            val minTs = when (filter) {
                FilterType.TODAY -> getStartOfDay()
                FilterType.WEEK -> getStartOfWeekMonday()
                FilterType.MONTH -> getStartOfMonth()
                FilterType.YEAR -> getStartOfYear()
                FilterType.ALL -> 0L
            }
            repository.getSessionsAfter(minTs)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskStats: StateFlow<Map<String, Long>> = sessions
        .map { list -> 
            list.groupBy { it.name }
                .mapValues { entry -> entry.value.sumOf { it.duration() } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val suggestions: StateFlow<List<String>> = repository.getUniqueTaskNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _taskName = MutableStateFlow("")
    val taskName = _taskName.asStateFlow()

    private val _activeStartTime = MutableStateFlow<Long?>(null)
    val activeStartTime = _activeStartTime.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMs = _currentTimeMs.asStateFlow()

    val activeDuration: StateFlow<Long> = combine(_activeStartTime, _currentTimeMs) { start, current ->
        if (start != null) current - start else 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private var tickerJob: Job? = null

    fun onTaskNameChange(newName: String) {
        _taskName.value = newName
    }

    fun onFilterChange(filter: FilterType) {
        _selectedFilter.value = filter
    }

    fun startTask() {
        if (_taskName.value.isNotBlank() && _activeStartTime.value == null) {
            val now = System.currentTimeMillis()
            _activeStartTime.value = now
            _currentTimeMs.value = now
            
            tickerJob?.cancel()
            tickerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _currentTimeMs.value = System.currentTimeMillis()
                }
            }
        }
    }

    fun stopTask() {
        val start = _activeStartTime.value
        val name = _taskName.value
        if (start != null && name.isNotBlank()) {
            tickerJob?.cancel()
            viewModelScope.launch {
                repository.insertSession(
                    TaskSession(
                        name = name,
                        startTime = start,
                        endTime = System.currentTimeMillis()
                    )
                )
                _activeStartTime.value = null
                _taskName.value = ""
            }
        }
    }

    fun deleteSession(session: TaskSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun updateSessionEndTime(session: TaskSession, newEndTime: Long) {
        viewModelScope.launch {
            repository.updateSession(session.copy(endTime = newEndTime))
        }
    }

    fun exportAllData(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val allSessions = repository.getAllSessions().first()
                val success = DataBackupManager.exportToUri(context, uri, allSessions)
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun importAllData(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val importedSessions = DataBackupManager.importFromUri(context, uri)
            if (importedSessions != null) {
                importedSessions.forEach {
                    // Reset IDs to 0 to treat them as new insertions and avoid conflicts with auto-gen
                    repository.insertSession(it.copy(id = 0))
                }
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun exportPdfReport(context: Context, filter: FilterType, onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            val minTs = when (filter) {
                FilterType.TODAY -> getStartOfDay()
                FilterType.WEEK -> getStartOfWeekMonday()
                FilterType.MONTH -> getStartOfMonth()
                FilterType.YEAR -> getStartOfYear()
                FilterType.ALL -> 0L
            }
            val filteredSessions = repository.getSessionsAfter(minTs).first()
            val uri = exportPDF(context, filteredSessions, filter.label)
            onResult(uri)
        }
    }
}
