package com.jeandealmeida_dev.billortest.map.ui.search

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jeandealmeida_dev.billortest.commons.ui.extensions.hideKeyboard
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import kotlinx.coroutines.launch

/**
 * Manages search functionality including autocomplete, suggestions, and place selection
 */
class SearchManager(
    private val lifecycleOwner: LifecycleOwner,
    private val searchEditText: EditText,
    private val clearButton: ImageButton,
    private val searchResultsView: SearchResultsView,
    private val searchPlaceView: SearchPlaceBottomSheetView
) {
    private lateinit var placeAutocompleteAdapter: PlaceAutocompleteUiAdapter
    private lateinit var placeAutocomplete: PlaceAutocomplete
    private var ignoreNextQueryUpdate = false

    private var onPlaceSelectedListener: ((Point) -> Unit)? = null
    private var onNavigateClickListener: ((Point) -> Unit)? = null
    private var onShareClickListener: ((SearchPlace) -> Unit)? = null
    private var onCloseClickListener: (() -> Unit)? = null

    /**
     * Initialize the search manager and set up all search-related UI components
     */
    fun initialize() {
        setupPlaceAutocomplete()
        setupSearchPlaceView()
        setupTextWatcher()
        setupClearButton()
        setupSearchResultsView()
    }

    fun setupListeners(
        onPlaceSelectedListener: ((Point) -> Unit)? = null,
        onNavigateClickListener: ((Point) -> Unit)? = null,
        onShareClickListener: ((SearchPlace) -> Unit)? = null,
        onCloseClickListener: (() -> Unit)? = null
    ) {
        this.onPlaceSelectedListener = onPlaceSelectedListener
        this.onNavigateClickListener = onNavigateClickListener
        this.onShareClickListener = onShareClickListener
        this.onCloseClickListener = onCloseClickListener
    }

    private fun setupPlaceAutocomplete() {
        placeAutocomplete = PlaceAutocomplete.create()
        placeAutocompleteAdapter = PlaceAutocompleteUiAdapter(
            view = searchResultsView,
            placeAutocomplete = placeAutocomplete
        )

        placeAutocompleteAdapter.addSearchListener(object :
            PlaceAutocompleteUiAdapter.SearchListener {
            override fun onError(e: Exception) {
                // Handle search error
            }

            override fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion) {
                searchEditText.setText(suggestion.name)
            }

            override fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
                openPlaceCard(suggestion)
            }

            override fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>) {
                // Handle suggestions shown
            }
        })
    }

    private fun setupSearchPlaceView() {
        searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isFavoriteButtonVisible = false

            addOnCloseClickListener {
                hide()
                closePlaceCard()
                onCloseClickListener?.invoke()
            }

            addOnNavigateClickListener { searchPlace ->
                onNavigateClickListener?.invoke(searchPlace.coordinate)
            }

            addOnShareClickListener { searchPlace ->
                onShareClickListener?.invoke(searchPlace)
            }
        }
    }

    private fun setupTextWatcher() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                clearButton.visibility = if (query.isBlank()) View.GONE else View.VISIBLE

                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false
                } else {
                    closePlaceCard()
                }

                lifecycleOwner.lifecycleScope.launch {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        placeAutocompleteAdapter.search(query)
                        searchResultsView.isVisible = query.isNotBlank()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            searchEditText.text?.clear()
            searchResultsView.visibility = View.GONE
        }
    }

    private fun setupSearchResultsView() {
        searchResultsView.initialize(
            SearchResultsView.Configuration(
                CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )
    }

    private fun openPlaceCard(suggestion: PlaceAutocompleteSuggestion) {
        ignoreNextQueryUpdate = true
        searchEditText.setText("")

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                placeAutocomplete.select(suggestion).onValue { result ->
                    val coordinate = result.coordinate
                    onPlaceSelectedListener?.invoke(coordinate)
                    searchPlaceView.open(SearchPlace.createFromPlaceAutocompleteResult(result))
                    searchEditText.hideKeyboard()
                    searchResultsView.isVisible = false
                }.onError { error ->
                    // Handle selection error
                }
            }
        }
    }

    private fun closePlaceCard() {
        searchResultsView.isVisible = false
    }

    /**
     * Close the place card view
     */
    fun close() {
        searchPlaceView.hide()
        closePlaceCard()
    }
}
