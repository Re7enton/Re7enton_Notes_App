package com.example.re7entonnotesapp.data.mappers

import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.data.network.dto.NoteDto

fun Note.toDto() = NoteDto(id, title, content, timestamp, lastEdited)
fun NoteDto.toEntity() = Note(id, title, content, timestamp, lastEdited)