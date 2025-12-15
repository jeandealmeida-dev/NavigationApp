package com.jeandealmeida_dev.billortest.commons.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

/**
 * Request location permission
 * @param requestCode The request code to identify the permission request
 * @param permissionAllowed Optional callback when permission is already granted
 */
fun Fragment.requestLocationPermission(
    requestCode: Int,
    permissionAllowed: (() -> Unit)? = null
) {
    if (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestCode
        )
    } else {
        permissionAllowed?.invoke()
    }
}

/**
 * Check if location permission is granted
 * @return true if ACCESS_FINE_LOCATION permission is granted, false otherwise
 */
fun Fragment.isLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Check if location access is fully enabled (permission granted AND GPS enabled)
 * @return true if both permission is granted and GPS is enabled, false otherwise
 */
fun Fragment.isLocationAccessEnabled(): Boolean {
    val isPermissionGranted = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true && isPermissionGranted
}
