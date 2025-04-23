package com.example.re7entonnotesapp.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NoteListScreen(
    notes: StateFlow<List<Note>>,
    onAddNote: () -> Unit,
    onEditNote: (Long) -> Unit
) {
    val noteList by notes.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_note))
            }}) { paddingValues ->
            if (noteList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_notes),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(noteList) { note ->
                        NoteListItem(note, onClick = { onEditNote(note.id) })
                        HorizontalDivider()
                    }
                }
            }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            note.title,
            style = MaterialTheme.typography.titleMedium        // a bit smaller than titleLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            note.content,
            modifier = Modifier.size(100.dp),
            style = MaterialTheme.typography.bodyMedium,

        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteListScreenPreview() {
    val sampleNotes = listOf(
        Note(1L, "First", "Content of first note", System.currentTimeMillis()),
        Note(2L, "Second", "Another note here", System.currentTimeMillis())
    )
    val notesFlow = MutableStateFlow(sampleNotes)
    NoteListScreen(
        notes = notesFlow,
        onAddNote = {},
        onEditNote = {}
    )
}