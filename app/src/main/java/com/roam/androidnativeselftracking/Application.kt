package com.roam.androidnativeselftracking

import android.app.Application
import com.roam.sdk.Roam

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        //Add Your project publish key
        //Roam.initialize(this, "PUBLISH_KEY")
        Roam.initialize(this, "48e2b50e7a19d667f6788c3063d08e7e24d58bd8fcf80fe0890484761acaff6f")
    }
}