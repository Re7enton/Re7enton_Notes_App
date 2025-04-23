package com.example.re7entonnotesapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.domain.model.Note
import com.example.re7entonnotesapp.domain.usecase.AddNoteUseCase
import com.example.re7entonnotesapp.domain.usecase.DeleteNoteUseCase
import com.example.re7entonnotesapp.domain.usecase.GetNotesUseCase
import com.example.re7entonnotesapp.domain.usecase.SyncNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    getNotes: GetNotesUseCase,
    private val addNote: AddNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val syncNotes: SyncNotesUseCase
) : ViewModel() {

    /** Exposed to UI: current list of notes */
    val notes: StateFlow<List<Note>> =
        getNotes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** Called by UI to add or update a note */
    fun saveNote(title: String, content: String, id: Long = 0L) {
        val now = System.currentTimeMillis()
        val note = Note(id = id, title = title, content = content, updatedAt = now)
        viewModelScope.launch {
            addNote(note)
        }
    }

    /** Called by UI to delete a note */
    fun removeNote(id: Long) {
        viewModelScope.launch {
            deleteNote(id)
        }
    }

    /** Called by UI or WorkManager to sync with server */
    fun sync() {
        viewModelScope.launch {
            syncNotes()
        }
    }
}
