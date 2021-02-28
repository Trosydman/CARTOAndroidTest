package com.carto.androidtest.data_source.remote.network.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PoisResponse<T>(
    @Json(name = "rows") val pois: List<T>,
    @Json(name = "total_rows") val poisAmount: Long
)
