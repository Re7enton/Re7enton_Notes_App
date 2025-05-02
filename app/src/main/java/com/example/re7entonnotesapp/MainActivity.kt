package com.example.re7entonnotesapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.re7entonnotesapp.auth.AuthViewModel
import com.example.re7entonnotesapp.presentation.ui.AppScaffold
import com.example.re7entonnotesapp.presentation.viewmodel.NoteViewModel
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen
import com.example.re7entonnotesapp.presentation.ui.NoteDetailScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private val notesVm: NoteViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            authVm.handleSignInResult(res.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val authState by authVm.stateFlow.collectAsState()

            Re7entonNotesAppTheme {
                AppScaffold(
                    accountEmail    = authState.email,
                    driveAuthorized = authState.driveAuthorized,
                    onSignIn        = {
                        // launch classic GoogleSignIn flow
                        signInLauncher.launch(authVm.getSignInIntent())
                    },
                    onSignOut       = { authVm.signOut() },
                    onSync          = {
                        notesVm.sync()   // now that Drive scope was granted during sign‑in
                    }
                ) { padding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "note_list",
                        modifier = padding
                    ) {
                        composable("note_list") {
                            NoteListScreen(
                                notes = notesVm.notes,
                                onAddNote = { navController.navigate("note_detail/0") },
                                onEditNote = { navController.navigate("note_detail/$it") }
                            )
                        }
                        composable(
                            "note_detail/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("noteId") ?: 0L
                            NoteDetailScreen(
                                noteId = id,
                                notesFlow = notesVm.notes,
                                onSave = { t, c ->
                                    notesVm.saveNote(t, c, id)
                                    navController.popBackStack()
                                },
                                onDelete = {
                                    if (id != 0L) notesVm.removeNote(id)
                                    navController.popBackStack()
                                },
                                onCancel = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
