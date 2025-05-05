package com.example.re7entonnotesapp.data.remote

import android.util.Log
import com.example.re7entonnotesapp.presentation.AuthState
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream

private const val TAG = "DriveNotesApiImpl"

class DriveNotesApiImpl(
    private val authStateFlow: StateFlow<AuthState>
) : NotesApi {

    companion object {
        private const val FILENAME = "notes.json"
        private const val MIME_JSON = "application/json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** Build Drive client only when token available */
    private fun driveService(): Drive? {
        val token = authStateFlow.value.driveAccessToken
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "driveService: no driveAccessToken; user not authorized")
            return null
        }
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val initializer = HttpRequestInitializer { req ->
            req.headers.authorization = "Bearer $token"
        }
        return Drive.Builder(transport, GsonFactory(), initializer)
            .setApplicationName("Re7entonNotesApp")
            .build()
    }

    private suspend fun loadAll(): MutableList<NoteDto> = withContext(Dispatchers.IO) {
        val drive = driveService() ?: return@withContext mutableListOf()
        val list: FileList = drive.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id,name)")
            .execute()
        val file: File? = list.files.firstOrNull { it.name == FILENAME }
        if (file == null) return@withContext mutableListOf()
        val out = java.io.ByteArrayOutputStream()
        drive.files().get(file.id).executeMediaAndDownloadTo(out)
        val text = out.toString("UTF-8")
        json.decodeFromString(ListSerializer(NoteDto.serializer()), text).toMutableList()
    }

    private suspend fun saveAll(all: List<NoteDto>) = withContext(Dispatchers.IO) {
        val drive = driveService() ?: run {
            Log.e(TAG, "saveAll: Drive unavailable, skipping")
            return@withContext
        }
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
            Log.d(TAG, "saveAll: Updated notes.json on Drive (${all.size} items)")
        } else {
            drive.files().create(File().setName(FILENAME).setParents(listOf("appDataFolder")), content).execute()
            Log.d(TAG, "saveAll: Created notes.json on Drive (${all.size} items)")
        }
    }

    override suspend fun getNotes(): List<NoteDto> = loadAll()

    override suspend fun addNote(note: NoteDto) {
        // replace any same‑id then add
        val current = loadAll().filter { it.id != note.id }.toMutableList()
        current.add(note)
        Log.d(TAG, "addNote: writing ${current.size} notes after filtering id=${note.id}")
        saveAll(current)
    }

    override suspend fun deleteNote(id: Long) {
        val current = loadAll().filter { it.id != id }
        Log.d(TAG, "deleteNote: writing ${current.size} notes after removing id=$id")
        saveAll(current)
    }
}