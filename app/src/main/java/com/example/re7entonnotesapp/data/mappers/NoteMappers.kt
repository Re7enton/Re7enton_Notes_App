package com.example.re7entonnotesapp.data.mappers

import com.example.re7entonnotesapp.data.local.NoteEntity
import com.example.re7entonnotesapp.data.remote.NoteDto
import com.example.re7entonnotesapp.domain.model.Note


// Entity → Domain
fun NoteEntity.toDomain(): Note =
    Note(id, title, content, updatedAt)

// Domain → Entity
fun Note.toEntity(): NoteEntity =
    NoteEntity(id = id, title = title, content = content, updatedAt = updatedAt)

// DTO → Entity
fun NoteDto.toEntity(): NoteEntity =
    NoteEntity(id = id, title = title, content = content, updatedAt = updatedAt)

// Entity → DTO
fun NoteEntity.toDto(): NoteDto =
    NoteDto(id = id, title = title, content = content, updatedAt = updatedAt)

// Domain → DTO (if needed)
fun Note.toDto(): NoteDto =
    NoteDto(id = id, title = title, content = content, updatedAt = updatedAt)