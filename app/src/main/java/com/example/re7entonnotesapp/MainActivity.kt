package com.example.re7entonnotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.re7entonnotesapp.presentation.navigation.NavRoutes
import com.example.re7entonnotesapp.presentation.ui.NoteDetailScreen
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen
import com.example.re7entonnotesapp.presentation.viewmodel.NoteViewModel

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
                        startDestination = NavRoutes.NOTE_LIST,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // List Screen
                        composable(
                            NavRoutes.NOTE_LIST) {
                            val vm: NoteViewModel = hiltViewModel()
                            NoteListScreen(
                                notes = vm.notes,
                                onAddNote = { navController.navigate(NavRoutes.noteDetailRoute(0L)) },
                                onEditNote = { navController.navigate(NavRoutes.noteDetailRoute(it)) }
                            )
                        }
                        // Detail Screen with noteId arg
                        composable(
                            route = "${NavRoutes.NOTE_DETAIL}/{${NavRoutes.ARG_NOTE_ID}}",
                            arguments = listOf(navArgument(NavRoutes.ARG_NOTE_ID) {
                                type = NavType.LongType } )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong(NavRoutes.ARG_NOTE_ID) ?: 0L
                            val vm: NoteViewModel = hiltViewModel()
                            NoteDetailScreen(
                                noteId = id,
                                notesFlow = vm.notes,
                                onSave = { title, content ->
                                    vm.saveNote(title, content, id)
                                    navController.popBackStack()
                                },
                                onDelete = {
                                    if (id != 0L) vm.removeNote(id)
                                    navController.popBackStack() },
                                onCancel  = { navController.popBackStack() } )
                        }
                    }
                }
            }
        }
    }
}