package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.facebook.FacebookIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger


class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1wD4YINtPQW9lwdAXsc38ZlRW0k"
        private const val DATA_PLANE_URL = "https://285b83208a68.ngrok.io"
        private const val CONTROL_PLANE_URL = "https://sour-pig-84.loca.lt"
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
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withFactory(FacebookIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .build()
        )
    }
}