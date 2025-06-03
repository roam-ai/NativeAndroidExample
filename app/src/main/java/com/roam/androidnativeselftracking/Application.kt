package com.roam.androidnativeselftracking

import android.app.Application
import com.roam.roam_batch_connector.RoamBatch
import com.roam.sdk.Roam
import com.roam.sdk.enums.LogLevel


class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        //Add Your project publish key
        Roam.initialize(this, "")
        //Roam batch processing
        RoamBatch.initialize(this)
        Roam.setLogLevel(LogLevel.VERBOSE)
    }
}