package com.example.re7entonnotesapp.domain.usecase

import com.example.re7entonnotesapp.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    suspend operator fun invoke(id: Long) = repo.deleteNote(id)
}