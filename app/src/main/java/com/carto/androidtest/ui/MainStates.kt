package com.carto.androidtest.ui

import com.google.android.gms.maps.model.LatLng

sealed class MainStates {
    sealed class MapStates: MainStates() {
        object ApplyInitialMapSetup: MapStates()
        data class AddCurrentFakeLocationMarker(val location: LatLng): MapStates()
        object HidePoiDetails : MapStates()
        data class ShowFab(val isPreparingRoute: Boolean = false) : MapStates()
        object HideCurrentLocationFab : MapStates()
        object ResetHighlightedMarker : MapStates()
        object ShowPoiDetails : MapStates()
        object ShowDistanceToCurrentLocation : MapStates()
        object HighlightCurrentLocation : MapStates()
        data class HighlightSelectedMarker(val animateCamera: Boolean = true) : MapStates()
        data class ShowRouteDetails(val isFromCurrentLocation: Boolean = true) : MapStates()
        object CloseRouteDetails : MapStates()
        data class DrawRouteOnMap(val isFromCurrentLocation: Boolean = true) : MapStates()
        data class CameraOnRoute(val isFromCurrentLocation: Boolean = true) : MapStates()
        object ResetRoute : MapStates()
        object FinishApp : MapStates()
        object OpenPoiList : MapStates()
    }

    sealed class PoisListStates: MainStates() {
        object PopBackStack: PoisListStates()
    }
}
