package com.carto.androidtest.ui

sealed class MainEvents {
    sealed class MapEvents: MainEvents() {
        object OnMapReady: MapEvents()
        data class OnMarkerClicked(val relatedPoiId: String) : MainEvents()
        object OnCurrentLocationMarkerClicked: MainEvents()
        object OnPoiDetailsHide : MainEvents()
        object OnCurrentLocationFabClicked : MainEvents()
        object OnDirectionsFabClicked : MainEvents()
        object OnRouteDetailsClosed : MainEvents()
        object OnBackPressed : MainEvents()
    }
}
