package com.example.re7entonnotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    // Expose notes as a StateFlow to be observed in the UI.
    val notes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Add a note
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insert(Note(title = title, content = content))
        }
    }

    // Delete a note
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    // Update a note
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.update(note.copy(lastEdited = System.currentTimeMillis()))
        }
    }
}
