package com.carto.androidtest.utils.extensions

import android.content.Context
import com.carto.androidtest.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

private const val AVG_SPEED_KMH = 80.0f
private const val KM_IN_METERS = 1000f
private const val MINUTES_IN_HOUR = 60.0f

fun Float.beautifyDistance(context: Context): String {
    return if (this < KM_IN_METERS) {
        this.metersToString(context)
    } else {
        this.distanceToKm().kmToString(context)
    }
}

fun Float.beautifyTime(context: Context): String {
    val distanceInKm = this.distanceToKm()
    val kmsPerHour = distanceInKm / AVG_SPEED_KMH

    return if (distanceInKm < AVG_SPEED_KMH) {
        kmsPerHour.kmsInMinutes(context)
    } else {
        kmsPerHour.kmsInHours(context)
    }
}

fun Float.distanceToKm(): Float {
    return this / KM_IN_METERS
}

fun Float.metersToString(context: Context): String {
    return context.getString(R.string.distanceInMeters, formatDecimals().format(this))
}

fun Float.kmToString(context: Context): String {
    return context.getString(R.string.distanceInKms, formatDecimals().format(this))
}

fun Float.kmsInHours(context: Context): String {
    return context.getString(R.string.timeInHours, formatDecimals().format(this))
}

fun Float.kmsInMinutes(context: Context): String {
    return context.getString(R.string.timeInMinutes, formatDecimals().format(this * MINUTES_IN_HOUR))
}

private fun formatDecimals(): NumberFormat {
    val locale = Locale.getDefault()
    val result = NumberFormat.getInstance(locale)
    result.minimumFractionDigits = 2
    result.maximumFractionDigits = 2

    if (result is DecimalFormat) result.isDecimalSeparatorAlwaysShown = false

    return result
}
