package com.example.re7entonnotesapp.data.network

import com.example.re7entonnotesapp.data.network.dto.NoteDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class NoteApi(private val client: HttpClient) {

    private val baseUrl = "http://10.0.2.2:8080/notes"

    suspend fun fetchNotes(): List<NoteDto> =
        client.get(baseUrl).body()

    suspend fun postNote(dto: NoteDto): NoteDto =
        client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updateNote(dto: NoteDto): NoteDto =
        client.put("$baseUrl/${dto.id}") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun deleteNote(id: Int) {
        client.delete("$baseUrl/$id")
    }
}