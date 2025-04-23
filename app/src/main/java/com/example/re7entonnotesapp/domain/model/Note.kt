package com.example.re7entonnotesapp.domain.model

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val updatedAt: Long
)