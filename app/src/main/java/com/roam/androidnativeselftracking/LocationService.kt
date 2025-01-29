package com.roam.androidnativeselftracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder


class LocationService:Service() {
    private var mLocationReceiver: LocationReceiver? = null
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    override fun onCreate() {
        super.onCreate()
        register()
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun register() {
        mLocationReceiver = LocationReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.roam.android.RECEIVED")
        intentFilter.addAction("com.roam.android.NETWORK")
        intentFilter.addAction("com.roam.android.CUSTOM.LOG")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mLocationReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(mLocationReceiver, intentFilter)
        }
    }

    private fun unRegister() {
        if (mLocationReceiver != null) {
            unregisterReceiver(mLocationReceiver)
        }
    }
}