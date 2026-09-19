package com.moneybooth.app.sms.android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.moneybooth.app.MainActivity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.accounting.Money
import kotlin.math.abs

/** Taps the owner on the shoulder when a transaction arrives whose balance doesn't add up. */
class DiscrepancyNotifier(private val context: Context) {

    fun notify(tx: TransactionEntity) {
        val difference = tx.discrepancyMinor ?: return
        if (difference == 0L) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        ensureChannel()
        val who = tx.senderName ?: tx.recipientName ?: "a customer"
        val text = "The balance doesn't add up by ${Money.formatWithCurrency(abs(difference))} around the " +
            "${Money.formatWithCurrency(tx.amountMinor ?: 0L)} transaction with $who. You might want to follow up."
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Unusual transaction")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify((tx.uid.hashCode() and 0x7fffffff), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Unusual transactions", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts when a transaction's balance doesn't match the ledger"
            },
        )
    }

    companion object {
        private const val CHANNEL_ID = "discrepancies"
    }
}
