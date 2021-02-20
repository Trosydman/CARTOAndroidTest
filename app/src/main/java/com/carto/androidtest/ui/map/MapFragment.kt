package com.carto.androidtest.ui.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.R
import com.carto.androidtest.databinding.FragmentMapBinding
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.toPx
import com.carto.androidtest.ui.MainEvents
import com.carto.androidtest.ui.MainStates.MapStates
import com.carto.androidtest.ui.MainViewModel
import com.carto.androidtest.ui.custom.PoiDetailsBottomSheet
import com.carto.androidtest.ui.custom.RouteDetailsView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val MAP_MIN_ZOOM = 6.5f
const val CAMERA_BOUNDS_PADDING = 60

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback, PoiDetailsBottomSheet.OnClickListener,
    RouteDetailsView.OnClickListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationButtonClickListener {

    enum class MapZoomLevel(val level: Float) {
        WORLD(1f),
        CONTINENT(5f),
        CITY(10f),
        STREETS(15f),
        BUILDINGS(20f)
    }

    private val onPoiDetailsVisibilityChangedListener =
        object : PoiDetailsBottomSheet.OnVisibilityChangedListener {
            override fun onShow() {
                // TODO
            }

            override fun onHide() {
                // TODO
            }

        }

    private val onRouteDetailsVisibilityChangedListener =
        object : RouteDetailsView.OnVisibilityChangedListener {
            override fun onShow() {
                // TODO
            }

            override fun onHide() {
                // TODO
            }

        }

    private val viewModel: MainViewModel by viewModels()

    private val binding: FragmentMapBinding
        get() = _binding!!
    private var _binding: FragmentMapBinding? = null

    private lateinit var map: GoogleMap
    private var currentLocationMarker: MarkerOptions? = null
    private var poiDetailsSheet: PoiDetailsBottomSheet? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMapBinding.bind(view)

        lifecycleScope.launchWhenStarted {
            initMap()
            viewModel.states.collect {
                when (it) {

                    is MapStates.ApplyInitialMapSetup -> {
                        applyInitialMapSetup()
                    }

                    is MapStates.AddCurrentFakeLocationMarker -> {
                        addCurrentLocationMarker(it.location)
                    }

                    else -> {
                        if (BuildConfig.DEBUG) {
                            throw IllegalStateException("Unknown state: ${it::class.java.simpleName}")
                        }
                    }
                }
            }
        }

        viewModel.pois.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val markerBounds = addMarkers(it)
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(markerBounds,
                    CAMERA_BOUNDS_PADDING.toPx()), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        map.setLatLngBoundsForCameraTarget(markerBounds)
                    }

                    override fun onCancel() {
                        // TODO
                    }
                })
            }
        }

        initViews()
    }

    private fun applyInitialMapSetup() {
        map.uiSettings.isMapToolbarEnabled = false
        map.setMinZoomPreference(MAP_MIN_ZOOM)
    }

    private fun addCurrentLocationMarker(location: LatLng) {
        val blueDotIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bluedot)
        currentLocationMarker = MarkerOptions()
            .position(location)
            .icon(blueDotIcon)

        map.addMarker(currentLocationMarker)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, MapZoomLevel.CONTINENT.level))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        poiDetailsSheet = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
        map.setOnMyLocationButtonClickListener(this)
        sendEvent(MainEvents.MapEvents.OnMapReady)
        // TODO
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        // TODO
        return false
    }

    override fun onMyLocationButtonClick(): Boolean {
        // TODO
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationMarker?.position, 15f))
        return false
    }

    override fun onDirectionsFabClicked() {
        // TODO
    }

    override fun onCloseButtonClicked() {
        // TODO
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun sendEvent(event: MainEvents) = lifecycleScope.launch {
        viewModel.eventsChannel.send(event)
    }

    private fun initViews() {
        with(binding) {
            poiDetailsSheet = PoiDetailsBottomSheet(poiDetailsBottomSheet.root).apply {
                onVisibilityChangedListener = onPoiDetailsVisibilityChangedListener
                onClickListener = this@MapFragment
            }

            currentLocationFAB.setOnClickListener {
                // TODO
                poiDetailsSheet?.show()
            }

            searchButton.setOnClickListener {
                // TODO
            }

            helpButton.setOnClickListener {
                // TODO
            }
        }
    }

    private fun addMarkers(pois: List<Poi>): LatLngBounds {
        val boundsBuilder = LatLngBounds.Builder()

        pois.forEach { poi ->
            val latLng = LatLng(poi.latitude, poi.longitude)
            val markerTitle = poi.id
            val newMarker = MarkerOptions()
                .title(markerTitle)
                .position(latLng)

            map.addMarker(newMarker)
            boundsBuilder.include(latLng)
        }

        return boundsBuilder.build()
    }
}
