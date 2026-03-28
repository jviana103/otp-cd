package pt.isel.datascan.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            android.widget.Toast.makeText(context, "Ligue o Bluetooth para iniciar o scan!", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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

    fun stopRide(context: Context) {
        context.stopService(Intent(context, RideService::class.java))
    }
}