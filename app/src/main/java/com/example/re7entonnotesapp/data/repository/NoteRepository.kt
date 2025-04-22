package com.example.re7entonnotesapp.data.repository

import com.example.re7entonnotesapp.data.local.NoteEntity
import com.example.re7entonnotesapp.data.local.NoteDao
import com.example.re7entonnotesapp.data.mappers.toDto
import com.example.re7entonnotesapp.data.mappers.toEntity
import com.example.re7entonnotesapp.data.remote.NoteApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject



// Repository exposing a clean API for data operations.
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val api: NoteApi
) {

//    Local methods:
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    suspend fun insert(noteEntity: NoteEntity) = noteDao.insert(noteEntity)
    suspend fun update(noteEntity: NoteEntity) = noteDao.update(noteEntity)
    suspend fun delete(noteEntity: NoteEntity) = noteDao.delete(noteEntity)

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