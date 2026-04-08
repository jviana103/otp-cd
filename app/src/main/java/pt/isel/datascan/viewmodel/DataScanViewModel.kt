package pt.isel.datascan.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.R
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TransportationType
import pt.isel.datascan.viewmodel.state.DataScanUiState
import pt.isel.services.RideService
import pt.isel.settings.domain.repository.SettingsRepository
import java.util.UUID
class DataScanViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DataScanUiState())
    var uiState = _uiState.asStateFlow()

    private val _selectedTransport = MutableStateFlow<TransportationType?>(null)
    val selectedTransport = _selectedTransport.asStateFlow()

    fun selectTransport(type: TransportationType) {
        _selectedTransport.value = type
    }

    init {
        viewModelScope.launch {
            RideService.secondsRemaining.collect { seconds ->
                _uiState.update { it.copy(secondsRemaining = seconds) }
            }
        }
        viewModelScope.launch {
            RideService.isServiceRunning.collect { running ->
                _uiState.update { it.copy(isRiding = running) }
            }
        }
        viewModelScope.launch {
            RideService.isPaused.collect { paused ->
                _uiState.update { it.copy(isPaused = paused) }
            }
        }
        viewModelScope.launch {
            RideService.currentLocation.collect { location ->
                _uiState.update {
                    it.copy(
                        lastRead =
                            ScanReading(
                                latitude = location?.latitude,
                                longitude = location?.longitude
                            )
                    )
                }
            }
        }
        viewModelScope.launch {
            RideService.currentBluetoothCount.collect { count ->
                _uiState.update { it.copy(
                    lastRead = it.lastRead?.copy(bluetoothCount = count)) }
            }
        }
        viewModelScope.launch {
            RideService.currentWifiCount.collect { count ->
                _uiState.update {
                    it.copy(
                        lastRead = it.lastRead?.copy(wifiCount = count)
                    )
                }
            }
        }
        viewModelScope.launch {
            RideService.finishedTripId.collect { tripId ->
                _uiState.update { it.copy(finishedTripIdToConfirm = tripId) }
            }
        }
    }

    fun startRide() {
        _uiState.update { it.copy(isAwaitingInitialRating = true) }
    }

    fun cancelStart() {
        _uiState.update { it.copy(isAwaitingInitialRating = false) }
    }

    fun confirmInitialRating(context: Context, rating: Int) {
        viewModelScope.launch {
            if (!checkBluetoothConnection(context)) return@launch
            if (!checkLocationConnection(context)) return@launch

            val newTripId = "trip_${UUID.randomUUID()}"

            val currentTimeout = repository.timeout.first()
            val currentInterval = repository.interval.first()
            val isTest = repository.isTestTrip.first()

            _uiState.update {
                it.copy(
                    isAwaitingInitialRating = false,
                    isRiding = true,
                    tripId = newTripId,
                    currentSubjectiveRating = rating,
                    secondsRemaining = currentTimeout
                )
            }

            val intent = Intent(context, RideService::class.java).apply {
                putExtra("TRIP_ID", newTripId)
                putExtra("RATING", rating)
                putExtra("TRANSPORT_TYPE", selectedTransport.value?.name)

                putExtra("TIMEOUT", currentTimeout)
                putExtra("INTERVAL", currentInterval)
                putExtra("IS_TEST", isTest)
            }
            context.startForegroundService(intent)
        }
    }

    fun checkBluetoothConnection(context: Context) : Boolean{
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val isBluetoothOn = bluetoothManager.adapter?.isEnabled == true

        if (!isBluetoothOn) {
            Toast.makeText(context, context.getString(R.string.bluetooth_reminder), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkLocationConnection(context: Context) : Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isLocationOn) {
            Toast.makeText(context, context.getString(R.string.location_reminder), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.ACCESS_FINE_LOCATION)
            missing.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (missing.isNotEmpty()) {
            Toast.makeText(context, context.getString(R.string.toast_permissions_required), Toast.LENGTH_LONG).show()
        }

        return missing
    }

    fun togglePause(context: Context) {
        val action = if (uiState.value.isPaused) "RESUME" else "PAUSE"
        val intent = Intent(context, RideService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }

    fun updateOngoingRating(context: Context, newRating: Int) {
        _uiState.update { it.copy(currentSubjectiveRating = newRating) }

        val intent = Intent(context, RideService::class.java).apply {
            action = "UPDATE_RATING"
            putExtra("NEW_RATING", newRating)
        }

        context.startService(intent)
    }

    fun handleTripConfirmation(context: Context, stayedInside: Boolean) {
        val tripId = uiState.value.finishedTripIdToConfirm
        val appContext = context.applicationContext

        if (tripId != null && !stayedInside) {
            viewModelScope.launch {
                val isTest = repository.isTestTrip.first()
                val intent = Intent(appContext, RideService::class.java).apply {
                    action = "INVALIDATE_TRIP"
                    putExtra("TRIP_ID", tripId)
                    putExtra("IS_TEST", isTest)
                }
                appContext.startService(intent)
            }
        }

        _uiState.update { it.copy(finishedTripIdToConfirm = null) }
        RideService.finishedTripId.value = null
    }

    fun stopRide(context: Context) {
        context.stopService(Intent(context, RideService::class.java))
    }
}