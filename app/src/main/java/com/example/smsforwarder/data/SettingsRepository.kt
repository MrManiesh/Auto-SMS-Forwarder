package com.example.smsforwarder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val TARGET_PHONE = stringPreferencesKey("target_phone")
        val TARGET_EMAIL = stringPreferencesKey("target_email")
        val FORWARDING_ENABLED = booleanPreferencesKey("forwarding_enabled")
        val EMAIL_FORWARDING_ENABLED = booleanPreferencesKey("email_forwarding_enabled")
        val SENDER_EMAIL = stringPreferencesKey("sender_email")
        val APP_PASSWORD = stringPreferencesKey("app_password")
        val RECENT_HASHES = stringPreferencesKey("recent_hashes") // For deduplication
    }

    val targetPhoneFlow: Flow<String> = dataStore.data.map { it[TARGET_PHONE] ?: "" }
    val targetEmailFlow: Flow<String> = dataStore.data.map { it[TARGET_EMAIL] ?: "" }
    val forwardingEnabledFlow: Flow<Boolean> = dataStore.data.map { it[FORWARDING_ENABLED] ?: false }
    val emailForwardingEnabledFlow: Flow<Boolean> = dataStore.data.map { it[EMAIL_FORWARDING_ENABLED] ?: false }
    val senderEmailFlow: Flow<String> = dataStore.data.map { it[SENDER_EMAIL] ?: "" }
    val appPasswordFlow: Flow<String> = dataStore.data.map { it[APP_PASSWORD] ?: "" }
    val recentHashesFlow: Flow<String> = dataStore.data.map { it[RECENT_HASHES] ?: "" }

    suspend fun updateTargetPhone(phone: String) {
        dataStore.edit { it[TARGET_PHONE] = phone }
    }

    suspend fun updateTargetEmail(email: String) {
        dataStore.edit { it[TARGET_EMAIL] = email }
    }

    suspend fun setForwardingEnabled(enabled: Boolean) {
        dataStore.edit { it[FORWARDING_ENABLED] = enabled }
    }

    suspend fun setEmailForwardingEnabled(enabled: Boolean) {
        dataStore.edit { it[EMAIL_FORWARDING_ENABLED] = enabled }
    }

    suspend fun updateSenderEmail(email: String) {
        dataStore.edit { it[SENDER_EMAIL] = email }
    }

    suspend fun updateAppPassword(password: String) {
        dataStore.edit { it[APP_PASSWORD] = password }
    }

    suspend fun updateRecentHashes(hashes: String) {
        dataStore.edit { it[RECENT_HASHES] = hashes }
    }
}
