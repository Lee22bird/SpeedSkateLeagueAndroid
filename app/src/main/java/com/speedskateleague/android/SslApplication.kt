package com.speedskateleague.android

import android.app.Application
import com.speedskateleague.android.network.SslApiClient

class SslApplication : Application() {
    lateinit var apiClient: SslApiClient

    override fun onCreate() {
        super.onCreate()
        apiClient = SslApiClient(this)
    }
}
