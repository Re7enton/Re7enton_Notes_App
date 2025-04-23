package com.example.re7entonnotesapp.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.re7entonnotesapp.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    notesFlow: StateFlow<List<Note>>,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val notes by notesFlow.collectAsState()
    val existing = notes.find { it.id == noteId }

    var title by rememberSaveable { mutableStateOf(existing?.title.orEmpty()) }
    var content by rememberSaveable { mutableStateOf(existing?.content.orEmpty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == 0L) "New Note" else "Edit Note",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (noteId != 0L) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onSave(title, content) }) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        },
        content = { paddingValues ->                      // apply paddingValues :contentReference[oaicite:2]{index=2}
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    )
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
        onDelete = {}
    )
}