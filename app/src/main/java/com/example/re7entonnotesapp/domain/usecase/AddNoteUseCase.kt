package com.example.re7entonnotesapp.domain.usecase


import com.example.re7entonnotesapp.domain.model.Note
import com.example.re7entonnotesapp.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repo.addNote(note)
}