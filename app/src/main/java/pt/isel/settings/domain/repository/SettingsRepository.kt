package pt.isel.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val timeout: Flow<Int>

    val interval: Flow<Int>

    val isTestTrip: Flow<Boolean>

    val notificationInterval: Flow<Int>

    val userId: Flow<String?>

    suspend fun updateTimeout(newTimeout: Int)

    suspend fun updateInterval(newInterval: Int)

    suspend fun updateIsTestTrip(newIsTestTrip: Boolean)

    suspend fun updateNotificationInterval(newNotificationInterval: Int)

    suspend fun createUserId()
}
