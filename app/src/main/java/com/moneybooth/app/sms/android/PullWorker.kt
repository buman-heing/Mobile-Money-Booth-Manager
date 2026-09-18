package com.moneybooth.app.sms.android

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneybooth.app.core.data.AppContainer

/** Background catch-up from the cloud, so an owner phone is current even before it is opened. */
class PullWorker(
    context: Context,
    params: WorkerParameters,
    private val container: AppContainer,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val businessUid = container.cloudSyncController.businessUid() ?: return Result.success()
        return try {
            container.cloudPuller.pullOnce(businessUid)
            container.deviceSettingsStore.lastPullAt = System.currentTimeMillis()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}
