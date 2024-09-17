# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Integrating Facebook with RudderStack's Android SDK

1. Add [Facebook](https://www.facebook.com) as a destination in the [Dashboard](https://app.rudderstack.com/) and define `applicationId`, and `clientKey`. If you turn on the Development Environment flag, make sure to put your development key in `clientKey`.

2. Setup the Hybrid Mode of integration:

  - Turning on the switch beside `Initialize Native SDK to send automated events` in the dashboard will initialize the Facebook native SDK in the application.
  - Turning on the switch beside `Use native SDK to send user generated events` in the dashboard will instruct your `data-plane` to skip the events for Facebook and the events will be sent from the Facebook SDK.

3. Add the dependency under ```dependencies``` section of your `app/build.gradle` file:
```
// Rudder core sdk and facebook extension
implementation 'com.rudderstack.android.sdk:core:x.y.z'
implementation 'com.rudderstack.android.integration:facebook:x.y.z'
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
Follow the steps from [RudderStack Android SDK](https://github.com/rudderlabs/rudder-sdk-android).

## Contact Us

If you come across any issues while configuring or using this integration, please feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
