package com.carto.androidtest.domain.model

data class Poi(
    val id: String,
    val title: String,
    val description: String,
    val direction: String,
    val region: String,
    val image: String,
    val longitude: Double,
    val latitude: Double
)
