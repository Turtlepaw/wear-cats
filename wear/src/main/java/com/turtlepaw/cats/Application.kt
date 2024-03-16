package com.turtlepaw.cats

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder().build()
        )
    }
}