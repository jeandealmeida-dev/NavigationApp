package com.jeandealmeida_dev.billortest.map.ui.navigation

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper

/**
 * Manages replay functionality for navigation simulation
 */
class ReplayManager {
    private val replayRouteMapper = ReplayRouteMapper()
    private var replayProgressObserver: ReplayProgressObserver? = null

    companion object {
        private const val REPLAY_ENABLED = true
    }

    /**
     * Start replay session with the given route
     */
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    fun startReplay(navigation: MapboxNavigation, route: NavigationRoute) {
        val replayData = replayRouteMapper.mapDirectionsRouteGeometry(route.directionsRoute)
        
        if (replayData.isNotEmpty()) {
            navigation.mapboxReplayer.play()
            navigation.mapboxReplayer.pushEvents(replayData)
            navigation.mapboxReplayer.playFirstLocation()
            navigation.startReplayTripSession()
        }
    }

    /**
     * Stop replay session
     */
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    fun stopReplay(navigation: MapboxNavigation) {
        navigation.mapboxReplayer.stop()
        navigation.mapboxReplayer.clearEvents()
    }

    /**
     * Check if replay mode is enabled
     */
    fun isReplayEnabled(): Boolean = REPLAY_ENABLED

    /**
     * Set replay progress observer
     */
    fun setReplayProgressObserver(observer: ReplayProgressObserver?) {
        replayProgressObserver = observer
    }

    /**
     * Get replay progress observer
     */
    fun getReplayProgressObserver(): ReplayProgressObserver? = replayProgressObserver
}
