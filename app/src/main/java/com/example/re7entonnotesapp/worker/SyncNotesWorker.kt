package com.example.re7entonnotesapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.re7entonnotesapp.data.NoteDatabase
import com.example.re7entonnotesapp.repository.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncNotesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: NoteRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            repository.syncNotesWithServer()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
