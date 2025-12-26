package com.jeandealmeida_dev.billortest.map.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.jeandealmeida_dev.billortest.map.ui.navigation.NavigationManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar

/**
 * Custom MapView with integrated navigation functionality
 * Separates concerns: map configuration, location, and navigation
 */
class BillorMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr) {

    /**
     * Setup the map with all necessary configurations
     */
    fun setup(onFinish: () -> Unit) {
        configureGestures()
        configureStyle(onStyleLoaded = {
            onFinish()
        })
    }

    /**
     * Configure map gestures (pitch, zoom, scroll)
     */
    private fun configureGestures() {
        gestures.updateSettings {
            pitchEnabled = true
            doubleTapToZoomInEnabled = true
            scrollEnabled = true
        }
    }

    /**
     * Load map style
     */
    private fun configureStyle(onStyleLoaded: (() -> Unit)? = null) {
        scalebar.enabled = false
        mapboxMap.loadStyle(Style.Companion.MAPBOX_STREETS) { style ->
            onStyleLoaded?.invoke()
        }
    }

    /**
     * Animate camera progressively from current position to street level view
     */
    fun animateCameraToLocation(point: Point?) {
        if (point == null) return

        val TARGET_ZOOM = 17.0
        val DURATION_PER_ZOOM_UNIT = 400L
        val MIN_DURATION = 2000L // 2 segundos
        val MAX_DURATION = 6000L // 6 segundos

        val zoomDelta = cameraDistance + TARGET_ZOOM
        val calculatedDuration = (zoomDelta * DURATION_PER_ZOOM_UNIT).toLong()

        val finalDuration = calculatedDuration
            .coerceAtLeast(MIN_DURATION)
            .coerceAtMost(MAX_DURATION)

        camera.flyTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(TARGET_ZOOM)
                .build(),
            MapAnimationOptions.Builder()
                .duration(finalDuration)
                .build()
        )
    }

    /**
     * Get current camera distance (zoom level)
     */
    private val cameraDistance: Double
        get() = mapboxMap.cameraState.zoom
}