package com.carto.androidtest.ui

sealed class MainEvents {
    sealed class MapEvents: MainEvents() {
        object OnMapReady: MapEvents()
    }
}
