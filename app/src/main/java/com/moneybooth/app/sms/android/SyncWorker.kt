package com.moneybooth.app.sms.android

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.sync.SyncRunResult

/** Uploads whatever is queued. Retries with backoff when the cloud is unreachable; never drops data. */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val container: AppContainer,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = container.deviceSettingsStore
        val result = container.syncEngine.runOnce()
        if (result != SyncRunResult.CloudNotConfigured) container.cloudSyncController.sendHeartbeat()
        return when (result) {
            is SyncRunResult.Uploaded -> {
                settings.lastSyncAt = System.currentTimeMillis()
                settings.lastSyncError = null
                Result.success()
            }
            SyncRunResult.NothingToDo -> {
                settings.lastSyncAt = System.currentTimeMillis()
                settings.lastSyncError = null
                Result.success()
            }
            SyncRunResult.CloudNotConfigured -> Result.success()
            is SyncRunResult.Failed -> {
                settings.lastSyncError = result.message
                if (runAttemptCount < MAX_ATTEMPTS_PER_RUN) Result.retry() else Result.failure()
            }
        }
    }

    companion object {
        private const val MAX_ATTEMPTS_PER_RUN = 5
    }
}
