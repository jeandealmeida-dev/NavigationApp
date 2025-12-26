package com.jeandealmeida_dev.billortest.location.ui.extension

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat

fun Context.isGpsEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
}

fun Context.isLocationEnabled(): Boolean {
    val locationManager = ContextCompat.getSystemService(this, LocationManager::class.java)
    return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
}

fun Context.openLocationSettings() {
    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}