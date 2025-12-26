package com.jeandealmeida_dev.billortest.location.ui.handler

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jeandealmeida_dev.billortest.location.data.repository.LocationRepository
import com.jeandealmeida_dev.billortest.location.domain.model.GeoPoint
import javax.inject.Inject

class LocationHandler @Inject constructor(
    private val context: Context,
    private val locationRepository: LocationRepository
) : DefaultLifecycleObserver {

    private var callback: LocationCallback? = null
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private var cancellationTokenSource: CancellationTokenSource? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null

    fun setCallback(callback: LocationCallback?) {
        this.callback = callback
    }

    fun startLocationFlow() {
        if (hasPermission()) {
            executeGetLocation()
        } else {
            callback?.onPermissionNeeded()
        }
    }

    fun requestPermission() {
        permissionLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    private fun executeGetLocation() {
        // Create a new CancellationTokenSource for each location request
        cancellationTokenSource = CancellationTokenSource()
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            location?.let {
                val geo = GeoPoint(it.latitude, it.longitude)
                locationRepository.updateLocation(geo)
                callback?.onSuccess(geo)
            } ?: callback?.onFailure(LocationError.LocationNull)
        }.addOnFailureListener {
            callback?.onFailure(LocationError.NetworkError(it))
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        val registry = (owner as? Fragment)?.requireActivity()?.activityResultRegistry ?: return

        permissionLauncher = registry.register(
            "location_key_${owner.hashCode()}",
            owner,
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) executeGetLocation()
            else callback?.onFailure(LocationError.PermissionDenied)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        cancellationTokenSource?.cancel()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        cancellationTokenSource?.cancel()
        cancellationTokenSource = null
        callback = null
        permissionLauncher?.unregister()
        permissionLauncher = null
    }

    sealed class LocationError {
        object PermissionDenied : LocationError()
        object LocationDisabled : LocationError()
        object LocationNull : LocationError()
        data class NetworkError(val cause: Throwable) : LocationError()
    }

    interface LocationCallback {
        fun onPermissionNeeded()
        fun onSuccess(geoPoint: GeoPoint)
        fun onFailure(error: LocationError)
    }
}