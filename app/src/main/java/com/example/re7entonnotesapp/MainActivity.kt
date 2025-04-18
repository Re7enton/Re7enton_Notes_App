package com.example.re7entonnotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.re7entonnotesapp.ui.theme.Re7entonNotesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.re7entonnotesapp.ui.screens.AddNoteScreen
import com.example.re7entonnotesapp.ui.screens.NoteListScreen
import com.example.re7entonnotesapp.worker.SyncNotesWorker
import java.util.concurrent.TimeUnit

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
        val workRequest = PeriodicWorkRequestBuilder<SyncNotesWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncNotesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}