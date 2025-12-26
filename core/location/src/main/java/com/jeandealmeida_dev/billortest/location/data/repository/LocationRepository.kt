package com.jeandealmeida_dev.billortest.location.data.repository

import com.jeandealmeida_dev.billortest.location.domain.model.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor() {
    
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)

    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    fun updateLocation(point: GeoPoint?) {
        point?.let {
            require(it.latitude in -90.0..90.0) { "Invalid latitude" }
            require(it.longitude in -180.0..180.0) { "Invalid longitude" }
        }
        _currentLocation.value = point
    }

    fun getCurrentLocationValue(): GeoPoint? {
        return _currentLocation.value
    }
}
