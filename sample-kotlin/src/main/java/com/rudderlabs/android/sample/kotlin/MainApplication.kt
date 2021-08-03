package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.facebook.FacebookIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger
import com.facebook.FacebookSdk

import com.facebook.LoggingBehavior


class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1wD4YINtPQW9lwdAXsc38ZlRW0k"
        private const val DATA_PLANE_URL = "https://215e6479a713.ngrok.io"
        private const val CONTROL_PLANE_URL = "https://curly-rattlesnake-57.loca.lt/"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
                this,
                WRITE_KEY,
                RudderConfig.Builder()
                        .withDataPlaneUrl(DATA_PLANE_URL)
                        .withControlPlaneUrl(CONTROL_PLANE_URL)
                        .withLogLevel(RudderLogger.RudderLogLevel.ERROR)
                        .withFactory(FacebookIntegrationFactory.FACTORY)
                        .withTrackLifecycleEvents(false)
                        .withRecordScreenViews(false)
                        .build()
        )
    }
}