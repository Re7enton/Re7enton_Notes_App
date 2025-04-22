package com.example.re7entonnotesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.example.re7entonnotesapp.worker.NoteSyncScheduler
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Annotate the application class to trigger Hilt's code generation.
@HiltAndroidApp
class NotesApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var noteSyncScheduler: NoteSyncScheduler

    override fun onCreate() {
        super.onCreate()
        noteSyncScheduler.scheduleNoteSyncWorker()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

