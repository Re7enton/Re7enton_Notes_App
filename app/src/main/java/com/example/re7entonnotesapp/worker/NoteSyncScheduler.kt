package com.example.re7entonnotesapp.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class NoteSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleNoteSyncWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SyncNotesWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SyncNotesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
