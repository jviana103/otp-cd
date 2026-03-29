package pt.isel.settings.domain.repository

import kotlinx.coroutines.flow.flowOf
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL

class MockSettingsRepository: SettingsRepository {
    override val timeout = flowOf(DEFAULT_TIMEOUT)
    override val interval = flowOf(DEFAULT_INTERVAL)
    override val isTestTrip = flowOf(IS_TEST_TRIP)
    override val notificationInterval = flowOf(NOTIFICATION_REMINDER_INTERVAL)
    override val userId = flowOf("test_user")

    override suspend fun updateTimeout(newTimeout: Int) {}
    override suspend fun updateInterval(newInterval: Int) {}
    override suspend fun updateIsTestTrip(newIsTestTrip: Boolean) {}
    override suspend fun updateNotificationInterval(newNotificationInterval: Int) {}
    override suspend fun createUserId() {}
}