package com.example.re7entonnotesapp

import android.app.Application
import com.example.re7entonnotesapp.worker.NoteSyncScheduler
import dagger.hilt.android.HiltAndroidApp

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // start your periodic sync worker
        NoteSyncScheduler.scheduleSync(this)
    }
}