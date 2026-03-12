package pt.isel.services

import pt.isel.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pt.isel.MainActivity
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import kotlin.time.Duration.Companion.seconds

class RideService() : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        val secondsRemaining = MutableStateFlow(DEFAULT_TIMEOUT)
        val isServiceRunning = MutableStateFlow(false)
    }

    private var currentRating = DEFAULT_SUBJ_RATING

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_RATING" -> {
                currentRating = intent.getIntExtra("NEW_RATING", DEFAULT_SUBJ_RATING)
                Log.e("RideService", "Received new rating: $currentRating")
            }
            else -> {
                val tripId = intent?.getStringExtra("TRIP_ID") ?: "unknown"
                currentRating = intent?.getIntExtra("RATING", DEFAULT_SUBJ_RATING)
                    ?: DEFAULT_SUBJ_RATING
                Log.e("RideService", "Received tripId: $tripId")
                Log.e("RideService", "Received rating: $currentRating")
                startForeground(1, createNotificationWithTime(DEFAULT_TIMEOUT))
                startRideTicker(tripId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRideTicker(tripId: String) {
        isServiceRunning.value = true
        serviceScope.launch {
            for (seconds in DEFAULT_TIMEOUT downTo 0) {
                secondsRemaining.value = seconds

                val updatedNotification = createNotificationWithTime(seconds)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(1, updatedNotification)


                if ((DEFAULT_TIMEOUT - seconds) % DEFAULT_INTERVAL == 0 && seconds != DEFAULT_TIMEOUT) {
                    performDataScanAndUpload(tripId)
                }

                delay(1.seconds)
            }
            stopSelf()
        }
    }

    private fun createNotificationWithTime(seconds: Int): Notification {
        val channelId = "ride_service_channel"
        val minutes = seconds / 60
        val secs = seconds % 60
        val timeText = "Tempo restante: $minutes:$secs"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Monitorização de Transporte",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Viagem em Curso")
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_train)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotification(): Notification {
        val channelId = "ride_service_channel"
        val channelName = "Monitorização de Transporte"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Canal para monitorização de ocupação em tempo real"
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Viagem em Curso")
            .setContentText("A recolher dados de Wi-Fi e Bluetooth...")
            .setSmallIcon(R.drawable.ic_train)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun performDataScanAndUpload(tripId: String) {
        Log.d("RideService", "Performing data scan and uploading for trip $tripId")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        isServiceRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}