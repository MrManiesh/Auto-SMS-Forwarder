package com.example.smsforwarder

import android.app.Application

class SmsForwarderApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
