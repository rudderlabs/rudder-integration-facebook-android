package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderMessageBuilder
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
                .setEventName("level_up")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("daily_rewards_claim")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )

        MainApplication.rudderClient.track(
            RudderMessageBuilder()
                .setEventName("revenue")
                .setProperty(
                    TrackPropertyBuilder()
                        .setCategory("test_category")
                        .build()
                )
                .setUserId("test_user_id")
        )
    }
}
