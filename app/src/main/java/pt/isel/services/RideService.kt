package pt.isel.services

import pt.isel.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.wifi.ScanResult
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
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.repository.FirestoreRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class RideService() : Service() {
    private lateinit var locationService: LocationService

    private lateinit var bluetoothService: BluetoothService

    private lateinit var wifiService: WifiService

    private val firestoreRepository = FirestoreRepository()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        val secondsRemaining = MutableStateFlow(DEFAULT_TIMEOUT)
        val isServiceRunning = MutableStateFlow(false)
        val currentLocation = MutableStateFlow<Location?>(null)
        val currentBluetoothCount = MutableStateFlow(0)
        val currentWifiCount = MutableStateFlow(0)
        val currentScanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    }

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this)
        serviceScope.launch {
            locationService.currentLocation.collect { location ->
                currentLocation.value = location
            }
        }
        bluetoothService = BluetoothService(this)
        serviceScope.launch {
            bluetoothService.deviceCount.collect { count ->
                currentBluetoothCount.value = count
            }
        }
        wifiService = WifiService(this)
        serviceScope.launch {
            wifiService.wifiCount.collect { count ->
                currentWifiCount.value = count
            }
        }
    }

    private var currentRating = DEFAULT_SUBJ_RATING

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_RATING" -> {
                currentRating = intent.getIntExtra("NEW_RATING", DEFAULT_SUBJ_RATING)
            }
            else -> {
                val tripId = intent?.getStringExtra("TRIP_ID") ?: "unknown_${System.currentTimeMillis()}"
                val transportType = intent?.getStringExtra("TRANSPORT_TYPE") ?: "Unknown"
                currentRating = intent?.getIntExtra("RATING", DEFAULT_SUBJ_RATING)
                    ?: DEFAULT_SUBJ_RATING

                startForeground(1, createNotificationWithTime(DEFAULT_TIMEOUT))

                val trip = TripData(
                    transportType = transportType,
                    startDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                )

                firestoreRepository.createTrip(tripId = tripId, trip = trip, onSuccess = {
                    locationService.startLocationUpdates()
                    bluetoothService.startScan()
                    wifiService.startScan()
                }
                )





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

    private fun performDataScanAndUpload(tripId: String) {
        Log.d("RideService", "Performing data scan and uploading for trip $tripId")
        val location = locationService.currentLocation.value
        val bluetoothCount = bluetoothService.deviceCount.value
        bluetoothService.clearScan()

        val wifiCount = wifiService.wifiCount.value
        wifiService.requestNewScan()

        val reading = ScanReading(
            wifiCount = wifiCount,
            bluetoothCount = bluetoothCount,
            latitude = location?.latitude,
            longitude = location?.longitude,
            subjectiveRating = currentRating,
        )

        Log.d("RideService", "Uploading reading to Firestore: $reading")
        firestoreRepository.addReading(tripId, reading)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        locationService.stopLocationUpdates()
        bluetoothService.stopScan()
        wifiService.stopScan()
        isServiceRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}