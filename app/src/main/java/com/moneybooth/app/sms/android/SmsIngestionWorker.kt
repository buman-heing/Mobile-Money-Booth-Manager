package com.moneybooth.app.sms.android

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionSource

/** Runs the exact same [com.moneybooth.app.sms.common.SmsIngestionPipeline] the developer paste screen uses. */
class SmsIngestionWorker(
    context: Context,
    params: WorkerParameters,
    private val container: AppContainer,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val boothId = container.deviceSettingsStore.activeBoothId
        val attribution = boothId?.let { container.attributionService.currentAttribution(it) }

        container.smsIngestionPipeline.ingest(
            input = RawSmsInput(sender = sender, body = body, receivedTimestamp = timestamp),
            source = TransactionSource.SMS_AUTO,
            boothId = boothId,
            shiftId = attribution?.shiftId,
            employeeId = attribution?.employeeId,
        )
        return Result.success()
    }

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
