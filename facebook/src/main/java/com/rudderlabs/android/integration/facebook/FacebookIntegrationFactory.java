package com.rudderlabs.android.integration.facebook;

import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderTraits;

import java.util.Map;

public class FacebookIntegrationFactory extends RudderIntegration<AppEventsLogger> {
    private static final String FACEBOOK_KEY = "Facebook App Events";
    private AppEventsLogger instance;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new FacebookIntegrationFactory(settings, client);
        }

        @Override
        public String key() {
            return FACEBOOK_KEY;
        }
    };

    private FacebookIntegrationFactory(Object config, RudderClient client) {
        if (config != null && client != null && client.getApplication() != null) {
            String facebookApplicationId = (String) ((Map<String, Object>) config).get("apiKey");
            FacebookSdk.setApplicationId(facebookApplicationId);
            FacebookSdk.sdkInitialize(client.getApplication());
            FacebookSdk.setAutoInitEnabled(true);
            FacebookSdk.setAutoLogAppEventsEnabled(true);
            FacebookSdk.fullyInitialize();
            AppEventsLogger.activateApp(client.getApplication(), facebookApplicationId);

            this.instance = AppEventsLogger.newLogger(client.getApplication());
        } else {
            RudderLogger.logError("Facebook Factory is not initialized");
        }
    }

    private void processRudderEvent(RudderMessage element) {
        String messageType = element.getType();
        if (messageType != null && instance != null) {
            switch (messageType) {
                case MessageType.IDENTIFY:
                    AppEventsLogger.setUserID(element.getUserId());
                    RudderTraits.Address address = RudderTraits.Address.fromString(
                            RudderTraits.getAddress(element.getTraits())
                    );
                    AppEventsLogger.setUserData(
                            // email
                            RudderTraits.getEmail(element.getTraits()),
                            // firstName
                            RudderTraits.getFirstname(element.getTraits()),
                            // lastName
                            RudderTraits.getLastname(element.getTraits()),
                            // phone
                            RudderTraits.getPhone(element.getTraits()),
                            // dateOfBirth
                            RudderTraits.getBirthday(element.getTraits()),
                            // gender
                            RudderTraits.getGender(element.getTraits()),
                            // city
                            address != null ? address.getCity() : null,
                            // state
                            address != null ? address.getState() : null,
                            // zip
                            address != null ? address.getPostalCode() : null,
                            // country
                            address != null ? address.getCountry() : null
                    );
                    break;
                case MessageType.TRACK:
                    Bundle paramBundle = Utils.getBundleForMap(element.getProperties());
                    if (paramBundle == null) {
                        instance.logEvent(element.getEventName());
                    } else {
                        instance.logEvent(element.getEventName(), paramBundle);
                    }
                    break;
                case MessageType.SCREEN:

                    break;
                default:
                    RudderLogger.logWarn("MessageType is not supported");
                    break;
            }
        }
    }

    @Override
    public void reset() {
        AppEventsLogger.clearUserID();
        AppEventsLogger.clearUserData();
    }

    @Override
    public void dump(RudderMessage element) {
        if (element != null) {
            processRudderEvent(element);
        }
    }

    @Override
    public AppEventsLogger getUnderlyingInstance() {
        return instance;
    }
}
