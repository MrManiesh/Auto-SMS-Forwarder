package com.example.smsforwarder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smsforwarder.SmsForwarderApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ForwardingService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "forwarder_channel",
                "SMS Forwarder Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "forwarder_channel")
            .setContentTitle("SMS Forwarder Active")
            .setContentText("Listening for incoming SMS to forward.")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        fun processIncomingSms(context: Context, sender: String, body: String) {
            val app = context.applicationContext as SmsForwarderApp
            val container = app.container

            CoroutineScope(Dispatchers.IO).launch {
                val isEnabled = container.settingsRepository.forwardingEnabledFlow.first()
                if (!isEnabled) {
                    Log.d("Forwarding", "Forwarding is disabled, ignoring SMS.")
                    return@launch
                }

                val currentHashes = container.settingsRepository.recentHashesFlow.first()
                val messageHash = (sender + body).hashCode().toString()
                if (currentHashes.contains(messageHash)) {
                    Log.d("Forwarding", "Duplicate SMS ignored.")
                    return@launch
                }
                
                val split = currentHashes.split(",").filter { it.isNotEmpty() }.takeLast(49)
                container.settingsRepository.updateRecentHashes((split + listOf(messageHash)).joinToString(","))

                val targetPhone = container.settingsRepository.targetPhoneFlow.first()
                val targetEmail = container.settingsRepository.targetEmailFlow.first()
                val senderEmail = container.settingsRepository.senderEmailFlow.first()
                val appPassword = container.settingsRepository.appPasswordFlow.first()
                val emailForwardingEnabled = container.settingsRepository.emailForwardingEnabledFlow.first()

                var status = "Success"
                
                if (targetPhone.isNotBlank()) {
                    try {
                        val smsManager = context.getSystemService(SmsManager::class.java)
                        val formattedMsg = "From: $sender\nMessage: $body"
                        val parts = smsManager.divideMessage(formattedMsg)
                        smsManager.sendMultipartTextMessage(targetPhone, null, parts, null, null)
                    } catch (e: Exception) {
                        status = "SMS Failed"
                        Log.e("Forwarding", "SMS Forwarding failed", e)
                    }
                }

                if (emailForwardingEnabled && targetEmail.isNotBlank() && senderEmail.isNotBlank() && appPassword.isNotBlank()) {
                    val subject = "Forwarded SMS from $sender"
                    val emailSuccess = container.emailClient.sendEmailWithRetry(
                        senderEmail = senderEmail,
                        appPassword = appPassword,
                        targetEmail = targetEmail,
                        subject = subject,
                        body = "From: $sender\n\n$body"
                    )
                    if (!emailSuccess) {
                        status = if (status == "Success") "Email Failed" else "SMS & Email Failed"
                    }
                } else if (targetPhone.isBlank()) {
                    status = "No Target Configured"
                }

                container.logRepository.insertLog(sender, body, status)
            }
        }
    }
}
