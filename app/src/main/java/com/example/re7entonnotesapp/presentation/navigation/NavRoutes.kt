package com.example.re7entonnotesapp.presentation.navigation

object NavRoutes {
    const val NOTE_LIST = "note_list"
    const val NOTE_DETAIL = "note_detail"
    const val ARG_NOTE_ID = "noteId"
    fun noteDetailRoute(id: Long) = "$NOTE_DETAIL/$id"
}