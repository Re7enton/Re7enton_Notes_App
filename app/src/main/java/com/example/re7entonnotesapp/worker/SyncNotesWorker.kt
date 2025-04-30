package com.example.re7entonnotesapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.re7entonnotesapp.domain.usecase.SyncNotesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncNotesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncNotesUseCase: SyncNotesUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncNotesUseCase()         // perform sync
            Result.success()
        } catch (e: Exception) {
            Result.retry()             // retry on failure
        }
    }
}
