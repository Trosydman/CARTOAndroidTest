package com.carto.androidtest.ui

import com.carto.androidtest.domain.model.Poi
import com.google.android.gms.maps.model.LatLng

sealed class MainStates {
    sealed class MapStates: MainStates() {
        object ApplyInitialMapSetup: MapStates()
        data class AddCurrentFakeLocationMarker(val location: LatLng): MapStates()
        object HidePoiDetails : MapStates()
        data class ShowFab(val isPreparingRoute: Boolean = false, val isNavigating: Boolean = false)
            : MapStates()
        object HideCurrentLocationFab : MapStates()
        object ResetHighlightedMarker : MapStates()
        data class ShowPoiDetails(val poi: Poi? = null) : MapStates()
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
        data class HighlightPoiMarker(val poiId: String) : MapStates()
        data class SetRouteMarkers(val isFromCurrentLocation: Boolean): MapStates()
        object StartNavigation : MapStates()
        object StopNavigation : MapStates()
    }

    sealed class PoisListStates: MainStates() {
        object PopBackStack: PoisListStates()
    }
}
