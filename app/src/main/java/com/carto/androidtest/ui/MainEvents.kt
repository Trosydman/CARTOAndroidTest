package com.carto.androidtest.ui

import com.carto.androidtest.domain.model.Poi

sealed class MainEvents {
    sealed class MapEvents: MainEvents() {
        object OnMapReady: MapEvents()
        data class OnMarkerClicked(val relatedPoiId: String) : MapEvents()
        object OnCurrentLocationMarkerClicked: MapEvents()
        object OnPoiDetailsHide : MapEvents()
        object OnFabClicked : MapEvents()
        object OnDirectionsFabClicked : MapEvents()
        object OnRouteDetailsClosed : MapEvents()
        object OnBackPressed : MapEvents()
        object OnSearchButtonClicked : MapEvents()
        object OnHelpButtonClicked : MapEvents()
    }

    sealed class PoisListEvents: MainEvents() {
        object OnCloseButtonClicked : PoisListEvents()
        data class OnPoiItemClicked(val poi: Poi) : PoisListEvents()
        data class OnSearchFieldTextChanged(val query: String) : PoisListEvents()
    }
}
