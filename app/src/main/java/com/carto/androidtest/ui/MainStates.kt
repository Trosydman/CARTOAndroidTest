package com.carto.androidtest.ui

import com.google.android.gms.maps.model.LatLng

sealed class MainStates {
    sealed class MapStates: MainStates() {
        object ApplyInitialMapSetup: MapStates()
        data class AddCurrentFakeLocationMarker(val location: LatLng): MapStates()
        object HidePoiDetails : MainStates()
        data class ShowFab(val isPreparingRoute: Boolean = false) : MainStates()
        object HideCurrentLocationFab : MainStates()
        object ResetHighlightedMarker : MainStates()
        object ShowPoiDetails : MainStates()
        object ShowDistanceToCurrentLocation : MainStates()
        object HighlightCurrentLocation : MainStates()
        data class HighlightSelectedMarker(val animateCamera: Boolean = true) : MainStates()
        data class ShowRouteDetails(val isFromCurrentLocation: Boolean = true) : MainStates()
        data class DrawRouteOnMap(val isFromCurrentLocation: Boolean = true) : MainStates()
        data class CameraOnRoute(val isFromCurrentLocation: Boolean = true) : MainStates()
        object ResetRoute : MainStates()
    }
}
