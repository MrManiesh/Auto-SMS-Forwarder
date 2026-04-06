package com.example.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.smsforwarder.service.ForwardingService

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sender = messages[0]?.originatingAddress ?: "Unknown"
            val body = messages.joinToString(separator = "") { it?.messageBody ?: "" }

            Log.d("SmsReceiver", "Received SMS from $sender")

            ForwardingService.processIncomingSms(context, sender, body)
        }
    }
}
