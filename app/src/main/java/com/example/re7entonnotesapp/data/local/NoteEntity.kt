package com.example.re7entonnotesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity(tableName = "notes")
@Serializable
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val updatedAt: Long = Instant.now().toEpochMilli()
)