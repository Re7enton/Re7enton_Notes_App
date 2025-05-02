package com.example.re7entonnotesapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.example.re7entonnotesapp.auth.AuthViewModel
import com.example.re7entonnotesapp.presentation.ui.AppScaffold
import com.example.re7entonnotesapp.presentation.viewmodel.NoteViewModel
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.re7entonnotesapp.presentation.ui.NoteDetailScreen
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private val notesVm: NoteViewModel by viewModels()

    // Declare launcher but don't initialize it here
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Now initialize it
        launcher = registerForActivityResult(StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Let ViewModel parse the authorization result
                authVm.handleDriveAuthResponse(result.data)
            }
        }

        // Try silent sign‑in on API‑34+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            authVm.trySilentSignIn()
        }

        setContent {
            val authState by authVm.authState.collectAsState()

            Re7entonNotesAppTheme {
                AppScaffold(
                    accountEmail    = authState.email,
                    driveAuthorized = authState.driveAuthorized,
                    onSignIn        = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            authVm.signIn()
                        }
                    },
                    onSignOut       = { authVm.signOut() },
                    onSync          = {
                        when {
                            authState.email == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                                authVm.signIn()

                            authState.email != null && !authState.driveAuthorized &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                                // ask VM to produce a PendingIntent, then launch it
                                authVm.requestDriveAuth { pi ->
                                    val req = IntentSenderRequest.Builder(pi).build()
                                    launcher.launch(req)
                                }
                            }

                            else ->
                                notesVm.sync()
                        }
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