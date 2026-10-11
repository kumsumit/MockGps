package com.lilstiffy.mockgps.service

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.lilstiffy.mockgps.MainActivity
import com.lilstiffy.mockgps.MockGpsApp


object LocationHelper {
    private const val REQUEST_CODE = 69
    val DEFAULT_LOCATION = LatLng(40.712776, -74.005974)

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }

    fun requestPermissions(activity: ComponentActivity) {
        if (activity is MainActivity) {
            activity.requestLocationPermission()
        } else {
            activity.requestPermissions(
                getRequiredPermissions(),
                REQUEST_CODE
            )
        }
    }

    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppSettings(context)
        }
    }

    // Geocoding
    fun reverseGeocoding(latLng: LatLng, result: (Address?) -> Unit) {
        val geocoder = Geocoder(MockGpsApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { response ->
                val address = response.firstOrNull()
                result(address)
            }
        } else {
            val response = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = response?.firstOrNull()
            result(address)
        }
    }

    /**
     * @param searchterm Search term the user wants to do a coordinate look up for
     * @param result lambda containing [LatLng] object if a result was found from the Geocoding lookup.
     */
    fun geocoding(searchterm: String, result: (LatLng?) -> Unit) {
        val geocoder = Geocoder(MockGpsApp.shared.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(
                searchterm,
                1
            ) { response ->
                val address = response.firstOrNull()
                if (address == null) {
                    result(null)
                    return@getFromLocationName
                }

                result(LatLng(address.latitude, address.longitude))
            }
        } else {
            val response = geocoder.getFromLocationName(searchterm, 1)
            val address = response?.firstOrNull()

            if (address == null) {
                result(null)
                return
            }

            result(LatLng(address.latitude, address.longitude))
        }
    }
}
