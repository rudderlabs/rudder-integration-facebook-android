package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.identify)?.setOnClickListener { identify() }

        findViewById<Button>(R.id.productAdded)?.setOnClickListener { productAdded() }
        findViewById<Button>(R.id.productAddedToWishlist)?.setOnClickListener { productAddedToWishlist() }

        findViewById<Button>(R.id.productViewed)?.setOnClickListener { productViewed() }

        findViewById<Button>(R.id.checkoutStarted)?.setOnClickListener { checkoutStarted() }
        findViewById<Button>(R.id.spendCredits)?.setOnClickListener { spendCredits() }

        findViewById<Button>(R.id.orderCompleted)?.setOnClickListener { orderCompleted() }

        findViewById<Button>(R.id.productsSearched)?.setOnClickListener { productsSearched() }
        findViewById<Button>(R.id.paymentInfoEntered)?.setOnClickListener { paymentInfoEntered() }
        findViewById<Button>(R.id.completeRegistration)?.setOnClickListener { completeRegistration() }
        findViewById<Button>(R.id.achieveLevel)?.setOnClickListener { achieveLevel() }
        findViewById<Button>(R.id.completeTutorial)?.setOnClickListener { completeTutorial() }
        findViewById<Button>(R.id.unlockAchievement)?.setOnClickListener { unlockAchievement() }
        findViewById<Button>(R.id.subscribe)?.setOnClickListener { subscribe() }
        findViewById<Button>(R.id.startTrial)?.setOnClickListener { startTrial() }
        findViewById<Button>(R.id.promotionClicked)?.setOnClickListener { promotionClicked() }
        findViewById<Button>(R.id.promotionViewed)?.setOnClickListener { promotionViewed() }
        findViewById<Button>(R.id.productReviewed)?.setOnClickListener { productReviewed() }

        findViewById<Button>(R.id.customTrack1)?.setOnClickListener { customTrack1() }
        findViewById<Button>(R.id.customTrack2)?.setOnClickListener { customTrack2() }

        findViewById<Button>(R.id.screen)?.setOnClickListener { screen() }
        findViewById<Button>(R.id.reset)?.setOnClickListener { reset() }
    }

    private fun identify() {
        MainApplication.rudderClient.identify("Android userId", getAddress(), null)
    }

    private fun productAdded() {
        MainApplication.rudderClient.track("Product Added", getAllStandardProperty())
    }

    private fun productAddedToWishlist() {
        MainApplication.rudderClient.track("Product Added to Wishlist", getAllStandardProperty())
    }

    private fun productViewed() {
        MainApplication.rudderClient.track("Product Viewed", getAllStandardProperty())
    }

    private fun checkoutStarted() {
        MainApplication.rudderClient.track("Checkout Started", getAllStandardProperty())
    }

    private fun spendCredits() {
        MainApplication.rudderClient.track("Spend Credits", getAllStandardProperty())
    }

    private fun orderCompleted() {
        MainApplication.rudderClient.track("Order Completed", getAllStandardProperty())
    }

    private fun productsSearched() {
        MainApplication.rudderClient.track("Products Searched", getAllStandardProperty())
    }

    private fun paymentInfoEntered() {
        MainApplication.rudderClient.track("Payment Info Entered", getAllStandardProperty())
    }

    private fun completeRegistration() {
        MainApplication.rudderClient.track("Complete Registration", getAllStandardProperty())
    }

    private fun achieveLevel() {
        MainApplication.rudderClient.track("Achieve Level", getAllStandardProperty())
    }

    private fun completeTutorial() {
        MainApplication.rudderClient.track("Complete Tutorial", getAllStandardProperty())
    }

    private fun unlockAchievement() {
        MainApplication.rudderClient.track("Unlock Achievement", getAllStandardProperty())
    }

    private fun subscribe() {
        MainApplication.rudderClient.track("Subscribe", getAllStandardProperty())
    }

    private fun startTrial() {
        MainApplication.rudderClient.track("Start Trial", getAllStandardProperty())
    }

    private fun promotionClicked() {
        MainApplication.rudderClient.track("Promotion Clicked", getAllStandardProperty())
    }

    private fun promotionViewed() {
        MainApplication.rudderClient.track("Promotion Viewed", getAllStandardProperty())
    }

    private fun productReviewed() {
        MainApplication.rudderClient.track("Product Reviewed", getAllStandardProperty())
    }

    private fun customTrack1() {
        MainApplication.rudderClient.track("Random Event 1")
    }

    private fun customTrack2() {
        MainApplication.rudderClient.track("Random Event 2", getCustomProperty())
    }

    private fun screen() {
        MainApplication.rudderClient.screen("Main Activity 1")
        MainApplication.rudderClient.screen("Main Activity 2", getCustomProperty())
    }

    private fun reset() {
        MainApplication.rudderClient.reset()
    }

    private fun getAddress(): RudderTraits {
        val address: RudderTraits.Address = RudderTraits.Address()
            .putCity("Random city")
            .putState("Random state")
            .putPostalCode("Random postalCode")
            .putCountry("Random country")
        return RudderTraits()
            .putEmail("test@random.com")
            .putFirstName("John")
            .putLastName("Doe")
            .putPhone("1234567890")
            .putBirthday("1990-01-01")
            .putGender("M")
            .putAddress(address)
    }

    private fun getAllStandardProperty(): RudderProperty {
        return RudderProperty()
            .putValue("price", 1234)
            .putValue("value", 1235)
            .putValue("revenue", 1236)
            .putValue("currency", "INR")
            .putValue("product_id", "product_001")
            .putValue("rating", 4)
            .putValue("name", "AdTypeValue 2")
            .putValue("order_id", "order_001")
            .putValue("description", "Random description")
            .putValue("query", "Random query")
            .putValue("key-1", 2445656)
            .putValue("key-2", "value-2")
    }

    private fun getCustomProperty(): RudderProperty {
        return RudderProperty()
            .putValue("key-1", 2445656)
            .putValue("key-2", "value-2")
    }
}
