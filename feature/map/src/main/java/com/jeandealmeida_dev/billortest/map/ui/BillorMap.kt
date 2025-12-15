package com.jeandealmeida_dev.billortest.map.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions

class BillorMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr) {

    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var replayProgressObserver: ReplayProgressObserver
    private val replayRouteMapper = ReplayRouteMapper()

    fun setup(onFinish: () -> Unit) {
        configureGestures()
        configureStyle(onStyleLoaded = {
            onFinish()
        })
        configureLocation()
        configureNatigation()
    }

    private fun configureGestures() {
        // Configure gestures
        gestures.updateSettings {
            pitchEnabled = true
            doubleTapToZoomInEnabled = true
            scrollEnabled = true
        }
    }

    fun configureStyle(onStyleLoaded: (() -> Unit)? = null) {
        mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            onStyleLoaded?.invoke()
        }
    }

    fun configureLocation() {
        location.updateSettings {
            enabled = true
            this.pulsingEnabled = true
        }
    }

    fun configureNatigation() {
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        location.apply {
            setLocationProvider(navigationLocationProvider)
            locationPuck = LocationPuck2D()
            enabled = true
        }
        val pixelDensity = this.resources.displayMetrics.density
        viewportDataSource.followingPadding =
            EdgeInsets(
                180.0 * pixelDensity,
                40.0 * pixelDensity,
                150.0 * pixelDensity,
                40.0 * pixelDensity
            )

        // initialize a NavigationCamera
        navigationCamera = NavigationCamera(mapboxMap, camera, viewportDataSource)

        // Initialize route line api and view for drawing the route on the map
        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
        routeLineView = MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(context).build())
    }

    /**
     * Animate camera progressively from current position to street level view
     */
    fun animateCameraToLocation(point: Point) {
        val duration = if(cameraDistance > 17) {
            8000L
        } else if (cameraDistance > 14) {
            4000L
        } else {
            2000L
        }
        camera.flyTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(17.0)
                .build(),
            MapAnimationOptions.Builder()
                .duration(duration)
                .build()
        )
    }

    // routes observer draws a route line and origin/destination circles on the map
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(routeUpdateResult.navigationRoutes) { value ->
                mapboxMap.style?.apply { routeLineView.renderRouteDrawData(this, value) }
            }

            // update viewportSourceData to include the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()

            // set the navigationCamera to OVERVIEW
            navigationCamera.requestNavigationCameraToOverview()
        }
    }

    // locationObserver updates the location puck and camera to follow the user's location
    private val locationObserver =
        object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {}

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhancedLocation = locationMatcherResult.enhancedLocation
                // update location puck's position on the map
                navigationLocationProvider.changePosition(
                    location = enhancedLocation,
                    keyPoints = locationMatcherResult.keyPoints,
                )

                // update viewportDataSource to trigger camera to follow the location
                viewportDataSource.onLocationChanged(enhancedLocation)
                viewportDataSource.evaluate()

                // set the navigationCamera to FOLLOWING
                navigationCamera.requestNavigationCameraToFollowing()
            }
        }

    // define MapboxNavigation
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onCreatedObserver = null,
        onStartedObserver = null,
        onResumedObserver =
            object : MapboxNavigationObserver {
                @SuppressLint("MissingPermission")
                override fun onAttached(mapboxNavigation: MapboxNavigation) {
                    // register observers
                    mapboxNavigation.registerRoutesObserver(routesObserver)
                    mapboxNavigation.registerLocationObserver(locationObserver)

                    replayProgressObserver =
                        ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                    mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                    mapboxNavigation.startReplayTripSession()
                }

                override fun onDetached(mapboxNavigation: MapboxNavigation) {}
            },
        onInitialize = initNavigation()
    )

    // on initialization of MapboxNavigation, request a route between two fixed points
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun initNavigation() {
        MapboxNavigationApp.setup(NavigationOptions.Builder(context).build())

        // initialize location puck
        location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = createDefault2DPuck()
            enabled = true
        }

        val origin = Point.fromLngLat(-122.43539772352648, 37.77440680146262)
        val destination = Point.fromLngLat(-122.42409811526268, 37.76556957793795)

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    mapboxNavigation.setNavigationRoutes(routes)

                    // start simulated user movement
                    val replayData =
                        replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
                    mapboxNavigation.mapboxReplayer.pushEvents(replayData)
                    mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
                    mapboxNavigation.mapboxReplayer.play()
                }
            }
        )
    }
}