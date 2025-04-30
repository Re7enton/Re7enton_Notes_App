package com.example.re7entonnotesapp.data.repository


import com.example.re7entonnotesapp.data.local.NoteDao
import com.example.re7entonnotesapp.data.local.NoteEntity
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
import com.example.re7entonnotesapp.data.remote.NoteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val api: NotesApi
) : NoteRepository {

    /** Emits local notes as domain models */
    override fun getNotes(): Flow<List<Note>> =
        dao.observeAllNotes()                // use your existing method
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

    /** Two-way sync using only getNotes(), addNote(), deleteNote() */
    override suspend fun syncNotes() = withContext(Dispatchers.IO) {
        // 1) fetch remote list
        val remote: List<NoteDto> = api.getNotes()

        // 2) fetch local list
        val localEntities: List<NoteEntity> = dao.getAllNotesOnce()
        val local: List<NoteDto> = localEntities.map { it.toDto() }

        // 3) decide additions & deletions
        val remoteIds = remote.map { it.id }.toSet()
        val localIds  = local .map { it.id }.toSet()

        // a) Notes present locally but missing remotely → add remote
        val toAddRemotely = local.filter { it.id !in remoteIds }
        // b) Notes present remotely but missing locally → delete remote
        val toDeleteRemotely = remote.filter { it.id !in localIds }

        // 4) apply remote changes
        toAddRemotely.forEach { api.addNote(it) }
        toDeleteRemotely.forEach { api.deleteNote(it.id) }

        // 5) merge “last‐modified wins” for updates:
        //    For simplicity, treat every note present in both as “up to date” locally,
        //    since my Drive implementation always overwrites the full JSON on each addNote().
        //    If I need per-field updates, I could extend your API with an updateNote().

        // 6) refresh local database to match remote master list
        val finalRemote: List<NoteDto> = api.getNotes()
        dao.clearAndInsert(finalRemote.map { it.toEntity() })
    }
}