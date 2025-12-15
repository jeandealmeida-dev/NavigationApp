package com.jeandealmeida_dev.billortest.map.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jeandealmeida_dev.billortest.commons.extensions.LocationUtils.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.jeandealmeida_dev.billortest.commons.extensions.hideKeyboard
import com.jeandealmeida_dev.billortest.commons.extensions.isLocationPermissionGranted
import com.jeandealmeida_dev.billortest.commons.extensions.requestLocationPermission
import com.jeandealmeida_dev.billortest.map.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.autofill.AddressAutofillResult
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment that displays a Mapbox map with user location and address search with autocomplete
 *
 * Features:
 * - Requests location permissions at runtime
 * - Gets user's current location
 * - Animates camera progressively from world view to street level
 * - Shows user location on the map
 * - Supports two-finger vertical scroll for tilt/pitch (like Google Maps)
 * - Address search with autocomplete using Mapbox Autofill UI
 * - Real-time address suggestions as user types
 * - Animates to selected locations
 *
 * Note: Mapbox token is configured via string resource 'mapbox_access_token'
 * which is read from local.properties during build time.
 */
@AndroidEntryPoint
class MapFragment : Fragment() {

    private var mapView: BillorMapView? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    // Search UI components
    private var searchEditText: EditText? = null
    private var clearButton: ImageButton? = null
    private var searchResultsView: SearchResultsView? = null

    // Mapbox Search components
    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteUiAdapter
    private lateinit var placeAutocomplete: PlaceAutocomplete

    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private var ignoreNextQueryUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_map,
            container,
            false
        )

        mapView = view.findViewById(R.id.mapView)
        searchEditText = view.findViewById(R.id.searchEditText)
        clearButton = view.findViewById(R.id.clearButton)
        searchResultsView = view.findViewById(R.id.searchResultsView)
        searchPlaceView = view.findViewById(R.id.search_place_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMapview()
        setupSearchBar()
        setupSearchResultView()
    }

    /**
     * Setup search bar functionality with AddressAutofill
     */
    private fun setupSearchBar() {
        val resultsView = searchResultsView ?: return

        placeAutocomplete = PlaceAutocomplete.create()
        placeAutocompleteAdapter = PlaceAutocompleteUiAdapter(
            view = resultsView,
            placeAutocomplete = placeAutocomplete
        )
        searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

            isFavoriteButtonVisible = false

            addOnCloseClickListener {
                hide()
                closePlaceCard()
            }

            addOnNavigateClickListener { searchPlace ->
                //startActivity(geoIntent(searchPlace.coordinate))
            }

            addOnShareClickListener { searchPlace ->
                //startActivity(shareIntent(searchPlace))
            }
        }

        placeAutocompleteAdapter.addSearchListener(object :
            PlaceAutocompleteUiAdapter.SearchListener {
            override fun onError(e: Exception) {}

            override fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion) {
                searchEditText?.setText(suggestion.name)
            }

            override fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
                openPlaceCard(suggestion)
            }

            override fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>) {}

        })


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val response = placeAutocomplete.suggestions(query = "Joinville")
                response.onValue { suggestions ->
                    processSuggestions()
                }
            }
        }

        // Handle text changes for autocomplete
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                clearButton?.visibility = if (query.isBlank()) View.GONE else View.VISIBLE

                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false
                } else {
                    closePlaceCard()
                }

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED){
                        placeAutocompleteAdapter.search(query)
                        searchResultsView?.isVisible = query.isNotBlank()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear button functionality
        clearButton?.setOnClickListener {
            searchEditText?.text?.clear()
            searchResultsView?.visibility = View.GONE
        }
    }

    private fun setupSearchResultView() {
        searchResultsView?.initialize(
            SearchResultsView.Configuration(
                CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )
    }

    private fun processSuggestions() {

    }

    private fun setupMapview() {
        mapView?.setup {
            checkLocationPermissionAndGetLocation()
        }
    }

    /**
     * Check location permission and get location if permission is granted
     * Uses LocationExt extension functions for permission handling
     */
    private fun checkLocationPermissionAndGetLocation() {
        requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE) {
            // Permission already granted, get location
            getCurrentLocationAndAnimateCamera()
        }
    }

    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (isLocationPermissionGranted()) {
                    getCurrentLocationAndAnimateCamera()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission is required to show your location",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun openPlaceCard(suggestion: PlaceAutocompleteSuggestion) {
        ignoreNextQueryUpdate = true
        searchEditText?.setText("")

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                placeAutocomplete.select(suggestion).onValue { result ->
                    //mapMarkersManager.showMarker(result.coordinate)
                    mapView?.animateCameraToLocation(result.coordinate)
                    searchPlaceView.open(SearchPlace.createFromPlaceAutocompleteResult(result))
                    searchEditText?.hideKeyboard()
                    searchResultsView?.isVisible = false
                }.onError { error ->
                    //Log.d(LOG_TAG, "Suggestion selection error", error)
                    //showToast(R.string.place_autocomplete_selection_error)
                }
            }
        }
    }

    private fun geoIntent(point: Point): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))

    /**
     * Handle address selection from autocomplete
     */
    private fun handleAddressSelected(result: AddressAutofillResult) {
        val coordinate = result.coordinate
        searchEditText?.setText(result.address.place)

        // Animate camera to selected location
        val point = Point.fromLngLat(coordinate.longitude(), coordinate.latitude())
        mapView?.camera?.flyTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build(),
            MapAnimationOptions.Builder()
                .duration(2000)
                .build()
        )

        Toast.makeText(context, result.address.place, Toast.LENGTH_SHORT).show()
    }

    /**
     * Get current location and animate camera progressively to street level
     * Note: This method should only be called after permission is granted
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndAnimateCamera() {
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = Point.fromLngLat(it.longitude, it.latitude)

                // Enable location component using BillorMap extension
                //mapView?.enableLocationComponent(pulsingEnabled = true)

                // Animate camera progressively from world view to street level
                mapView?.animateCameraToLocation(userLocation)
            } ?: run {
                Toast.makeText(
                    context,
                    "Unable to get current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                context,
                "Location error: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun closePlaceCard() {
        searchResultsView?.isVisible = false
        //mapMarkersManager.clearMarkers()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        cancellationTokenSource?.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        mapView = null
        cancellationTokenSource?.cancel()
        cancellationTokenSource = null
    }
}
