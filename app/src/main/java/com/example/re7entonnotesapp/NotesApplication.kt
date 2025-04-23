package com.example.re7entonnotesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.example.re7entonnotesapp.worker.NoteSyncScheduler
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // start your periodic sync worker
        NoteSyncScheduler.scheduleSync(this)
    }
}