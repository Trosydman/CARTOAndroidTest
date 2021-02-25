package com.carto.androidtest.ui

sealed class MainEvents {
    sealed class MapEvents: MainEvents() {
        object OnMapReady: MapEvents()
        data class OnMarkerClicked(val relatedPoiId: String) : MapEvents()
        object OnCurrentLocationMarkerClicked: MapEvents()
        object OnPoiDetailsHide : MapEvents()
        object OnCurrentLocationFabClicked : MapEvents()
        object OnDirectionsFabClicked : MapEvents()
        object OnRouteDetailsClosed : MapEvents()
        object OnBackPressed : MapEvents()
        object OnSearchButtonClicked : MapEvents()
    }

    sealed class PoisListEvents: MainEvents() {
        object OnCloseButtonClicked : PoisListEvents()
    }
}
