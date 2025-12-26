package com.jeandealmeida_dev.billortest.map.ui.navigation

import android.content.res.Resources
import com.jeandealmeida_dev.billortest.map.ui.components.BillorMapView
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource

/**
 * Manages navigation camera and viewport operations
 */
class CameraManager(
    private val mapView: BillorMapView
) {
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private val viewportDataSource: MapboxNavigationViewportDataSource =
        MapboxNavigationViewportDataSource(mapView.mapboxMap)
            .also {
                it.followingPadding = followingPadding
            }
    
    private val navigationCamera: NavigationCamera = NavigationCamera(
        mapView.mapboxMap,
        mapView.camera,
        viewportDataSource
    )

    fun getViewportDataSource(): MapboxNavigationViewportDataSource = viewportDataSource
    
    fun getNavigationCamera(): NavigationCamera = navigationCamera

    /**
     * Request camera to follow navigation
     */
    fun requestFollowingMode() {
        navigationCamera.requestNavigationCameraToFollowing()
    }

    /**
     * Reset camera frame to initial state
     */
    fun resetFrame() {
        navigationCamera.resetFrame()
    }
}
