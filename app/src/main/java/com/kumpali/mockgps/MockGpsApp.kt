package com.kumpali.mockgps

import android.app.Application
import com.kumpali.mockgps.service.VibratorService
import com.kumpali.mockgps.service.LocationHelper
import com.kumpali.mockgps.storage.StorageManager

class MockGpsApp : Application() {
    companion object {
        lateinit var shared: MockGpsApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        shared = this
        StorageManager.initialise(this)
        VibratorService.initialise(this)
        LocationHelper.initializePlaces(this, BuildConfig.PLACES_API_KEY)
    }

}
