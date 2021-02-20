package com.carto.androidtest.ui

import com.google.android.gms.maps.model.LatLng

sealed class MainStates {
    sealed class MapStates: MainStates() {
        object ApplyInitialMapSetup: MapStates()
        data class AddCurrentFakeLocationMarker(val location: LatLng): MapStates()
    }
}
