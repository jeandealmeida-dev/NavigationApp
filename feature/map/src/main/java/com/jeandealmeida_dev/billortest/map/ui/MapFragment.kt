package com.jeandealmeida_dev.billortest.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import com.jeandealmeida_dev.billortest.commons.ui.handler.SpeechToTextHandler
import com.jeandealmeida_dev.billortest.location.domain.model.GeoPoint
import com.jeandealmeida_dev.billortest.location.ui.handler.LocationHandler
import com.jeandealmeida_dev.billortest.map.R
import com.jeandealmeida_dev.billortest.map.databinding.FragmentMapBinding
import com.jeandealmeida_dev.billortest.map.ui.navigation.NavigationManager
import com.jeandealmeida_dev.billortest.map.ui.search.SearchManager
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    @Inject
    lateinit var locationHandler: LocationHandler

    @Inject
    lateinit var speechToTextHandler: SpeechToTextHandler

    @Inject
    lateinit var navigationManager: NavigationManager

    // Search components
    private var searchManager: SearchManager? = null
    private var userLocation: Point? = null
    private var isNavigating: Boolean = false

    private var binding: FragmentMapBinding? = null

    /**
     * Define MapboxNavigation instance with lifecycle observers
     */
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                binding?.mapView?.let {
                    it.setup {
                        navigationManager.setup(mapboxNavigation, it)
                    }
                }
                locationHandler.startLocationFlow()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                navigationManager.detachNavigation(mapboxNavigation)
            }
        },
        onInitialize = {}
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLocationHandler()
        setupSpeechToTextHandler()

        setupOnBackPressed()
        setupSearchManager()
        setupVoiceSearch()
    }

    fun setupLocationHandler() {
        lifecycle.addObserver(locationHandler)
        locationHandler.setCallback(object : LocationHandler.LocationCallback {
            override fun onPermissionNeeded() {
                locationHandler.requestPermission()
            }

            override fun onSuccess(geoPoint: GeoPoint) {
                Point.fromLngLat(geoPoint.longitude, geoPoint.latitude).let { point ->
                    userLocation = point
                    navigationManager.updateInitialLocation(point)
                    binding?.mapView?.animateCameraToLocation(point)
                }
            }

            override fun onFailure(error: LocationHandler.LocationError) {
                // Handle location error
                when (error) {
                    is LocationHandler.LocationError.NetworkError -> error.cause.printStackTrace()
                    else -> {} // Handle other errors silently
                }
            }

        })
    }

    fun setupSpeechToTextHandler() {
        lifecycle.addObserver(speechToTextHandler)
        speechToTextHandler.setCallback(object : SpeechToTextHandler.SpeechToTextCallback {
            override fun onPermissionNeeded() {
                speechToTextHandler.requestPermission()
            }

            override fun onSuccess(recognizedText: String) {
                val searchEditText = view?.findViewById<EditText>(R.id.searchEditText) ?: return
                searchEditText.setText(recognizedText)
            }

            override fun onFailure(exception: Exception) {
                Toast.makeText(
                    context,
                    "Speech recognition error: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Setup search manager with all search-related functionality
     */
    private fun setupSearchManager() {
        binding?.let {
            SearchManager(
                lifecycleOwner = viewLifecycleOwner,
                searchEditText = it.searchEditText,
                clearButton = it.clearButton,
                searchResultsView = it.searchResultsView,
                searchPlaceView = it.searchPlaceView
            ).apply {
                initialize()
                setupListeners(
                    onPlaceSelectedListener = { coordinate ->
                        binding?.mapView?.animateCameraToLocation(coordinate)
                    },
                    onNavigateClickListener = { destination ->
                        searchManager?.close()
                        binding?.apply {
                            mapView.animateCameraToLocation(userLocation)
                            tripProgressView.visibility = View.VISIBLE
                        }
                        startNavigationToDestination(destination)
                    },
                    onShareClickListener = { searchPlace ->
                        startActivity(shareIntent(searchPlace.coordinate))
                    },
                    onCloseClickListener = {

                    }
                )
            }.also {
                searchManager = it
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
            if (isNavigating) {
                onExitNavigation()
                true
            } else {
                false
            }
        }
    }

    /**
     * Setup voice search functionality with speech-to-text
     */
    private fun setupVoiceSearch() {
        binding?.apply {
            voiceSearchButton.setOnClickListener {
                speechToTextHandler.startSpeechFlow()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun onExitNavigation() {
        navigationManager.stopNavigation()
        isNavigating = false
    }

    //region Actions

    /**
     * Start navigation from current user location to the selected destination
     */
    private fun startNavigationToDestination(destination: Point) {
        isNavigating = true
        navigationManager.startNavigation(
            destination = destination,
            isReplay = true
        )
        navigationManager.setOnProgressUpdateListener {
            binding?.tripProgressView?.render(it)
        }
    }

    /**
     * Create share intent for a location
     */
    private fun shareIntent(point: Point): Intent {
        val shareText =
            "Check out this location: https://maps.google.com/?q=${point.latitude()},${point.longitude()}"
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
    }

    //endregion

    //region Override Android

    override fun onStart() {
        super.onStart()
        binding?.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding?.mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.mapView?.onLowMemory()
    }

    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.mapView?.onDestroy()
        searchManager = null
        binding = null
    }
}
