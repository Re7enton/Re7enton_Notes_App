package com.example.re7entonnotesapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.viewmodel.NoteViewModel


@Composable
fun AddNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AddNoteContent(
        title = title,
        onTitleChange = { title = it },
        content = content,
        onContentChange = { content = it },
        onSave = {
            viewModel.addNote(title, content)
            navController.popBackStack()
        }
    )
}

@Composable
fun AddNoteContent(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium)) // Replace resource for preview-safe code
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.note_title)) }, // Avoid resource for preview
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text(stringResource(R.string.note_content)) },
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNoteContentPreview() {
    var title by remember { mutableStateOf("Sample Title") }
    var content by remember { mutableStateOf("This is the note content.") }

    AddNoteContent(
        title = title,
        onTitleChange = { title = it },
        content = content,
        onContentChange = { content = it },
        onSave = {
            println("Saving: $title - $content")
        }
    )
}
