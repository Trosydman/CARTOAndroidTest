package com.carto.androidtest.data_source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PoiDTO(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "direction") val direction: String,
    @Json(name = "region") val region: String,
    @Json(name = "image") val image: String,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "latitude") val latitude: Double
)
