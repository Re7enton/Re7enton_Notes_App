package com.example.re7entonnotesapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: Long,
    val lastEdited: Long
)