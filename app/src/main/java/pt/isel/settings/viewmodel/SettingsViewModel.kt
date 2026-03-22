package pt.isel.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.settings.domain.repository.SettingsRepository

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val timeout = repository.timeout
    val interval = repository.interval
    val isTestTrip = repository.isTestTrip
    val notificationInterval = repository.notificationInterval

    fun updateTimeout(newTimeout: Int) {
        viewModelScope.launch { repository.updateTimeout(newTimeout) }
    }

    fun updateInterval(newInterval: Int) {
        viewModelScope.launch { repository.updateInterval(newInterval) }
    }

    fun updateIsTestTrip(newIsTestTrip: Boolean) {
        viewModelScope.launch { repository.updateIsTestTrip(newIsTestTrip) }
    }

    fun updateNotificationInterval(newInterval: Int) {
        viewModelScope.launch { repository.updateNotificationInterval(newInterval) }
    }
}