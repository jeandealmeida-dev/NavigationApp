package com.jeandealmeida_dev.billortest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for BillorTest
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class BillorTestApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization code here
    }
}
