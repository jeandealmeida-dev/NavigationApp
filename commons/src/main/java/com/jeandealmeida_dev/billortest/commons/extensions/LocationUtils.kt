package com.jeandealmeida_dev.billortest.commons.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

class LocationUtils(private val fragment: Fragment): BroadcastReceiver() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private val LOCATION_PROVIDERS_CHANGED_REGEX = "android.location.PROVIDERS_CHANGED".toRegex()
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(fragment.requireContext())
    }

    private var onLocationTriggerChanged: ((isLocationEnable: Boolean) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun getLocation(
        onGetLocation: (Double, Double) -> Unit,
        onFailureGetLocation: (() -> Unit)? = null
    ) {
        if (!isLocationAccessEnable()) {
            onFailureGetLocation?.invoke()
            return
        }
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onGetLocation.invoke(location.latitude, location.longitude)
                } else {
                    onFailureGetLocation?.invoke()
                }
            }
            .addOnFailureListener {
                onFailureGetLocation?.invoke()
            }
    }

    fun checkLocationPermission(permissionAllowed: (() -> Unit)? = null) {
        fragment.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE, permissionAllowed)
    }

    fun isLocationAccessEnable(): Boolean {
        return fragment.isLocationAccessEnabled()
    }


    fun setupGps(activity: FragmentActivity?, onLocationTriggerChanged: ((isLocationEnable: Boolean) -> Unit)?) {
        activity?.registerReceiver(
            this,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
        this.onLocationTriggerChanged = onLocationTriggerChanged

        if(isLocationAccessEnable()) {
            this.onLocationTriggerChanged?.invoke(true)
        } else {
            this.onLocationTriggerChanged?.invoke(false)
            checkLocationPermission {
                onLocationTriggerChanged?.invoke(true)
            }
        }
    }

    fun unregisterReceiver(activity: FragmentActivity?) {
        try {
            activity?.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) { }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action?.matches(LOCATION_PROVIDERS_CHANGED_REGEX) == true) {
            onLocationTriggerChanged?.invoke(isLocationAccessEnable())
        }
    }
}