package com.roam.androidnativeselftracking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.roam.sdk.Roam
import com.roam.sdk.builder.RoamTrackingMode
import com.roam.sdk.callback.RoamCallback
import com.roam.sdk.callback.SubscribeCallback
import com.roam.sdk.callback.TrackingCallback
import com.roam.sdk.models.RoamError
import com.roam.sdk.models.RoamUser
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkPermissionsQ()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Roam.REQUEST_CODE_LOCATION_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startTracking()
                    createRoamUser()
                } else {
                    showToast("Location permission required")
                }
            }
            Roam.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startTracking()
                    createRoamUser()
                } else {
                    showToast("Background Location permission required")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Roam.REQUEST_CODE_LOCATION_ENABLED) {
            //startTracking()
            createRoamUser()
        }
    }

    private fun checkPermissionsQ() {
        if (!Roam.checkLocationPermission()) {
            Roam.requestLocationPermission(this)
        } else if (!Roam.checkBackgroundLocationPermission()) {
            Roam.requestBackgroundLocationPermission(this)
        } else if (!Roam.checkLocationServices()) {
            Roam.requestLocationServices(this)
        } else {
            //startTracking()
            createRoamUser();
        }
    }

    private fun createRoamUser() {
        val metadata = JSONObject()
        metadata.put("key", "value")
        Roam.createUser("SET-USER-DESCRIPTION-HERE", metadata, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Log.e("TAG", "onSuccess: User created successfully "+roamUser.userId )
                // Access Roam user data below
                // roamUser.getUserId();
                // roamUser.getDescription();
                // roamUser.getEventListenerStatus();
                // roamUser.getLocationListenerStatus();
                // roamUser.getLocationEvents();
                // roamUser.getGeofenceEvents();
                // roamUser.getMovingGeofenceEvents();
                // roamUser.getTripsEvents();

                enableToggleEvents()



            }

            override fun onFailure(roamError: RoamError) {
                // Access the error code & message below
                // roamError.getCode()
                // roamError.getMessage()
            }
        })
    }

    private fun enableToggleEvents() {
        Roam.toggleEvents(true, true, true, true, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Log.e("TAG", "onSuccess: events enabled" )
                enableListener()
            }

            override fun onFailure(error: RoamError) {

            }
        })
    }

    private fun enableListener() {
        Roam.toggleListener(true, true, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Log.e("TAG", "onSuccess: Listener enabled" )
                subscribeListener(roamUser)
            }

            override fun onFailure(error: RoamError) {

            }
        })
    }

    private fun subscribeListener(roamUser: RoamUser) {
        Roam.subscribe(
            Roam.Subscribe.LOCATION,
           roamUser.userId,
            object : SubscribeCallback {
                override fun onSuccess(message: String, userId: String) {
                    Log.e("TAG", "onSuccess: $userId $message")
                    startTracking()
                }

                override fun onError(error: RoamError) {
                    Log.e("TAG", "onFailure: " + Gson().toJson(error))
                }
            })

    }

    private fun startTracking() {
        startService(Intent(this, LocationService::class.java))


        val roamTrackingMode = RoamTrackingMode.Builder(5)
            .setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH)
            .build()

            //Roam.startTracking(RoamTrackingMode.ACTIVE, object: TrackingCallback {
            Roam.startTracking(roamTrackingMode, object: TrackingCallback {
                override fun onSuccess(p0: String?) {
                    Log.e("TAG", "onSuccess: "+p0)
                }

                override fun onError(p0: RoamError?) {
                    Log.e("TAG", "onSuccess: "+ (p0?.message))

                }
            })
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}