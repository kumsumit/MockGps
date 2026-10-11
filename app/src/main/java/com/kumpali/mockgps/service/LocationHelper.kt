package com.kumpali.mockgps.service

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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.kumpali.mockgps.MainActivity
import com.kumpali.mockgps.MockGpsApp
import com.kumpali.mockgps.ui.models.LatLng


object LocationHelper {
    private const val REQUEST_CODE = 69
    private var placesClient: PlacesClient? = null
    val DEFAULT_LOCATION = LatLng(40.712776, -74.005974)

    fun initializePlaces(context: Context, apiKey: String) {
        if (apiKey.isBlank()) return
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context.applicationContext, apiKey)
        }
        placesClient = Places.createClient(context.applicationContext)
    }

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

    /** Searches Google Places and returns coordinates for the OpenStreetMap map. */
    fun geocoding(searchterm: String, result: (LatLng?) -> Unit) {
        val client = placesClient
        if (client == null) {
            result(null)
            return
        }

        val predictionRequest = FindAutocompletePredictionsRequest.builder()
            .setQuery(searchterm)
            .build()

        client.findAutocompletePredictions(predictionRequest)
            .addOnSuccessListener { response ->
                val prediction = response.autocompletePredictions.firstOrNull()
                if (prediction == null) {
                    result(null)
                    return@addOnSuccessListener
                }

                val placeRequest = FetchPlaceRequest.newInstance(
                    prediction.placeId,
                    listOf(Place.Field.LAT_LNG)
                )
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val location = placeResponse.place.latLng
                        result(location?.let { LatLng(it.latitude, it.longitude) })
                    }
                    .addOnFailureListener { result(null) }
            }
            .addOnFailureListener { result(null) }
    }
}
