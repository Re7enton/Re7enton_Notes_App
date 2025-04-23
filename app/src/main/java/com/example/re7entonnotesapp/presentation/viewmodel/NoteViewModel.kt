package com.example.re7entonnotesapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.data.local.NoteEntity
import com.example.re7entonnotesapp.data.repository.`NoteRepositoryImpl.kt`
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: `NoteRepositoryImpl.kt`
) : ViewModel() {

    // Expose notes as a StateFlow to be observed in the UI.
    val notes: StateFlow<List<NoteEntity>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Add a note
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insert(NoteEntity(title = title, content = content))
        }
    }

    // Delete a note
    fun deleteNote(noteEntity: NoteEntity) {
        viewModelScope.launch {
            repository.delete(noteEntity)
        }
    }

    // Update a note with a timestamp
    fun updateNote(noteEntity: NoteEntity) {
        viewModelScope.launch {
            repository.update(noteEntity.copy(lastEdited = System.currentTimeMillis()))
        }
    }

//    Syncing notes with a server
    fun syncNotes() = viewModelScope.launch {
        repository.syncNotesWithServer()
    }
    fun fetchNotes() = viewModelScope.launch {
        repository.fetchNotesFromServerAndUpdateDb()
    }
}
