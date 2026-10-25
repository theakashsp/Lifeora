package com.liferise.app.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.liferise.app.data.repository.DiaryRepository
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMoodFilter = MutableStateFlow<Mood?>(null)
    val selectedMoodFilter: StateFlow<Mood?> = _selectedMoodFilter.asStateFlow()

    val entries: StateFlow<List<DiaryEntry>> = combine(_searchQuery, _selectedMoodFilter) { query, mood ->
        Pair(query, mood)
    }.flatMapLatest { (query, mood) ->
        val flow = if (query.isBlank()) {
            repository.getAllEntries()
        } else {
            repository.searchEntries(query)
        }
        flow.map { list ->
            if (mood != null) list.filter { it.mood == mood } else list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMoodFilter(mood: Mood?) {
        _selectedMoodFilter.value = if (_selectedMoodFilter.value == mood) null else mood
    }

    suspend fun getEntryById(id: Long): DiaryEntry? {
        return repository.getEntryById(id)
    }

    fun saveEntry(
        id: Long = 0,
        title: String,
        content: String,
        mood: Mood,
        tags: List<String>,
        imageUris: List<String> = emptyList(),
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entry = DiaryEntry(
                id = id,
                title = title.trim(),
                content = content.trim(),
                mood = mood,
                tags = tags,
                imageUris = imageUris,
                createdAt = if (id == 0L) now else now,
                updatedAt = now
            )
            val resultId = if (id == 0L) {
                repository.createEntry(entry)
            } else {
                repository.updateEntry(entry)
                id
            }
            onSuccess(resultId)
        }
    }

    fun deleteEntry(id: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEntry(id)
            onDone()
        }
    }

    companion object {
        fun provideFactory(repository: DiaryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiaryViewModel(repository) as T
                }
            }
    }
}
