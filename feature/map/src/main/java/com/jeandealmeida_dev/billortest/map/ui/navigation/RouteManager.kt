package com.jeandealmeida_dev.billortest.map.ui.navigation

import android.content.Context
import com.jeandealmeida_dev.billortest.map.ui.components.BillorMapView
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions

/**
 * Manages route line rendering on the map
 */
class RouteManager(
    private val context: Context
) {
    private val routeLineApi: MapboxRouteLineApi = 
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    
    private val routeLineView: MapboxRouteLineView = 
        MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context)
            .routeLineBelowLayerId("road-label")
            .build())

    fun getRouteLineApi(): MapboxRouteLineApi = routeLineApi
    
    fun getRouteLineView(): MapboxRouteLineView = routeLineView

    /**
     * Clear route line from the map
     */
    fun clearRouteLine(mapView: BillorMapView) {
        mapView.mapboxMap.style?.let { style ->
            routeLineApi.clearRouteLine { 
                // Route line cleared - no need to render
            }
        }
    }
}
