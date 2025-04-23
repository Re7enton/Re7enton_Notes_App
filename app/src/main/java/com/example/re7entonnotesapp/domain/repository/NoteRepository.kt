package com.example.re7entonnotesapp.domain.repository

import com.example.re7entonnotesapp.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun syncNotes()
}