package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.facebook.FacebookIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1d4xNa8onF7fSuTXeVivKqOzfZr"
        private const val DATA_PLANE_URL = "https://hosted.rudderlabs.com"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .withFactory(FacebookIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(true)
                .withRecordScreenViews(true)
                .build()
        )
    }
}