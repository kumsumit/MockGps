package com.kumpali.mockgps.ui.models

data class LocationEntry(
    var latLng: LatLng,
    var addressLine: String? = null,
)
