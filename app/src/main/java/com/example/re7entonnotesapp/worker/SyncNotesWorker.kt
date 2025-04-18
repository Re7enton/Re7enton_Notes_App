package com.example.re7entonnotesapp.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.re7entonnotesapp.repository.NoteRepository

class SyncNotesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // TODO: Implement sync logic here
        return Result.success()
    }
}
