package com.example.smsforwarder.network

import android.util.Log
import kotlinx.coroutines.delay
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailClient {

    suspend fun sendEmailWithRetry(
        senderEmail: String,
        appPassword: String,
        targetEmail: String,
        subject: String,
        body: String,
        retries: Int = 3
    ): Boolean {
        var currentAttempt = 0
        
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")
            put("mail.smtp.auth", "true")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, appPassword)
            }
        })

        while (currentAttempt < retries) {
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail))
                    setSubject(subject)
                    setText(body)
                }

                Transport.send(message)
                return true
            } catch (e: Exception) {
                Log.e("EmailClient", "Exception sending email on attempt $currentAttempt", e)
            }
            currentAttempt++
            if (currentAttempt < retries) {
                delay(2000L * currentAttempt) // exponential backoff
            }
        }
        return false
    }
}
