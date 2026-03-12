package pt.isel.datascan.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.DataScanUiState
import pt.isel.services.RideService
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class DataScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DataScanUiState())
    var uiState = _uiState.asStateFlow()

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
    }

    fun startRide() {
        _uiState.update { it.copy(isAwaitingInitialRating = true) }
    }

    fun confirmInitialRating(context: Context, rating: Int) {
        val newTripId = "trip_${UUID.randomUUID()}"

        _uiState.update {
            it.copy(
                isAwaitingInitialRating = false,
                isRiding = true,
                tripId = newTripId,
                currentSubjectiveRating = rating,
                secondsRemaining = DEFAULT_TIMEOUT
            )
        }

        val intent = Intent(context, RideService::class.java).apply {
            putExtra("TRIP_ID", newTripId)
            putExtra("RATING", rating)
        }
        context.startForegroundService(intent)

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