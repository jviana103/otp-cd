package pt.isel.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.settings.domain.repository.SettingsRepository

@Suppress("UNCHECKED_CAST")
class SettingsVMFactoryVMFactory(
    private val settingsRepository: SettingsRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(settingsRepository) as T
    }
}