package com.moneybooth.app.sms.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.moneybooth.app.core.data.DeviceSettingsStore

class SmsReceivedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!DeviceSettingsStore(context).smsIngestionEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val bySender = messages.groupBy { it.originatingAddress ?: "UNKNOWN" }

        for ((sender, parts) in bySender) {
            val body = parts.joinToString(separator = "") { it.messageBody ?: "" }
            val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
            val data = Data.Builder()
                .putString(SmsIngestionWorker.KEY_SENDER, sender)
                .putString(SmsIngestionWorker.KEY_BODY, body)
                .putLong(SmsIngestionWorker.KEY_TIMESTAMP, timestamp)
                .build()
            val request = OneTimeWorkRequestBuilder<SmsIngestionWorker>().setInputData(data).build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
