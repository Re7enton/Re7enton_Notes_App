package com.example.re7entonnotesapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity(tableName = "notes")
@Serializable
data class NoteEntity(
        // We assign our own IDs (timestamps), so autoGenerate must be false
        @PrimaryKey
    val id: Long,
    val title: String,
    val content: String,
    val updatedAt: Long = Instant.now().toEpochMilli()
)