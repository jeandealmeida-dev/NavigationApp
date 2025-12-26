package com.jeandealmeida_dev.billortest

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BillorTestApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        MapboxNavigationApp.setup(
            navigationOptions = NavigationOptions.Builder(this)
                .isDebugLoggingEnabled(true)
                .build()
        )
    }
}
