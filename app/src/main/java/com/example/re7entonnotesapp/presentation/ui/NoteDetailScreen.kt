package com.example.re7entonnotesapp.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    notesFlow: StateFlow<List<Note>>,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val notes by notesFlow.collectAsState()
    val existing = remember(notes) { notes.find { it.id == noteId } }

    // use noteId as key, not existing?.id
    var title by rememberSaveable(noteId) { mutableStateOf("") }
    var content by rememberSaveable(noteId) { mutableStateOf("") }

    LaunchedEffect(existing) {
        if (existing != null) {
            title = existing.title
            content = existing.content
        } else {
            // new‑note: clear fields
            title = ""
            content = ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == 0L) stringResource(R.string.new_note)
                        else stringResource(R.string.edit_note),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    if (noteId != 0L) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onSave(title, content) }) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.save_note))
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.note_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.note_content)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    val sampleNotes = listOf(
        Note(1L, "Sample", "Here is some sample content", System.currentTimeMillis())
    )
    val notesFlow = MutableStateFlow(sampleNotes)
    NoteDetailScreen(
        noteId = 1L,
        notesFlow = notesFlow,
        onSave = { _, _ -> },
        onDelete = {},
        onCancel = {}
    )
}