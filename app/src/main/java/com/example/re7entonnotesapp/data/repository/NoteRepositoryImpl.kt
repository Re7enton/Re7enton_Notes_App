package com.example.re7entonnotesapp.data.repository


import com.example.re7entonnotesapp.data.local.NoteDao
import com.example.re7entonnotesapp.data.mappers.toDto
import com.example.re7entonnotesapp.data.mappers.toEntity
import com.example.re7entonnotesapp.data.remote.NotesApi
import com.example.re7entonnotesapp.domain.model.Note
import com.example.re7entonnotesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.re7entonnotesapp.data.mappers.toDomain

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val api: NotesApi
) : NoteRepository {

    /** Emits local notes as domain models */
    override fun getNotes(): Flow<List<Note>> =
        dao.getAllNotes()                // use your existing method
            .map { list -> list.map { it.toDomain() } }

    /** Insert locally, then push to server */
    override suspend fun addNote(note: Note) {
        val entity = note.toEntity()
        dao.insertNote(entity)
        api.addNote(entity.toDto())
    }

    /** Remove locally, then delete remotely */
    override suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
        api.deleteNote(id)
    }

    /** Fetch remote, overwrite local */
    override suspend fun syncNotes() {
        val remoteDtos = api.getNotes()
        val remoteEntities = remoteDtos.map { it.toEntity() }
        remoteEntities.forEach { dao.insertNote(it) }
          }
}