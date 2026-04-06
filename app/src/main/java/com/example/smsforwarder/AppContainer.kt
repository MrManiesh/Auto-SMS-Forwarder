package com.example.smsforwarder

import android.content.Context
import com.example.smsforwarder.data.AppDatabase
import com.example.smsforwarder.data.LogRepository
import com.example.smsforwarder.data.SettingsRepository
import com.example.smsforwarder.data.dataStore
import com.example.smsforwarder.network.EmailClient

class AppContainer(private val context: Context) {
    val settingsRepository by lazy { SettingsRepository(context.dataStore) }
    private val appDatabase by lazy { AppDatabase.getDatabase(context) }
    val logRepository by lazy { LogRepository(appDatabase.logDao()) }
    val emailClient by lazy { EmailClient() }
}
