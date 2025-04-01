package com.example.re7entonnotesapp.repository

import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.data.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Repository exposing a clean API for data operations.
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.update(note)
    suspend fun delete(note: Note) = noteDao.delete(note)
}