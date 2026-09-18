package com.moneybooth.app.sms.android

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.moneybooth.app.core.data.AppContainer

class AppWorkerFactory(private val container: AppContainer) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SmsIngestionWorker::class.java.name -> SmsIngestionWorker(appContext, workerParameters, container)
        SyncWorker::class.java.name -> SyncWorker(appContext, workerParameters, container)
        PullWorker::class.java.name -> PullWorker(appContext, workerParameters, container)
        else -> null
    }
}
