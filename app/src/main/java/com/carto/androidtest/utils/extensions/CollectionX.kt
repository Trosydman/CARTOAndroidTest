package com.carto.androidtest.utils.extensions

import com.carto.androidtest.utils.Permission

/**
 * Generates an indented string with all permissions provided in the Collection.
 */
fun Collection<Permission>.toIndentedString(): String {
    val stringBuilder = StringBuilder()

    for (permission in this) {
        stringBuilder.append("\n").append(permission.name)
    }

    return stringBuilder.toString()
}
