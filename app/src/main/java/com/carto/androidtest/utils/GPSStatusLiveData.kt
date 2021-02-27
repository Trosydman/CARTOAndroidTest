package com.carto.androidtest.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData

data class GPSStatus(val isGPSEnabled: Boolean)

class GPSStatusLiveData(private val context: Context?): MutableLiveData<GPSStatus>() {
    override fun onActive() {
        super.onActive()
        context?.registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    override fun onInactive() {
        super.onInactive()
        context?.unregisterReceiver(gpsReceiver)
    }

    private val gpsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val locationManager =
                (context?.getSystemService(Context.LOCATION_SERVICE)) as LocationManager
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            postValue(GPSStatus(isGPSEnabled))
        }
    }
}
