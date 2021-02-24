package com.carto.androidtest.utils.extensions

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.distanceTo(endLatLng: LatLng, context: Context): Float {
    val result = floatArrayOf(0f)
    Location.distanceBetween(
        this.latitude, this.longitude,
        endLatLng.latitude, endLatLng.longitude,
        result
    )

    return result.first()
}
