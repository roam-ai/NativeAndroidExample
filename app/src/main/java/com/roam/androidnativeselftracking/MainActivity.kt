package com.roam.androidnativeselftracking
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.roam.androidnativeselftracking.databinding.ActivityMainBinding
import com.roam.roam_batch_connector.RoamBatch
import com.roam.roam_batch_connector.builder.RoamBatchPublish
import com.roam.sdk.Roam
import com.roam.sdk.builder.RoamTrackingMode
import com.roam.sdk.callback.TrackingCallback
import com.roam.sdk.models.RoamError


class MainActivity : AppCompatActivity(){
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        Roam.setForegroundNotification(
            true,
            "Self tracking app",
            "Click here to redirect the app",
            R.drawable.ic_launcher_foreground,
            "com.roam.androidnativeselftracking.MainActivity",
            "com.roam.androidnativeselftracking.LocationService"
        )
        Roam.allowMockLocation(true)
        if (Roam.isLocationTracking())
            {
            showToast("TRACKING...")
            }
        else{
            showToast("NOT TRACKING...")
            Roam.startTracking(RoamTrackingMode.BALANCED, object: TrackingCallback {
                override fun onSuccess(p0: String?) {
                    showToast("TRACKING STARTED...")
                }

                override fun onError(p0: RoamError?) {
                    showToast( "TRACKING NOT STARTED... \n"+p0?.message)
                }
            })
        }

        val roamBatchPublish = RoamBatchPublish.Builder().enableAll().build()
        RoamBatch.setConfig(true, roamBatchPublish)

        binding?.myButton?.setOnClickListener {
            showToast("BUTTON CLICKED - STOP TRACKING...")
            Roam.stopTracking(object : TrackingCallback {
                override fun onSuccess(message: String?) {
                    // Tracking stopped successfully
                    showToast("TRACKING STOPPED...")
                }

                override fun onError(error: RoamError?) {
                    // Handle stopping error
                    showToast("TRACKING NOT STOPPED...\n"+error?.message)
                }
            })
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}