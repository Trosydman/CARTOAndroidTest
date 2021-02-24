package com.carto.androidtest.utils.extensions

import com.carto.androidtest.ui.CURRENT_FAKE_LOCATION_ID
import com.google.android.gms.maps.model.Marker

fun Marker.isMyCurrentLocation() = this.title == CURRENT_FAKE_LOCATION_ID
