package com.carto.androidtest.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.R
import com.carto.androidtest.databinding.FragmentMapBinding
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.toPx
import com.carto.androidtest.ui.CURRENT_FAKE_LOCATION_ID
import com.carto.androidtest.ui.MainEvents
import com.carto.androidtest.ui.MainEvents.MapEvents
import com.carto.androidtest.ui.MainStates.MapStates
import com.carto.androidtest.ui.MainViewModel
import com.carto.androidtest.ui.NewMainActivity
import com.carto.androidtest.ui.custom.PoiDetailsBottomSheet
import com.carto.androidtest.ui.custom.RouteDetailsView
import com.carto.androidtest.utils.extensions.beautifyDistance
import com.carto.androidtest.utils.extensions.beautifyTime
import com.carto.androidtest.utils.extensions.distanceTo
import com.carto.androidtest.utils.extensions.isMyCurrentLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

const val MAP_MIN_ZOOM = 6.5f

const val ROUTE_LINE_WIDTH = 5

const val CAMERA_BOUNDS_PADDING = 60
const val ROUTE_CAMERA_PADDING = 125

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback,
    PoiDetailsBottomSheet.OnClickListener, RouteDetailsView.OnClickListener,
    GoogleMap.OnMarkerClickListener, NewMainActivity.OnBackPressedListener {

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
                sendEvent(MapEvents.OnPoiDetailsHide)
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

    private val viewModel: MainViewModel by activityViewModels()

    private val binding: FragmentMapBinding
        get() = _binding!!
    private var _binding: FragmentMapBinding? = null

    private lateinit var map: GoogleMap
    private var currentLocationMarker: Marker? = null
    private var poiIdsMarkers = mutableMapOf<Poi, Marker>()
    private var lastClickedMarker: Marker? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null

    private var poiDetailsSheet: PoiDetailsBottomSheet? = null
    private var routeLine: Polyline? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMapBinding.bind(view)

        initMap()

        lifecycleScope.launchWhenResumed {
            viewModel.states.collect {
                Timber.i("State received => ${it::class.java.name}")

                if (it !is MapStates) {
                    return@collect
                }

                when (it) {

                    is MapStates.ApplyInitialMapSetup -> {
                        applyInitialMapSetup()
                    }

                    is MapStates.AddCurrentFakeLocationMarker -> {
                        addCurrentLocationMarker(it.location)
                    }

                    is MapStates.HideCurrentLocationFab -> {
                        binding.currentLocationFAB.hide()
                    }

                    is MapStates.HidePoiDetails -> {
                        poiDetailsSheet?.hide()
                    }

                    is MapStates.ShowFab -> {
                        if (it.isPreparingRoute) {
                            binding.currentLocationFAB.setImageResource(R.drawable.ic_gps_cursor)
                        } else {
                            binding.currentLocationFAB.setImageResource(R.drawable.ic_location)
                        }

                        binding.currentLocationFAB.show()
                    }

                    is MapStates.ResetHighlightedMarker -> {
                        resetMarker(lastClickedMarker)
                    }

                    is MapStates.ShowPoiDetails -> {
                        if (it.poi != null) {
                            poiDetailsSheet?.fillDetails(it.poi)
                        }

                        poiDetailsSheet?.show()
                    }

                    is MapStates.ShowDistanceToCurrentLocation -> {
                        val startingPosition = lastClickedMarker?.position ?: return@collect
                        val endingPosition = currentLocationMarker?.position ?: return@collect
                        val distance = startingPosition.distanceTo(endingPosition,
                            requireContext())

                        poiDetailsSheet?.setDistance(distance.beautifyDistance(requireContext()))
                        poiDetailsSheet?.setTime(distance.beautifyTime(requireContext()))
                    }

                    is MapStates.HighlightCurrentLocation -> {
                        highlightCurrentLocation(currentLocationMarker?.position)
                    }

                    is MapStates.HighlightSelectedMarker -> {
                        lastClickedMarker?.let {
                                marker ->  highlightMarker(marker, it.animateCamera)
                        }
                    }

                    is MapStates.SetRouteMarkers -> {
                        if (it.isFromCurrentLocation) {
                            originMarker = currentLocationMarker
                            destinationMarker = lastClickedMarker
                        } else {
                            if (destinationMarker != null) {
                                originMarker = lastClickedMarker
                            } else {
                                destinationMarker = lastClickedMarker
                            }
                        }
                    }

                    is MapStates.ShowRouteDetails -> {
                        with(binding.routeDetails) {
                            isStartingFromCurrentLocation(it.isFromCurrentLocation)

                            if (!it.isFromCurrentLocation) {
                                originMarker?.let {
                                    val originPoi = poiIdsMarkers.filterValues { marker ->
                                        marker == originMarker
                                    }.keys.first()

                                    setAddressFrom(originPoi.title)
                                }
                            }

                            setAddressTo(viewModel.destinationPoi.value!!.title)
                            show()
                        }
                    }

                    is MapStates.CloseRouteDetails -> {
                        binding.routeDetails.hide()
                    }

                    is MapStates.CameraOnRoute -> {
                        val boundsBuilder = LatLngBounds.Builder()
                        boundsBuilder.include(originMarker?.position
                            ?: return@collect)
                        boundsBuilder.include(destinationMarker?.position ?: return@collect)

                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(), ROUTE_CAMERA_PADDING.toPx()))
                    }

                    is MapStates.DrawRouteOnMap -> {
                        drawRoute(
                            originMarker?.position ?: return@collect,
                            destinationMarker?.position ?: return@collect
                        )
                    }

                    is MapStates.ResetRoute -> {
                        routeLine?.remove()
                        originMarker = null
                        destinationMarker = null
                    }

                    is MapStates.FinishApp -> {
                        requireActivity().finish()
                    }

                    is MapStates.OpenPoiList -> {
                        findNavController().navigate(R.id.to_poisBottomSheetDialogFragment)
                    }

                    is MapStates.HighlightPoiMarker -> {
                        resetMarker(lastClickedMarker)
                        lastClickedMarker = poiIdsMarkers.filterKeys { currentPoi ->
                            currentPoi.id == it.poiId
                        }.values.first()
                        sendEvent(MapEvents.OnMarkerClicked(it.poiId))
                    }

                    else -> {
                        if (BuildConfig.DEBUG) {
                            throw IllegalStateException(
                                "Unknown MapStates instance: ${it::class.java.simpleName}")
                        }
                    }
                }
            }
        }

        viewModel.locationStatusLiveData.observe(viewLifecycleOwner) { gpsStatus ->
            changeCurrentLocationMarkerIcon(gpsStatus.isLocationEnabled)
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
                        map.setLatLngBoundsForCameraTarget(markerBounds)
                    }
                })
            }
        }

        viewModel.selectedPoi.observe(viewLifecycleOwner) { poi ->
            if (poi != null) {
                poiDetailsSheet?.fillDetails(poi)
            }
        }

        viewModel.originPoi.observe(viewLifecycleOwner) { poi ->
            if (poi != null) {
                binding.routeDetails.setAddressFrom(poi.title)
            }
        }

        viewModel.destinationPoi.observe(viewLifecycleOwner) { poi ->
            if (poi != null) {
                binding.routeDetails.setAddressTo(poi.title)
            }
        }

        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        poiDetailsSheet = null
        currentLocationMarker = null
        lastClickedMarker = null
        originMarker = null
        destinationMarker = null
    }

    override fun onBackPressed(): Boolean {
        sendEvent(MapEvents.OnBackPressed)

        return false
    }

    private fun applyInitialMapSetup() {
        map.uiSettings.isMapToolbarEnabled = false
        map.setMinZoomPreference(MAP_MIN_ZOOM)
    }

    private fun addCurrentLocationMarker(location: LatLng) {
        val currentLocationMarkerOptions = MarkerOptions()
            .title(CURRENT_FAKE_LOCATION_ID)
            .position(location)

        currentLocationMarker = map.addMarker(currentLocationMarkerOptions)
        changeCurrentLocationMarkerIcon()

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, MapZoomLevel.CONTINENT.level))
    }

    private fun changeCurrentLocationMarkerIcon(
        isLocationEnabled: Boolean? = viewModel.locationStatusLiveData.value?.isLocationEnabled) {

        val iconDrawableID = if (isLocationEnabled == true) {
            R.drawable.ic_bluedot
        } else {
            R.drawable.ic_greydot
        }

        currentLocationMarker?.setIcon(BitmapDescriptorFactory.fromResource(iconDrawableID))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
        sendEvent(MapEvents.OnMapReady)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker == null) {
            return true
        }

        if (lastClickedMarker != marker) {
            resetMarker(lastClickedMarker)
        }

        if (!marker.isMyCurrentLocation()) {
            lastClickedMarker = marker

            sendEvent(MapEvents.OnMarkerClicked(marker.title))
        } else {
            sendEvent(MapEvents.OnCurrentLocationMarkerClicked)
        }

        return true
    }

    override fun onDirectionsFabClicked() {
        sendEvent(MapEvents.OnDirectionsFabClicked)
    }

    override fun onCloseButtonClicked() {
        sendEvent(MapEvents.OnRouteDetailsClosed)
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

            with(routeDetails) {
                onVisibilityChangedListener = onRouteDetailsVisibilityChangedListener
                onClickListener = this@MapFragment
            }

            currentLocationFAB.setOnClickListener {
                sendEvent(MapEvents.OnCurrentLocationFabClicked)
            }

            searchButton.setOnClickListener {
                sendEvent(MapEvents.OnSearchButtonClicked)
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
            val newMarkerOptions = MarkerOptions()
                .title(markerTitle)
                .position(latLng)

            val newMarker = map.addMarker(newMarkerOptions)
            poiIdsMarkers[poi] = newMarker

            boundsBuilder.include(latLng)
        }

        return boundsBuilder.build()
    }

    private fun highlightCurrentLocation(location: LatLng? = currentLocationMarker?.position) {
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location,
                MapZoomLevel.STREETS.level))
        }
    }

    private fun highlightMarker(marker: Marker, animateCamera: Boolean = true) {
        if (animateCamera) {
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
    }

    private fun resetMarker(marker: Marker?) {
        marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
    }

    private fun drawRoute(start: LatLng, end: LatLng) {
        routeLine?.remove()
        routeLine = map.addPolyline(PolylineOptions()
            .add(start)
            .add(end)
            .color(Color.MAGENTA)
            .width(ROUTE_LINE_WIDTH.toPx().toFloat()))
    }
}
