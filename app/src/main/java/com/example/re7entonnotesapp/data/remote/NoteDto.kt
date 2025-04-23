package com.example.re7entonnotesapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: Long,
    val title: String,
    val content: String,
    val updatedAt: Long
)