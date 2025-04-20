package com.example.re7entonnotesapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: Long,
    val lastEdited: Long
)