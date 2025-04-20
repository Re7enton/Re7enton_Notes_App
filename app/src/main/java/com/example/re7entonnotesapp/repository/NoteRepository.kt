package com.example.re7entonnotesapp.repository

import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.data.NoteDao
import com.example.re7entonnotesapp.data.mappers.toDto
import com.example.re7entonnotesapp.data.mappers.toEntity
import com.example.re7entonnotesapp.data.network.NoteApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject



// Repository exposing a clean API for data operations.
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val api: NoteApi
) {

//    Local methods:
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.update(note)
    suspend fun delete(note: Note) = noteDao.delete(note)

//    Remote methods:
suspend fun syncNotesWithServer() {
    noteDao.getAllNotes().first().forEach {
        api.postNote(it.toDto())
    }
}

    suspend fun fetchNotesFromServerAndUpdateDb() {
        api.fetchNotes().forEach {
            noteDao.insert(it.toEntity())
        }
    }
}