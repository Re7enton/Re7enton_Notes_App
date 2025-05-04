package com.example.re7entonnotesapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.re7entonnotesapp.auth.AuthViewModel
import com.example.re7entonnotesapp.presentation.ui.AppScaffold
import com.example.re7entonnotesapp.presentation.ui.NoteDetailScreen
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import com.example.re7entonnotesapp.presentation.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private val notesVm: NoteViewModel by viewModels()

    private lateinit var driveLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Launcher for Drive‑consent PendingIntent
        driveLauncher = registerForActivityResult(StartIntentSenderForResult()) { res ->
            Log.d(TAG, "Drive launcher result: $res")
            if (res.resultCode == RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                authVm.handleDriveAuthResponse(res.data)
            }
        }

        // Attempt silent sign‑in on API34+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            authVm.trySilentSignIn()
        }

        setContent {
            val authState by authVm.authState.collectAsState()
            val errorMsg by authVm.errorState.collectAsState()
            val snackbarHost = remember { SnackbarHostState() }

            // Show error in snackbar
            LaunchedEffect(errorMsg) {
                errorMsg?.let {
                    Log.e(TAG, "Showing error: $it")
                    snackbarHost.showSnackbar(it)
                }
            }

            Re7entonNotesAppTheme {
                AppScaffold(
                    accountEmail    = authState.email,
                    driveAuthorized = authState.driveAuthorized,
                    onSignIn        = {
                        Log.d(TAG, "Sign‑in button clicked")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            authVm.signIn()
                        }
                    },
                    onSignOut       = {
                        Log.d(TAG, "Sign‑out button clicked")
                        authVm.signOut()
                    },
                    onSync          = {
                        Log.d(TAG, "Sync button clicked: authState=$authState")
                        when {
                            authState.email == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                                authVm.signIn()

                            authState.email != null && !authState.driveAuthorized &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                                authVm.requestDriveAuth { pi ->
                                    Log.d(TAG, "Launching Drive consent UI")
                                    driveLauncher.launch(IntentSenderRequest.Builder(pi).build())
                                }
                            }

                            else ->
                                notesVm.sync()
                        }
                    },
                    snackbarHostState = snackbarHost
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
