package com.example.re7entonnotesapp.data.mappers

import com.example.re7entonnotesapp.data.local.NoteEntity
import com.example.re7entonnotesapp.data.remote.NoteDto

fun NoteEntity.toDto() = NoteDto(id, title, content, timestamp, lastEdited)
fun NoteDto.toEntity() = NoteEntity(id, title, content, timestamp, lastEdited)