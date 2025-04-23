package com.example.re7entonnotesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.re7entonnotesapp.worker.NoteSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class NotesApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        NoteSyncScheduler.scheduleSync(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)    // ← installs HiltWorkerFactory
            .build()
}