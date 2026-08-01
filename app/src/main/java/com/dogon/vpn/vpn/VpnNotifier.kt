package com.dogon.vpn.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dogon.vpn.MainActivity
import com.dogon.vpn.R

/**
 * Owns exactly ONE notification (fixed ID). We always call notify() with the same
 * ID so Android updates the existing notification in place instead of spawning new
 * ones — this is what makes the duration/speed counters feel "live" without spam.
 */
object VpnNotifier {
    const val CHANNEL_ID = "dogon_vpn_status"
    const val NOTIFICATION_ID = 4200

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DogonVPN Bağlantı Durumu",
                NotificationManager.IMPORTANCE_LOW // no sound/heads-up — it's a persistent status
            ).apply {
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun build(
        context: Context,
        connected: Boolean,
        durationText: String,
        downSpeedText: String,
        upSpeedText: String,
        toggleAction: PendingIntent
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (connected) "Bağlı · $durationText" else "Bağlı Değil"
        val body = if (connected) "↓ $downSpeedText   ↑ $upSpeedText" else "Dokunup bağlan"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_mono)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(connected)
            .setOnlyAlertOnce(true) // critical: never re-alerts, just silently updates
            .setContentIntent(openAppIntent)
            .addAction(
                if (connected) R.drawable.ic_power else R.drawable.ic_power,
                if (connected) "Kes" else "Bağlan",
                toggleAction
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun notify(context: Context, notification: Notification) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }
}
