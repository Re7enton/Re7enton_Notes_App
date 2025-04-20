package com.example.re7entonnotesapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.viewmodel.NoteViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun NoteListScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel() // Hilt provides the ViewModel
) {
    val notes = viewModel.notes.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        // Button to navigate to add note screen
        Button(onClick = { navController.navigate("addNote") }) {
            Text(text = stringResource(id = R.string.add_note))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        LazyColumn {
            items(notes) { note ->
                NoteItem(note = note, onEdit = {/* TODO */}, onDelete = { viewModel.deleteNote(it) })
            }
        }
    }
}

@Composable
fun NoteItem(note: Note,
             onEdit: (Note) -> Unit,
             onDelete: (Note) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = note.title,
            style = MaterialTheme.typography.titleSmall)
        Text(text = note.content,
            style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "Last edited: ${DateFormat.getDateTimeInstance().format(Date(note.lastEdited))}",
            style = MaterialTheme.typography.labelSmall
        )
        Row {
            Button(onClick = { onEdit(note) }) {
                Text("Edit")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onDelete(note) }) {
                Text("Delete")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NoteListScreenPreview() {
    val fakeNotes = listOf(
        Note(title = "Note 1", content = "Content of note 1"),
        Note(title = "Note 2", content = "Content of note 2")
    )

    val fakeNavController = rememberNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Use hardcoded dp for preview
    ) {
        Button(onClick = { /* Do nothing */ }) {
            Text(text = "Add Note")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(fakeNotes) { note ->
                NoteItem(note = note, onEdit = {/* TODO */}, onDelete = {})
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NoteItemPreview() {
    val sampleNote = Note(
        title = "Sample Note",
        content = "This is a sample note for preview purposes.",
        lastEdited = System.currentTimeMillis()
    )

    // Use dummy lambdas for preview
    NoteItem(
        note = sampleNote,
        onEdit = {},
        onDelete = {}
    )
}
