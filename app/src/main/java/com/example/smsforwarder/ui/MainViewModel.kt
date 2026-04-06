package com.example.smsforwarder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smsforwarder.data.LogRepository
import com.example.smsforwarder.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    val targetPhone = settingsRepository.targetPhoneFlow.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val targetEmail = settingsRepository.targetEmailFlow.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val forwardingEnabled = settingsRepository.forwardingEnabledFlow.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val emailForwardingEnabled = settingsRepository.emailForwardingEnabledFlow.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val senderEmail = settingsRepository.senderEmailFlow.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val appPassword = settingsRepository.appPasswordFlow.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val recentLogs = logRepository.recentLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateTargetPhone(phone: String) {
        viewModelScope.launch { settingsRepository.updateTargetPhone(phone) }
    }

    fun updateTargetEmail(email: String) {
        viewModelScope.launch { settingsRepository.updateTargetEmail(email) }
    }

    fun setForwardingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setForwardingEnabled(enabled) }
    }

    fun setEmailForwardingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setEmailForwardingEnabled(enabled) }
    }

    fun updateSenderEmail(email: String) {
        viewModelScope.launch { settingsRepository.updateSenderEmail(email) }
    }

    fun updateAppPassword(password: String) {
        viewModelScope.launch { settingsRepository.updateAppPassword(password) }
    }
}

class MainViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val logRepository: LogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(settingsRepository, logRepository) as T
    }
}
