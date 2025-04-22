package com.example.re7entonnotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.re7entonnotesapp.presentation.ui.AddNoteScreen
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Re7entonNotesAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "notelist",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("noteList") {
                            NoteListScreen(navController = navController)
                        }
                        composable("addNote") {
                            AddNoteScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}