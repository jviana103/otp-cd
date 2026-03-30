package pt.isel.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import pt.isel.MainActivity
import pt.isel.R

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun buildBaseNotification(
        title: String,
        text: String,
        channelId: String,
        importance: Int,
        isOngoing: Boolean,
        autoCancel: Boolean = false
    ): NotificationCompat.Builder {

        val name =
            if (importance == NotificationManager.IMPORTANCE_HIGH)
                context.getString(R.string.notif_channel_alerts)
            else context.getString(R.string.notif_channel_monitoring)

        val channel = NotificationChannel(channelId, name, importance)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_train)
            .setOngoing(isOngoing)
            .setAutoCancel(autoCancel)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
    }

    fun createTimerNotification(seconds: Int): Notification {
        val timeText = context.getString(R.string.notif_timer_content, formatTime(seconds))
        return buildBaseNotification(
            title = context.getString(R.string.notif_timer_title),
            text = timeText,
            channelId = "ride_service_channel",
            importance = NotificationManager.IMPORTANCE_LOW,
            isOngoing = true
        ).build()
    }

    fun sendRatingReminder() {
        val notification = buildBaseNotification(
            title = context.getString(R.string.notif_reminder_title),
            text = context.getString(R.string.notif_reminder_content),
            channelId = "rating_reminder_channel",
            importance = NotificationManager.IMPORTANCE_HIGH,
            isOngoing = false,
            autoCancel = true
        )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        notificationManager.notify(2, notification)
    }

    fun updateTimerNotification(seconds: Int) {
        notificationManager.notify(1, createTimerNotification(seconds))
    }

    fun sendTripFinishedNotification() {
        val notification = buildBaseNotification(
            title = context.getString(R.string.notif_trip_finished_title),
            text = context.getString(R.string.notif_trip_finished_content),
            channelId = "trip_finished_channel",
            importance = NotificationManager.IMPORTANCE_HIGH,
            isOngoing = false,
            autoCancel = true
        )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(3, notification)
    }
}