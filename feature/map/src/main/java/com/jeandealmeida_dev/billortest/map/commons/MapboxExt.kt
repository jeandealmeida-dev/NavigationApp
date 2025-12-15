package com.jeandealmeida_dev.billortest.map.commons

import android.content.Context

internal fun Context.retrieveAccessToken(): String? {
    return try {
        val resId = this.resources.getIdentifier(
            "mapbox_access_token",
            "string",
            this.packageName
        )
        this.getString(resId)
    } catch (e: Exception) {
        return null
    }
}
