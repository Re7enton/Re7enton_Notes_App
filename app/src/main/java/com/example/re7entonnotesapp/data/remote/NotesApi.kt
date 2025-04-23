package com.example.re7entonnotesapp.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

interface NotesApi {
    suspend fun getNotes(): List<NoteDto>
    suspend fun addNote(note: NoteDto)
    suspend fun deleteNote(id: Long)
}

class NotesApiImpl @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String
) : NotesApi {

    override suspend fun getNotes(): List<NoteDto> =
        client.get("$baseUrl/notes").body()

    override suspend fun addNote(note: NoteDto) {
        client.post("$baseUrl/notes") {
            setBody(note)
        }
    }

    override suspend fun deleteNote(id: Long) {
        client.delete("$baseUrl/notes/$id")
    }
}