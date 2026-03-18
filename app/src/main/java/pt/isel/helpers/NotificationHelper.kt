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

        val name = if (importance == NotificationManager.IMPORTANCE_HIGH) "Alertas" else "Monitorização"
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
        val timeText = "Tempo restante: ${formatTime(seconds)}"
        return buildBaseNotification(
            title = "Viagem em Curso",
            text = timeText,
            channelId = "ride_service_channel",
            importance = NotificationManager.IMPORTANCE_LOW,
            isOngoing = true
        ).build()
    }

    fun sendRatingReminder() {
        val notification = buildBaseNotification(
            title = "Atualizar Lotação?",
            text = "A ocupação mudou? Toque para atualizar.",
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

    // Function to update the existing foreground notification
    fun updateTimerNotification(seconds: Int) {
        notificationManager.notify(1, createTimerNotification(seconds))
    }
}