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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.data.Note
import com.example.re7entonnotesapp.viewmodel.NoteViewModel

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
                NoteItem(note = note, onDelete = { viewModel.deleteNote(it) })
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onDelete: (Note) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_small))
            .clickable { /* Future: navigate to detail/edit screen */ },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = note.title)
            Text(text = note.content)
        }
        Button(onClick = { onDelete(note) }) {
            Text(text = stringResource(id = R.string.delete))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteItemPreview() {
    // Sample note data
    val sampleNote = Note(
        title = "Sample Note",
        content = "This is a preview of the note content."
    )

    // Preview the NoteItem with a dummy onDelete
    NoteItem(note = sampleNote, onDelete = {})
}
