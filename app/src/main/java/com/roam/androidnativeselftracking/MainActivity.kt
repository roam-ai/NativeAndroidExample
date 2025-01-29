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
import com.roam.sdk.Roam
import com.roam.sdk.builder.RoamTrackingMode
import com.roam.sdk.callback.TrackingCallback
import com.roam.sdk.models.RoamError


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
                    startTracking()
                } else {
                    showToast("Location permission required")
                }
            }
            Roam.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking()
                } else {
                    showToast("Background Location permission required")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Roam.REQUEST_CODE_LOCATION_ENABLED) {
            startTracking()
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
            startTracking()
        }
    }

    private fun startTracking() {
        startService(Intent(this, LocationService::class.java))
            Roam.startTracking(RoamTrackingMode.ACTIVE, object: TrackingCallback {
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