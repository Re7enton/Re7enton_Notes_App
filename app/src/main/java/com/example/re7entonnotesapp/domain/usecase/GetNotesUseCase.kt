package com.example.re7entonnotesapp.domain.usecase


import com.example.re7entonnotesapp.domain.model.Note
import com.example.re7entonnotesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repo.getNotes()
}