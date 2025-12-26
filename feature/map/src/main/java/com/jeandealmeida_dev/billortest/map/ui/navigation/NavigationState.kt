package com.jeandealmeida_dev.billortest.map.ui.navigation

import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.RouterFailure

/**
 * Represents the current state of navigation
 */
enum class NavigationState {
    IDLE,
    CALCULATING_ROUTE,
    NAVIGATING,
    PAUSED
}

/**
 * Sealed class representing the result of a navigation operation
 */
sealed class NavigationResult {
    data class Success(val routes: List<NavigationRoute>) : NavigationResult()
    data class Error(val failure: List<RouterFailure>) : NavigationResult()
    object Cancelled : NavigationResult()
}
