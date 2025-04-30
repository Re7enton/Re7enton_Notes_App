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
        // 1) load both sides
        val remote: List<NoteDto>   = api.getNotes()
        val localEntities: List<NoteEntity> = dao.getAllNotesOnce()
        val local:  List<NoteDto>   = localEntities.map { it.toDto() }

        // 2) index by id
        val remoteById = remote.associateBy { it.id }
        val localById  = local.associateBy  { it.id }

        // 3) compute sets
        val allIds    = (remoteById.keys + localById.keys)
        val toAddOrUpdateRemotely = mutableListOf<NoteDto>()
        val toDeleteRemotely     = mutableListOf<Long>()
        val toAddOrUpdateLocally = mutableListOf<NoteDto>()
        // no method to delete locally by id? use dao.deleteNoteById(id)

        for (id in allIds) {
            val r = remoteById[id]
            val l = localById[id]

            when {
                r == null && l != null ->            // created locally
                    toAddOrUpdateRemotely += l

                l == null && r != null ->            // deleted locally
                    toDeleteRemotely += id

                l != null && r != null -> {          // exists both → compare timestamps
                    if (l.updatedAt > r.updatedAt) {
                        toAddOrUpdateRemotely += l       // local is newer → push up
                    } else if (r.updatedAt > l.updatedAt) {
                        toAddOrUpdateLocally += r        // remote is newer → pull down
                    }
                }
            }
        }

        // 4) apply remote changes
        toAddOrUpdateRemotely.forEach { api.addNote(it) }
        toDeleteRemotely.forEach     { api.deleteNote(it) }

        // 5) apply local updates
        //    create/update locally
        toAddOrUpdateLocally.forEach { dto ->
            dao.insertNote(dto.toEntity())        // REPLACE strategy updates existing row
        }
        //    delete locally
        toDeleteRemotely.forEach { id ->
            dao.deleteNoteById(id)
        }
    }
}