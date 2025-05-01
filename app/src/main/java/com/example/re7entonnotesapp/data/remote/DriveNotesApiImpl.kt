package com.example.re7entonnotesapp.data.remote

import android.content.Context
import com.example.re7entonnotesapp.domain.model.AuthState
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Implements your NotesApi interface by storing all notes in a single
 * notes.json inside the Drive appDataFolder.
 */
class DriveNotesApiImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authState: AuthState
) : NotesApi {

    companion object {
        private const val FILENAME = "notes.json"
        private const val MIME_JSON = "application/json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** Build Drive service using the current OAuth access token */
    private fun driveService(): Drive {
        // ① grab the current access token (will throw if null)
        val token = authState.driveAccessToken
            ?: throw IllegalStateException("Drive not authorized")

        // ② initializer that adds the Authorization header
        val initializer = HttpRequestInitializer { request ->
            request.headers.apply {
                authorization = "Bearer $token"
            }
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),                        // use GsonFactory, not JacksonFactory
            initializer
        )
            .setApplicationName("Re7entonNotesApp")
            .build()
    }

    /** Read the current list from notes.json (or return empty list) */
    private suspend fun loadAll(): MutableList<NoteDto> = withContext(Dispatchers.IO) {
        val drive = driveService()
        // list only in appDataFolder
        val list: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id,name)")
            .execute()
        val file: File? = list.files.firstOrNull { it.name == FILENAME }
        if (file == null) return@withContext mutableListOf()

        val out = ByteArrayOutputStream()
        drive.files().get(file.id).executeMediaAndDownloadTo(out)
        val text = out.toString("UTF-8")
        json.decodeFromString(ListSerializer(NoteDto.serializer()), text).toMutableList()
    }

    /** Write the given list back to notes.json (create or update) */
    private suspend fun saveAll(all: List<NoteDto>) = withContext(Dispatchers.IO) {
        val drive = driveService()
        val bytes = json.encodeToString(ListSerializer(NoteDto.serializer()), all)
            .toByteArray(Charsets.UTF_8)
        val content = ByteArrayContent(MIME_JSON, bytes)

        val list = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id,name)")
            .execute()
        val existing = list.files.firstOrNull { it.name == FILENAME }

        if (existing != null) {
            drive.files().update(existing.id, File().setName(FILENAME), content).execute()
        } else {
            val meta = File().setName(FILENAME).setParents(listOf("appDataFolder"))
            drive.files().create(meta, content).execute()
        }
    }

    // ─── NotesApi methods ─────────────────────────────────────────────────────

    /** Return all remote notes */
    override suspend fun getNotes(): List<NoteDto> {
        return loadAll()
    }

    /** Add one note remotely (keeps others intact) */
    override suspend fun addNote(note: NoteDto) {
        val list = loadAll().filter { it.id != note.id }.toMutableList()   // remove old version
        list.add(note)
        saveAll(list)
    }

    /** Delete by id remotely */
    override suspend fun deleteNote(id: Long) {
        val list = loadAll()
        list.removeAll { it.id == id }
        saveAll(list)
    }
}