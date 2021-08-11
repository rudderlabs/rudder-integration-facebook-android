package com.rudderlabs.android.integration.facebook;

import android.os.Bundle;

import com.facebook.FacebookSdk;

import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderTraits;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Currency;

public class FacebookIntegrationFactory extends RudderIntegration<AppEventsLogger> {
    private static final String FACEBOOK_KEY = "Facebook App Events";
    private AppEventsLogger instance;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new FacebookIntegrationFactory(settings, client, rudderConfig);
        }

        @Override
        public String key() {
            return FACEBOOK_KEY;
        }
    };

    private FacebookIntegrationFactory(Object config, RudderClient client, RudderConfig rudderConfig) {
        if (client.getApplication() != null) {
            // deserialize the destination config json into FacebookDestinationConfig object
            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonDeserializer<FacebookDestinationConfig> deserializer =
                    new JsonDeserializer<FacebookDestinationConfig>() {
                        @Override
                        public FacebookDestinationConfig deserialize(
                                JsonElement json,
                                Type typeOfT,
                                JsonDeserializationContext context
                        ) throws JsonParseException {
                            JsonObject jsonObject = json.getAsJsonObject();
                            return new FacebookDestinationConfig(
                                    Utils.getStringFromJsonObject(jsonObject, "appID"),
                                    Utils.getBooleanFromJsonObject(jsonObject, "limitedDataUse"),
                                    Utils.getStringFromJsonObject(jsonObject, "dpoCountry"),
                                    Utils.getStringFromJsonObject(jsonObject, "dpoState")
                            );
                        }
                    };
            gsonBuilder.registerTypeAdapter(FacebookDestinationConfig.class, deserializer);
            Gson customGson = gsonBuilder.create();
            FacebookDestinationConfig destinationConfig = customGson.fromJson(customGson.toJson(config), FacebookDestinationConfig.class);

            if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true);
                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
            }

            if (destinationConfig.limitedDataUse) {
                FacebookSdk.setDataProcessingOptions(new String[]{"LDU"}, destinationConfig.dpoCountry, destinationConfig.dpoState);
                RudderLogger.logDebug(String.format("FacebookSdk.setDataProcessingOptions(new String[] {\"LDU\"}, %s, %s);", destinationConfig.dpoCountry, destinationConfig.dpoState));
            } else {
                FacebookSdk.setDataProcessingOptions(new String[]{});
                RudderLogger.logDebug("FacebookSdk.setDataProcessingOptions(new String[] {});");
            }
            AppEventsLogger.activateApp(client.getApplication(), destinationConfig.appID);
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
                    // FB Event Names must be <= 40 characters
                    String eventName = Utils.truncate(element.getEventName(), 40);
                    if (eventName == null) {
                        return;
                    }
                    Bundle paramBundle = Utils.getBundleForMap(element.getProperties());
                    // If properties of an event exist
                    if (paramBundle != null) {
                        Double revenue = Utils.getRevenue(element.getProperties());
                        String currency = Utils.getCurrency(element.getProperties());
                        // If revenue is present in the properties of an event
                        if (revenue != null) {
                            instance.logPurchase(BigDecimal.valueOf(revenue), Currency.getInstance(currency));
                            paramBundle.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
                            instance.logEvent(eventName, revenue, paramBundle);
                            return;
                        }
                        instance.logEvent(eventName, paramBundle);
                        return;
                    }
                    // If properties of an event doesn't exist
                    instance.logEvent(eventName);
                    break;
                case MessageType.SCREEN:
                    // FB Event Names must be <= 40 characters
                    // 'Viewed' and 'Screen' with spaces take up 14
                    String screenName = Utils.truncate(element.getEventName(), 26);
                    if (screenName == null) {
                        return;
                    }
                    if (element.getProperties() != null && element.getProperties().size() != 0) {
                        Bundle screenProperties = Utils.getBundleForMap(element.getProperties());
                        instance.logEvent(String.format("Viewed %s Screen", screenName), screenProperties);
                        return;
                    }
                    instance.logEvent(String.format("Viewed %s Screen", screenName));
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
