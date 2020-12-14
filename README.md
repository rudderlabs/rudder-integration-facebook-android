# What is RudderStack?

**Short answer:** 
RudderStack is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:** 
RudderStack is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

## Getting Started with Facebook Integration of Android SDK
1. Add [Facebook](https://www.facebook.com) as a destination in the [Dashboard](https://app.rudderstack.com/) and define `applicationId`, and `clientKey`. If you turn on the Development Environment flag, make sure to put your development key in `clientKey`.

2. Setup the Hybrid Mode of integration: 
  - Turning on the switch beside `Initialize Native SDK to send automated events` in the dashboard will initialize the Facebook native SDK in the application.
  - Turning on the switch beside `Use native SDK to send user generated events` in the dashboard will instruct your `data-plane` to skip the events for Facebook and the events will be sent from the Facebook SDK.

3. Add these lines to your ```app/build.gradle```
```
repositories {
    maven { url "https://dl.bintray.com/rudderstack/rudderstack" }
}
```
4. Add the dependency under ```dependencies```
```
// Rudder core sdk and facebook extension
implementation 'com.rudderstack.android.sdk:core:1.0.2'
implementation 'com.rudderstack.android.integration:facebook:0.1.1'

// facebook core sdk
implementation 'com.facebook.android:facebook-android-sdk:5.9.0'

// gson
implementation 'com.google.code.gson:gson:2.8.6'
```

## Initialize ```RudderClient```
```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    WRITE_KEY,
    RudderConfig.Builder()
        .withDataPlaneUrl(DATA_PLANE_URL)
        .withFactory(FacebookIntegrationFactory.FACTORY)
        .build()
)
```

## Send Events
Follow the steps from [Rudder Android SDK](https://github.com/rudderlabs/rudder-sdk-android)

## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
