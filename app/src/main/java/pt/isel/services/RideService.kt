package pt.isel.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.isel.OTPCDApplication
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL
import pt.isel.helpers.NotificationHelper
import pt.isel.repository.FirestoreRepository
import pt.isel.settings.domain.repository.SettingsRepository
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RideService() : Service() {
    private lateinit var locationService: LocationService

    private lateinit var bluetoothService: BluetoothService

    private lateinit var wifiService: WifiService

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var networkService: NetworkService

    private lateinit var cellularService: CellularService

    private lateinit var settingsRepository: SettingsRepository

    private val firestoreRepository = FirestoreRepository()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentTimeout = DEFAULT_TIMEOUT
    private var currentScanInterval = DEFAULT_INTERVAL
    private var currentNotifInterval = NOTIFICATION_REMINDER_INTERVAL
    private var isTestTrip = IS_TEST_TRIP

    companion object {
        val secondsRemaining = MutableStateFlow(DEFAULT_TIMEOUT)
        val isServiceRunning = MutableStateFlow(false)
        val isPaused = MutableStateFlow(false)
        val currentLocation = MutableStateFlow<Location?>(null)
        val currentBluetoothCount = MutableStateFlow(0)
        val currentWifiCount = MutableStateFlow(0)
        val finishedTripId = MutableStateFlow<String?>(null)
        var currentTripId: String? = null
        //val currentScanResults = MutableStateFlow<List<ScanResult>>(emptyList())
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
        notificationHelper = NotificationHelper(this)
        cellularService = CellularService(this)

        networkService = NetworkService()

        settingsRepository = (application as OTPCDApplication).settingsRepository
    }

    private var currentRating = DEFAULT_SUBJ_RATING

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "DELETE_TRIP" -> {
                val tripIdToDelete = intent.getStringExtra("TRIP_ID")
                isTestTrip = intent.getBooleanExtra("IS_TEST", IS_TEST_TRIP)
                firestoreRepository.isTest = isTestTrip

                if (tripIdToDelete != null) {
                    firestoreRepository.deleteTrip(tripIdToDelete,
                        onSuccess = { stopSelf() },
                        onFailure = { stopSelf() }
                    )
                } else {
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            "UPDATE_RATING" -> {
                currentRating = intent.getIntExtra("NEW_RATING", DEFAULT_SUBJ_RATING)
            }
            "PAUSE" -> {
                isPaused.value = true
                locationService.stopLocationUpdates()
                bluetoothService.stopScan()
                wifiService.stopScan()
            }
            "RESUME" -> {
                isPaused.value = false
                locationService.startLocationUpdates()
                bluetoothService.startScan()
                wifiService.startScan()
            }
            else -> {
                currentTimeout = intent?.getIntExtra("TIMEOUT", DEFAULT_TIMEOUT) ?: DEFAULT_TIMEOUT
                currentScanInterval = intent?.getIntExtra("INTERVAL", DEFAULT_INTERVAL) ?: DEFAULT_INTERVAL
                currentNotifInterval = intent?.getIntExtra("NOTIF_INTERVAL", NOTIFICATION_REMINDER_INTERVAL) ?: NOTIFICATION_REMINDER_INTERVAL
                isTestTrip = intent?.getBooleanExtra("IS_TEST", IS_TEST_TRIP) ?: IS_TEST_TRIP

                firestoreRepository.isTest = isTestTrip

                val tripId = intent?.getStringExtra("TRIP_ID") ?: "unknown_${System.currentTimeMillis()}"
                currentTripId = tripId
                val transportType = intent?.getStringExtra("TRANSPORT_TYPE") ?: "Unknown"
                currentRating = intent?.getIntExtra("RATING", DEFAULT_SUBJ_RATING)
                    ?: DEFAULT_SUBJ_RATING

                secondsRemaining.value = currentTimeout

                startForeground(1, notificationHelper.createTimerNotification(currentTimeout))

                val trip = TripData(
                    transportType = transportType,
                    startDate = Date()
                )

                firestoreRepository.createTrip(tripId = tripId, trip = trip, onSuccess = {
                    locationService.startLocationUpdates()
                    bluetoothService.startScan()
                    wifiService.startScan()
                })

                startRideTicker(tripId)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRideTicker(tripId: String) {
        isServiceRunning.value = true
        isPaused.value = false
        serviceScope.launch {
            var seconds = currentTimeout

            while (seconds >= 0) {
                if (isPaused.value) {
                    delay(500.milliseconds)
                    continue
                }

                secondsRemaining.value = seconds
                notificationHelper.updateTimerNotification(seconds)

                val elapsedTime = currentTimeout - seconds

                if (elapsedTime % currentScanInterval == 0 && seconds != currentTimeout) {
                    performDataScanAndUpload(tripId)
                }

                if (elapsedTime % currentNotifInterval == 0 && seconds != currentTimeout) {
                    notificationHelper.sendRatingReminder()
                }

                delay(1.seconds)
                seconds--
            }
            notificationHelper.sendTripFinishedNotification()

            stopSelf()
        }
    }

    private fun performDataScanAndUpload(tripId: String) {
        serviceScope.launch {
            Log.d("RideService", "Performing data scan and uploading for trip $tripId")

            val location = locationService.currentLocation.value
            val bluetoothCount = bluetoothService.deviceCount.value
            val signalIntensitiesBT = bluetoothService.strongestSignals.value
            val signalIntensitiesWF = wifiService.strongestSignals.value
            val wifiCount = wifiService.wifiCount.value
            val cellularMetrics = cellularService.getCurrentMetrics()

            bluetoothService.clearScan()
            bluetoothService.startScan()
            wifiService.clearScan()
            wifiService.requestNewScan()

            val networkMetricsDeferred = async { networkService.measureNetworkMetrics() }
            val networkMetrics = networkMetricsDeferred.await()

            val reading = ScanReading(
                userId = settingsRepository.userId.first()!!,
                signalIntensitiesBT = signalIntensitiesBT,
                signalIntensitiesWF = signalIntensitiesWF,
                wifiCount = wifiCount,
                bluetoothCount = bluetoothCount,
                latitude = location?.latitude,
                longitude = location?.longitude,
                latencyAvg = networkMetrics.latencyAvg,
                latencyStdDev = networkMetrics.latencyStdDev,
                packetLoss = networkMetrics.packetLoss,
                subjectiveRating = currentRating,
                rsrp = cellularMetrics.rsrp,
                rssnr = cellularMetrics.rssnr,
                rsrq = cellularMetrics.rsrq
            )

            Log.d("RideService", "Uploading reading to Firestore: $reading")
            firestoreRepository.addReading(tripId, reading)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()

        if (currentTripId != null) {
            locationService.stopLocationUpdates()
            bluetoothService.stopScan()
            wifiService.stopScan()

            finishedTripId.value = currentTripId
            currentTripId = null
        }

        isServiceRunning.value = false
        isPaused.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
