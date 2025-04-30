package com.example.re7entonnotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import com.example.re7entonnotesapp.presentation.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.re7entonnotesapp.auth.AuthViewModel
import com.example.re7entonnotesapp.presentation.navigation.NavRoutes
import com.example.re7entonnotesapp.presentation.ui.AppScaffold
import com.example.re7entonnotesapp.presentation.ui.NoteDetailScreen
import com.example.re7entonnotesapp.presentation.ui.NoteListScreen
import com.example.re7entonnotesapp.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.map
import androidx.activity.result.IntentSenderRequest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // launcher for any PendingIntent (sign-in or Drive consent)
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            // no-op: your AuthViewModel already observes credential state via suspend calls
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authVm: AuthViewModel = hiltViewModel()
            val notesVm: NoteViewModel = hiltViewModel()

            val authState by authVm.authState.collectAsState()

            // attempt silent sign-in on launch
            LaunchedEffect(Unit) { authVm.trySilentSignIn() }

            Re7entonNotesAppTheme {
                AppScaffold(
                    accountEmail     = authState.email,
                    driveAuthorized  = authState.driveAuthorized,
                    onSignIn         = { authVm.beginInteractiveSignIn { pi -> launcher.launch(IntentSenderRequest.Builder(pi).build()) } },
                    onSignOut        = { authVm.signOut() },
                    onSync           = {
                        when {
                            authState.email == null ->
                                authVm.beginInteractiveSignIn { pi -> launcher.launch(IntentSenderRequest.Builder(pi).build()) }
                            !authState.driveAuthorized ->
                                authVm.requestDriveAuth { pi -> launcher.launch(IntentSenderRequest.Builder(pi).build()) }
                            else ->
                                notesVm.sync()
                        }
                    }
                ) { padding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.NOTE_LIST,
                        modifier = padding
                    ) {
                        // List Screen
                        composable(
                            NavRoutes.NOTE_LIST) {
                            NoteListScreen(
                                notes = notesVm.notes,
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
                            NoteDetailScreen(
                                noteId = id,
                                notesFlow = notesVm.notes,
                                onSave = { title, content ->
                                    notesVm.saveNote(title, content, id)
                                    navController.popBackStack()
                                },
                                onDelete = {
                                    if (id != 0L) notesVm.removeNote(id)
                                    navController.popBackStack() },
                                onCancel  = { navController.popBackStack() } )
                        }
                    }
                }
            }
        }
    }
}