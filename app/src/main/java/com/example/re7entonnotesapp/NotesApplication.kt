package com.example.re7entonnotesapp

import android.app.Application
import com.example.re7entonnotesapp.worker.NoteSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class NotesApplication : Application() {
    // Initialize any global resources here if needed.
    @Inject
    lateinit var noteSyncScheduler: NoteSyncScheduler

    override fun onCreate() {
        super.onCreate()
        noteSyncScheduler.scheduleNoteSyncWorker()
    }
}