package com.roam.androidnativeselftracking

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.roam.sdk.models.RoamError
import com.roam.sdk.models.RoamLocation
import com.roam.sdk.service.RoamReceiver

class LocationReceiver: RoamReceiver() {
    override fun onLocationUpdated(context: Context?, roamLocations: MutableList<RoamLocation>?) {
        super.onLocationUpdated(context, roamLocations)
        // receive own location updates here
        // do something with location data using location
        for (roamLocation in roamLocations!!){
            roamLocation.activity
            roamLocation.recordedAt
            roamLocation.timezoneOffset
            roamLocation.metadata
            roamLocation.batteryStatus
            roamLocation.networkStatus
            roamLocation.location.latitude
            roamLocation.location.longitude
            roamLocation.location.bearing
            roamLocation.location.altitude
            roamLocation.location.accuracy
            roamLocation.location.speed
            roamLocation.location.provider
            roamLocation.location.time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                roamLocation.location.verticalAccuracyMeters
            }
        }
        Log.e("TAG", "onLocationUpdated: "+roamLocations[0].location.latitude.toString()
                +" "+roamLocations[0].location.longitude )
        Toast.makeText(context, roamLocations[0].location.latitude.toString()
                +" "+roamLocations[0].location.longitude, Toast.LENGTH_SHORT).show()
    }

    override fun onError(context: Context?, roamError: RoamError?) {
        super.onError(context, roamError)
        // receive error message here
        roamError?.code
        roamError?.message
    }
}