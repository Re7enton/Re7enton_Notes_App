package com.example.re7entonnotesapp.data.repository


import android.util.Log
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

private const val TAG = "NoteRepositoryImpl"

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val api: NotesApi
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> =
        dao.observeAllNotes()
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addNote(note: Note) {
        val ent = note.toEntity()
        dao.insertNote(ent)
        api.addNote(ent.toDto())
    }

    override suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
        api.deleteNote(id)
    }

    override suspend fun syncNotes() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting syncNotes()")

        // 1) fetch remote + local
        val remote: List<NoteDto> = api.getNotes()
        val localEntities: List<NoteEntity> = dao.getAllNotesOnce()
        val local: List<NoteDto> = localEntities.map { it.toDto() }

        Log.d(TAG, "Remote has ${remote.size} notes; local has ${localEntities.size}")

        // ── EARLY‑OUT: fresh install → pull remote only ─────
        if (localEntities.isEmpty() && remote.isNotEmpty()) {
            Log.d(TAG, "Fresh install detected: pulling ${remote.size} remote notes into local DB")
            remote.forEach { dto ->
                dao.insertNote(dto.toEntity())
            }
            return@withContext
        }

        // 2) index by id
        val remoteById = remote.associateBy { it.id }
        val localById  = local.associateBy  { it.id }

        // 3) compute diffs
        val allIds = remoteById.keys + localById.keys

        val toPushUp   = mutableListOf<NoteDto>()
        val toPullDown = mutableListOf<NoteDto>()
        val toDeleteLocal = mutableListOf<Long>()
        val toDeleteRemote = mutableListOf<Long>()

        for (id in allIds) {
            val r = remoteById[id]
            val l = localById[id]

            when {
                // created locally (never existed remotely)
                r == null && l != null -> {
                    Log.d(TAG, "Note $id created locally → will push up")
                    toPushUp += l
                }
                // created remotely (never existed locally)
                l == null && r != null -> {
                    Log.d(TAG, "Note $id created remotely → will pull down")
                    toPullDown += r
                }
                // exists both → compare timestamps
                l != null && r != null -> {
                    if (l.updatedAt > r.updatedAt) {
                        Log.d(TAG, "Note $id updated locally (ts ${l.updatedAt} > ${r.updatedAt}) → push up")
                        toPushUp += l
                    } else if (r.updatedAt > l.updatedAt) {
                        Log.d(TAG, "Note $id updated remotely (ts ${r.updatedAt} > ${l.updatedAt}) → pull down")
                        toPullDown += r
                    }
                }
            }
        }

        // 4) apply pushes
        toPushUp.forEach { dto ->
            api.addNote(dto)
        }

        // 5) apply pulls into local
        toPullDown.forEach { dto ->
            dao.insertNote(dto.toEntity())
        }

        // 6) handle deletes: if remote no longer has something local once existed?
        //    (optional: you could track deletions separately; omitted here)

        Log.d(TAG, "syncNotes() complete: pushed ${toPushUp.size}, pulled ${toPullDown.size}")
    }
}