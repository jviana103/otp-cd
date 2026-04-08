package pt.isel.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.isel.settings.domain.repository.SettingsRepository
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL

private val timeoutKey = intPreferencesKey("timeout")
private val intervalKey = intPreferencesKey("interval")
private val isTestTripKey = booleanPreferencesKey("isTestTrip")
private val notificationIntervalKey = intPreferencesKey("notificationInterval")

private val userIdKey = stringPreferencesKey("userId")

class SettingsPreferenceRepository (
    private val dataStore: DataStore<Preferences>
): SettingsRepository {
    override val timeout: Flow<Int> = dataStore.data.map { preferences ->
        preferences[timeoutKey] ?: DEFAULT_TIMEOUT
    }

    override var interval: Flow<Int> = dataStore.data.map {
        it[intervalKey] ?: DEFAULT_INTERVAL
    }

    override var isTestTrip: Flow<Boolean> = dataStore.data.map {
        it[isTestTripKey] ?: IS_TEST_TRIP
    }

    override var notificationInterval: Flow<Int> = dataStore.data.map {
        it[notificationIntervalKey] ?: NOTIFICATION_REMINDER_INTERVAL
    }

    override val userId: Flow<String?> = dataStore.data.map {
        it[userIdKey] ?: null
    }

    override suspend fun updateTimeout(newTimeout: Int) {
        dataStore.edit { preferences ->
            preferences[timeoutKey] = newTimeout
        }
    }

    override suspend fun updateInterval(newInterval: Int) {
        dataStore.edit { preferences ->
            preferences[intervalKey] = newInterval
        }
    }

    override suspend fun updateIsTestTrip(newIsTestTrip: Boolean) {
        dataStore.edit { preferences ->
            preferences[isTestTripKey] = newIsTestTrip
        }
    }

    override suspend fun updateNotificationInterval(newNotificationInterval: Int) {
        dataStore.edit { preferences ->
            preferences[notificationIntervalKey] = newNotificationInterval
        }
    }

    override suspend fun createUserId() {
        val userIdNullable = userId.first()
        if (userIdNullable != null) return

        val release = android.os.Build.VERSION.RELEASE
        val sdk = android.os.Build.VERSION.SDK_INT

        dataStore.edit { preferences ->
            val uuid = java.util.UUID.randomUUID().toString()
            preferences[userIdKey] = "$uuid-v$release-api$sdk"
        }
    }
}
