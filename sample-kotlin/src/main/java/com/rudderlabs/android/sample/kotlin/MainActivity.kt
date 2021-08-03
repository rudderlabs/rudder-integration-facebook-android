package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.TrackPropertyBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed(this::sendEvents, 2000)
    }

    private fun sendEvents() {
        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("level_up_1")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("daily_rewards_claim_1")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )

                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            "Product Downloaded",
            RudderProperty()
                .putValue("revenue",347.67)
                .putValue("currency","INR")
                .putValue("product_id", "product_001")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("testing_1kaksjkajskajslajslajslajslajsajslajslajslasslajslajslasjlajslajslajslajslajslajsl")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.screen(
            "MainActivity",
            "HomeScreen",
            RudderProperty().putValue("foo", "bar"),
            null
        )
    }
}
