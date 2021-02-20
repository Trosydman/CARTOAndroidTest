package com.carto.androidtest.domain.model

import androidx.annotation.DrawableRes
import com.carto.androidtest.R
import java.util.*

data class Poi(
    val id: String,
    val title: String,
    val description: String,
    val direction: String,
    val region: String,
    val image: String,
    val longitude: Double,
    val latitude: Double
) {

    @DrawableRes
    val directionImage: Int =
        when (direction.toUpperCase(Locale.ROOT)) {
            "N" -> R.drawable.direction_n
            "N-E" -> R.drawable.direction_ne
            "E" -> R.drawable.direction_e
            "S-E" -> R.drawable.direction_se
            "S" -> R.drawable.direction_s
            "S-W" -> R.drawable.direction_sw
            "W" -> R.drawable.direction_w
            "N-W" -> R.drawable.direction_nw
            else -> R.drawable.direction_unknown
        }
}
