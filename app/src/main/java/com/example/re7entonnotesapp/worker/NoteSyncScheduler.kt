package com.example.re7entonnotesapp.worker

import android.content.Context
import androidx.work.*
import com.example.re7entonnotesapp.sync.SyncWorker
import javax.inject.Singleton
import java.util.concurrent.TimeUnit


object NoteSyncScheduler {
    private const val UNIQUE_WORK_NAME = "NotesSyncWork"

    fun scheduleSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}