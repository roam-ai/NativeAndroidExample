package com.roam.androidnativeselftracking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    // --- ActivityResultLaunchers ---
    private lateinit var requestForegroundLocationLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestBackgroundLocationLauncher: ActivityResultLauncher<String>
    private lateinit var requestNotificationLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_splash) // Optional

        initializeLaunchers()
        checkAndRequestForegroundLocation()
    }

    private fun initializeLaunchers() {
        // 1. Foreground Location Launcher
        requestForegroundLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                if (fineLocationGranted || coarseLocationGranted) {
                    Log.d(TAG, "Foreground location permission GRANTED.")
                    // Proceed to Background Location
                    checkAndRequestBackgroundLocation()
                } else {
                    Log.w(TAG, "Foreground location permission DENIED.")
                    showPermissionDeniedDialog(
                        "Foreground location is essential for this app. Please grant this permission.",
                        ::checkAndRequestForegroundLocation // Retry this step
                    )
                }
            }

        // 2. Background Location Launcher
        requestBackgroundLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d(TAG, "Background location permission GRANTED.")
                    // Proceed to Notification Permission
                    checkAndRequestNotificationPermission()
                } else {
                    Log.w(TAG, "Background location permission DENIED.")
                    showPermissionDeniedDialog(
                        "Background location helps provide [your feature] even when the app is closed. You can grant it later in settings or proceed with limited functionality.",
                        ::checkAndRequestNotificationPermission // Proceed to next step even if denied
                    )
                }
            }

        // 3. Notification Launcher
        requestNotificationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d(TAG, "Notification permission GRANTED.")
                } else {
                    Log.w(TAG, "Notification permission DENIED.")
                    // Optionally show a message that notifications might not work
                }
                // All requested permissions have been processed, redirect to Main
                redirectToMain()
            }
    }

    // --- Permission Check and Request Logic ---

    // Step 1: Foreground Location
    private fun checkAndRequestForegroundLocation() {
        Log.d(TAG, "Step 1: Checking Foreground Location")
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Typically, FINE implies COARSE. Only add COARSE if FINE isn't requested and you want to offer it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            Manifest.permission.ACCESS_FINE_LOCATION !in permissionsToRequest) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Consider showing a rationale here if shouldShowRequestPermissionRationale is true
            // for ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION before launching.
            // For simplicity in this direct flow, we launch directly.
            Log.d(TAG, "Requesting foreground location: ${permissionsToRequest.joinToString()}")
            requestForegroundLocationLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d(TAG, "Foreground location already granted.")
            checkAndRequestBackgroundLocation() // Move to next step
        }
    }

    // Step 2: Background Location
    private fun checkAndRequestBackgroundLocation() {
        Log.d(TAG, "Step 2: Checking Background Location")
        if (!needsBackgroundLocation()) {
            Log.d(TAG, "Background location not needed by this app.")
            checkAndRequestNotificationPermission() // Skip to next step
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Background location already granted.")
                checkAndRequestNotificationPermission() // Move to next step
            } else {
                // ALWAYS show rationale for background location
                showBackgroundLocationRationale()
            }
        } else {
            // Before Android Q, background location is implicitly granted with foreground.
            Log.d(TAG, "Pre-Q device, background location implied by foreground.")
            checkAndRequestNotificationPermission() // Move to next step
        }
    }

    // Step 3: Notification Permission
    private fun checkAndRequestNotificationPermission() {
        Log.d(TAG, "Step 3: Checking Notification Permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted.")
                redirectToMain() // All steps done
            } else {
                // Consider showing a rationale here if shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                // before launching. Especially if notifications are critical.
                Log.d(TAG, "Requesting notification permission.")
                requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Notifications permission not required before Android 13
            Log.d(TAG, "Pre-Tiramisu device, notification permission not needed at runtime.")
            redirectToMain() // All steps done
        }
    }

    // --- Rationale Dialogs and Helpers ---

    private fun showBackgroundLocationRationale() {
        AlertDialog.Builder(this)
            .setTitle("Allow Location Access 'All The Time'?")
            .setMessage("To provide [your compelling feature, e.g., 'real-time safety alerts even when the app is in the background'], this app needs to access your location 'All The Time'.\n\nYour location data is handled securely and used only for this purpose.")
            .setPositiveButton("Grant 'Allow All The Time'") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            .setNegativeButton("Maybe Later") { dialog, _ ->
                dialog.dismiss()
                Log.w(TAG, "User declined background location rationale.")
                // Proceed to notification permission even if background is declined
                checkAndRequestNotificationPermission()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog(message: String, onRetry: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(message)
            .setNegativeButton("Exit App") { _, _ -> finishAffinity() } // Exit completely
            .setCancelable(false)

        if (onRetry != null) {
            builder.setPositiveButton("Try Again") { _, _ -> onRetry() }
            builder.setNeutralButton("Go to Settings") { _, _ ->
                openAppSettings()
                // Don't automatically retry from here, user needs to manually grant in settings
            }
        } else {
            // If no retry, just offer settings or exit
            builder.setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
        }
        builder.create().show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
        // Typically, you might finish SplashActivity here or wait for onResume
        // For this flow, we let the user handle settings and relaunch if needed.
    }

    private fun redirectToMain() {
        Log.i(TAG, "All permission steps completed. Redirecting to MainActivity.")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Determine if your app *actually* needs background location.
     * This is a placeholder. Implement your app's logic.
     */
    private fun needsBackgroundLocation(): Boolean {
        // return true // If your app ALWAYS needs background location
        return true // For testing this flow, assume true
    }
}