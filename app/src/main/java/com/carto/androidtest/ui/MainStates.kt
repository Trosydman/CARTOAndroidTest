package com.carto.androidtest.ui

import com.google.android.gms.maps.model.LatLng

sealed class MainStates {
    sealed class MapStates: MainStates() {
        object ApplyInitialMapSetup: MapStates()
        data class AddCurrentFakeLocationMarker(val location: LatLng): MapStates()
        object HidePoiDetails : MainStates()
        object ShowCurrentLocationFab : MainStates()
        object HideCurrentLocationFab : MainStates()
        object ResetHighlightedMarker : MainStates()
        object ShowDistanceToCurrentLocation : MainStates()
    }
}
