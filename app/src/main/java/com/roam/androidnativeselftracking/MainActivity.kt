package com.roam.androidnativeselftracking
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.roam.androidnativeselftracking.databinding.ActivityMainBinding
import com.roam.roam_batch_connector.RoamBatch
import com.roam.roam_batch_connector.builder.RoamBatchPublish
import com.roam.sdk.Roam
import com.roam.sdk.builder.RoamPublish
import com.roam.sdk.builder.RoamTrackingMode
import com.roam.sdk.callback.PublishCallback
import com.roam.sdk.callback.RoamCallback
import com.roam.sdk.callback.SubscribeCallback
import com.roam.sdk.callback.TrackingCallback
import com.roam.sdk.models.RoamError
import com.roam.sdk.models.RoamUser
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() , View.OnClickListener{
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        checkPermissionsQ()

        if(!TextUtils.isEmpty(Preferences.getUserId(baseContext))){
            binding?.etUserId?.setText(Preferences.getUserId(baseContext))
        }

        binding!!.btCreateUser.setOnClickListener(this)
        binding!!.btGetUser.setOnClickListener(this)
        binding!!.btStartTracking.setOnClickListener(this)
        binding!!.btStopTracking.setOnClickListener(this)




        if(Preferences.getMockLocation(this)) {
            binding!!.switchMockLocation.isChecked = true
        }
        else {
            binding!!.switchMockLocation.isChecked = false
        }
        binding!!.switchMockLocation.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            showToast("Mock location: " +isChecked )
            Roam.allowMockLocation(isChecked)
            Preferences.setMockLocation(this,isChecked)
        }


        if(Preferences.getPublish(this)) {
            binding!!.switchPublishSave.isChecked = true
        }
        else {
            binding!!.switchPublishSave.isChecked = false
        }
        binding!!.switchPublishSave.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
           if(isChecked) {
               publishSave()
           }else{
               stopPublishing()
           }

            Preferences.setPublish(this,isChecked)
        }
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
                    checkPermissionsQ()
                } else {
                    showToast("Location permission required")
                }
            }
            Roam.REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionsQ()
                } else {
                    showToast("Background Location permission required")
                }
            }
            Roam.REQUEST_CODE_POST_NOTIFICATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Post notification permission given")
                    checkPermissionsQ()
                } else {
                    showToast("Post notification permission required")
                }
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Roam.REQUEST_CODE_LOCATION_ENABLED) {
            checkPermissionsQ()
        }
    }

    private fun checkPermissionsQ() {
        if (!Roam.checkLocationPermission()) {
            Roam.requestLocationPermission(this)
        } else if (!Roam.checkBackgroundLocationPermission()) {
            Roam.requestBackgroundLocationPermission(this)
        } else if (!Roam.checkLocationServices()) {
            Roam.requestLocationServices(this)
        } else if (!Roam.checkPostNotificationPermission()) {
            Roam.requestPostNotificationPermission(this)
        }
    }

    private fun createRoamUser() {
        val metadata = JSONObject()
        metadata.put("key", "value")
        Roam.createUser("User 1", metadata, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Log.e("TAG", "onSuccess: User created successfully "+roamUser.userId )
                showToast("User created successfully:: "+roamUser.userId )
                binding?.etUserId?.setText(roamUser.userId.toString())
                Preferences.setUserId(baseContext, roamUser.userId.toString())
                enableToggleEvents()
            }

            override fun onFailure(roamError: RoamError) {
                 roamError.getMessage()
                showToast("User not created "+roamError.getMessage() )
            }
        })
    }

    private fun getRoamUser() {
        if(!TextUtils.isEmpty(binding?.etUserId?.text.toString())) {
            Roam.getUser(binding?.etUserId?.text.toString(), object : RoamCallback {
                override fun onSuccess(user: RoamUser?) {
                    enableToggleEvents()
                    binding?.etUserId?.setText(user?.userId.toString())
                    Preferences.setUserId(baseContext, user?.userId.toString())
                    Log.e("TAG", "onSuccess:: " + user?.userId)
                    showToast("User get successfully "+user?.userId )
                }

                override fun onFailure(error: RoamError?) {
                    Log.e("TAG", "onFailure: " + error?.message)
                    showToast("User not get "+error?.getMessage() )
                }
            })
        }else{
            Toast.makeText(this, "Enter userId", Toast.LENGTH_SHORT).show()
        }


    }

    private fun enableToggleEvents() {
        Roam.toggleEvents(true, true, true, true, object : RoamCallback {
            override fun onSuccess(roamUser: RoamUser) {
                Log.e("TAG", "onSuccess: events enabled" )
                enableListener()
            }

            override fun onFailure(error: RoamError) {
                Log.e("TAG", "onFailure: "+error.message )
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
                Log.e("TAG", "onFailure: "+error.message )
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
                    //startTracking()
                    //publishSave()
                }

                override fun onError(error: RoamError) {
                    Log.e("TAG", "onFailure: " + Gson().toJson(error))
                }
            })

    }

    //To publish data in Roam server
    private fun publishSave() {
        try {
            val d = JSONObject()
            d.put("key","value")
            val roamPublish1 = RoamPublish.Builder()
                .metadata(d)
                .appId()
                .userId()
                //.geofenceEvents()
                //.locationEvents()
               //.nearbyEvents()
                //.tripsEvents()
                //.locationListener()
                //.eventListener()
                .altitude()
                .course()
                .speed()
                .horizontalAccuracy()
                .verticalAccuracy()
                .appContext()
                .allowMocked()
                .batteryRemaining()
                .batterySaver()
                .batteryStatus()
                .activity()
                .airplaneMode()
                .deviceManufacturer()
                .deviceModel()
                .trackingMode()
                .locationPermission()
                .networkStatus()
                .gpsStatus()
                .osVersion()
                .recordedAt()
                .tzOffset()
                .metadata(d)
                .androidSdkVersion()
                .androidReleaseVersion()
                .networkType()
                .networkState()
                .buildId()
                .buildType()
                .buildVersionIncremental()
                .kernelVersion()
                //.installedApplication()
                .aaid()
                .ipAddress()
                .deviceName()
                .wifiSsid()
                .localeCountry()
                .localeLanguage()
                .carrierName()
                .appInstallationDate()
                .appVersion()
                .publicIpAddress()
                .appName()
                .packageName()
                .systemName()
                .locationId()
                .build()

            Roam.publishAndSave(roamPublish1, object : PublishCallback {
                override fun onSuccess(message: String) {
                    Log.e("TAG", "onSuccess: $message")
                    showToast("onSuccess: $message")
                }

                override fun onError(error: RoamError) {
                    Log.e("TAG", "onFailure: " + Gson().toJson(error))
                    showToast("onError: ${error.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPublishing(){
        Roam.stopPublishing(object :  PublishCallback {
            override fun onSuccess(p0: String?) {
                showToast("onSuccess: $p0")
            }

            override fun onError(p0: RoamError?) {
                showToast("onError: ${p0?.message}")
            }

        })
    }

    private fun startTracking() {
        //startService(Intent(this, LocationService::class.java))

        // To enable service and foreground notification
        Roam.setForegroundNotification(
            true,
            "Self tracking app",
            "Click here to redirect the app",
            R.drawable.ic_launcher_background,
            "com.roam.androidnativeselftracking.MainActivity",
            "com.roam.androidnativeselftracking.LocationService"
        )


//        val roamTrackingMode = RoamTrackingMode.Builder(10)
//            .setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH)
//            .build()

            Roam.startTracking(RoamTrackingMode.BALANCED, object: TrackingCallback {
                override fun onSuccess(p0: String?) {
                    Log.e("TAG", "onSuccess: "+p0)
                    setBatchProcessing()
                    showToast("Tracking started")
                }

                override fun onError(p0: RoamError?) {
                    Log.e("TAG", "onFailure: "+ (p0?.message))
                    showToast( "Tracking not started "+p0?.message)
                }
            })
    }

    private fun stopTracking() {
        stopService(Intent(this, LocationService::class.java))
        Roam.stopTracking(object: TrackingCallback {
            override fun onSuccess(p0: String?) {
                Log.e("TAG", "onSuccess: "+p0)
                showToast("Tracking stoped")
            }

            override fun onError(p0: RoamError?) {
                Log.e("TAG", "onFailure: "+ (p0?.message))
                showToast("Tracking stoped "+ (p0?.message))
            }

        })
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setBatchProcessing() {
        val data = JSONObject()
        try {
            data.put("Key1", "PublishSaveMetaData1")
            data.put("Key2", "PublishSaveMetaData2")
            data.put("Key3", "PublishSaveMetaData3")
        } catch (e: JSONException) {
            e.printStackTrace()
        }


            val roamBatchPublish = RoamBatchPublish.Builder()
            .appId()
            .userId()
            .geofenceEvents()
            .locationEvents()
            .nearbyEvents()
            .geofenceEvents()
            .tripsEvents()
            .locationListener()
            .eventListener()
            .altitude()
            .course()
            .speed()
            .horizontalAccuracy()
            .verticalAccuracy()
            .appContext()
            .allowMocked()
            .batteryRemaining()
            .batterySaver()
            .batteryStatus()
            .activity()
            .airplaneMode()
            .deviceManufacturer()
            .deviceModel()
            .trackingMode()
            .locationPermission()
            .networkStatus()
            .gpsStatus()
            .osVersion()
            .recordedAt()
            .tzOffset() // .metadata(data)

            .androidSdkVersion()
            .androidReleaseVersion()
            .networkType()
            .networkState()
            .buildId()
            .buildType()
            .buildVersionIncremental()
            .kernelVersion()
            .installedApplication()
            .aaid()
            .ipAddress()
            .deviceName()
            .wifiSsid()
            .localeCountry()
            .localeLanguage()
            .carrierName()
            .appInstallationDate()
            .appVersion()
            .publicIpAddress()
            .appName()
            .packageName()
            .systemName()
            .locationId()

            .build()


            RoamBatch.setConfig(true,1, roamBatchPublish)

    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btGetUser) {
           getRoamUser()
        } else if (v?.id == R.id.btCreateUser) {
            createRoamUser()
        }else if (v?.id == R.id.btStartTracking) {
            startTracking()
        }else if (v?.id == R.id.btStopTracking) {
            stopTracking()
        }
    }


}