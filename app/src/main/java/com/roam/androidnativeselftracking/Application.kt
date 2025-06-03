package com.roam.androidnativeselftracking

import android.app.Application
import com.roam.roam_batch_connector.RoamBatch
import com.roam.sdk.Roam
import com.roam.sdk.callback.RoamLogoutCallback
import com.roam.sdk.enums.LogLevel


class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        //Add Your project publish key
        Roam.initialize(this, "a5592d8de41cdbcfd3bb2a76de577dc6173100b23f90c752cad2ca9bd26639c2")
        //Roam batch processing
        RoamBatch.initialize(this)
        Roam.setLogLevel(LogLevel.VERBOSE)
    }
}