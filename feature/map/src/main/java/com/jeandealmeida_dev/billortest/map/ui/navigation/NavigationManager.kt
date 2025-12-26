package com.jeandealmeida_dev.billortest.map.ui.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import com.jeandealmeida_dev.billortest.location.data.repository.LocationRepository
import com.jeandealmeida_dev.billortest.map.ui.components.BillorMapView
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateValue
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

/**
 * Manages navigation functionality with separated concerns
 * - Route calculation and rendering via RouteManager
 * - Camera operations via CameraManager
 * - Replay simulation via ReplayManager
 * - Navigation state management via StateFlow
 */
@Singleton
class NavigationManager(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository
) : DefaultLifecycleObserver {

    private val navigationLocationProvider = NavigationLocationProvider()

    private var mapboxNavigation: MapboxNavigation? = null
    private lateinit var mapView: BillorMapView

    // Separated responsibility managers
    private var routeManager: RouteManager? = null
    private var cameraManager: CameraManager? = null
    private val replayManager = ReplayManager()

    // Navigation state
    private val _navigationState = MutableStateFlow(NavigationState.IDLE)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Trip progress API
    private lateinit var tripProgressApi: MapboxTripProgressApi

    // Listeners
    private var onProgressUpdate: ((tripProgressUpdateValue: TripProgressUpdateValue) -> Unit)? =
        null

    /**
     * Setup navigation with MapboxNavigation instance and map view
     */
    fun setup(navigation: MapboxNavigation, view: BillorMapView) {
        this.mapboxNavigation = navigation
        this.mapView = view

        // Initialize managers
        routeManager = RouteManager(context)
        cameraManager = CameraManager(view)
        tripProgressApi =
            MapboxTripProgressApi(TripProgressUpdateFormatter.Builder(context).build())

        // Configure location provider and puck
        view.location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = createDefault2DPuck()
            enabled = true
            pulsingEnabled = true
            layerAbove = null
        }


        // Register observers
        navigation.registerRoutesObserver(routesObserver)
        navigation.registerLocationObserver(locationObserver)
        navigation.registerRouteProgressObserver(routeProgressObserver)
    }

    /**
     * Start navigation to destination with route calculation
     */
    fun startNavigation(destination: Point, isReplay: Boolean) {
        val navigation = mapboxNavigation ?: return
        val origin = locationRepository.getCurrentLocationValue() ?: return
        val originPoint = Point.fromLngLat(origin.longitude, origin.latitude)

        _navigationState.value = NavigationState.CALCULATING_ROUTE

        navigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(originPoint, destination))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(
                    routeOptions: RouteOptions,
                    routerOrigin: String
                ) {
                    _navigationState.value = NavigationState.IDLE
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    _navigationState.value = NavigationState.IDLE
                }

                @SuppressLint("MissingPermission")
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    startNavigationWithRoutes(routes, isReplay)
                }
            }
        )
    }

    /**
     * Start navigation with pre-calculated routes
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startNavigationWithRoutes(routes: List<NavigationRoute>, isReplay: Boolean) {
        val navigation = mapboxNavigation ?: return
        navigation.setNavigationRoutes(routes)

        navigation.stopTripSession()
        replayManager.stopReplay(navigation)

        if (isReplay) {
            replayManager.startReplay(navigation, routes.first())
        } else {
            navigation.startTripSession()
        }

        _navigationState.value = NavigationState.NAVIGATING
    }

    /**
     * Stop current navigation session
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    fun stopNavigation() {
        val navigation = mapboxNavigation ?: return

        navigation.stopTripSession()
        replayManager.stopReplay(navigation)

        routeManager?.clearRouteLine(mapView)
        cameraManager?.resetFrame()

        navigation.startTripSession()
        _navigationState.value = NavigationState.IDLE
    }

    /**
     * Detach MapboxNavigation instance and unregister observers
     */
    fun detachNavigation(mapboxNavigation: MapboxNavigation) {
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        replayManager.getReplayProgressObserver()?.let {
            mapboxNavigation.unregisterRouteProgressObserver(it)
        }
        routeManager?.clearRouteLine(mapView)
        this.mapboxNavigation = null
        _navigationState.value = NavigationState.IDLE
    }

    /**
     * Set listener for trip progress updates
     */
    fun setOnProgressUpdateListener(listener: (TripProgressUpdateValue) -> Unit) {
        this.onProgressUpdate = listener
    }

    /**
     * Get navigation location provider
     */
    fun getLocationProvider(): LocationProvider = navigationLocationProvider

    /**
     * Update navigation location provider with initial user location
     */
    fun updateInitialLocation(point: Point) {
        val location = Location.Builder()
            .latitude(point.latitude())
            .longitude(point.longitude())
            .build()
        navigationLocationProvider.changePosition(location)
    }

    /**
     * Routes observer draws route line and manages camera
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            val routeLineApi = routeManager?.getRouteLineApi()
            val routeLineView = routeManager?.getRouteLineView()
            val viewportDataSource = cameraManager?.getViewportDataSource()

            routeLineApi?.setNavigationRoutes(routeUpdateResult.navigationRoutes) { value ->
                mapView.mapboxMap.style?.apply {
                    routeLineView?.renderRouteDrawData(this, value)
                }
            }

            viewportDataSource?.onRouteChanged(routeUpdateResult.navigationRoutes.first())
        }
    }

    /**
     * Location observer updates the location puck and camera
     */
    private val locationObserver = object : LocationObserver {
        private var firstValidPosition = true
        override fun onNewRawLocation(rawLocation: Location) {}

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            if (enhancedLocation.latitude == 0.0 || enhancedLocation.longitude == 0.0) return

            val viewportDataSource = cameraManager?.getViewportDataSource()

            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            viewportDataSource?.onLocationChanged(enhancedLocation)
            viewportDataSource?.evaluate()

            if (_navigationState.value == NavigationState.NAVIGATING && firstValidPosition) {
                cameraManager?.requestFollowingMode()
                firstValidPosition = false
            }
        }
    }

    /**
     * Route progress observer for trip progress updates
     */
    private val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            val tripProgressData = tripProgressApi.getTripProgress(routeProgress)
            onProgressUpdate?.invoke(tripProgressData)
        }
    }
}
