package com.rudderlabs.android.integration.facebook;

import static com.rudderlabs.android.integration.facebook.Utils.getValueToSum;

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

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
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FacebookIntegrationFactory extends RudderIntegration<AppEventsLogger> {
    private static final String FACEBOOK_KEY = "Facebook App Events";
    private AppEventsLogger instance;

    private static final Set<String> TRACK_RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
            RSKeys.Ecommerce.PRODUCT_ID,
            RSKeys.Ecommerce.RATING,
            RSKeys.Ecommerce.PROMOTION_NAME,
            RSKeys.Ecommerce.ORDER_ID,
            RSKeys.Ecommerce.CURRENCY,
            RSKeys.Other.DESCRIPTION,
            RSKeys.Ecommerce.QUERY,
            RSKeys.Ecommerce.VALUE,
            RSKeys.Ecommerce.PRICE,
            RSKeys.Ecommerce.REVENUE)
    );

    public static final Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new FacebookIntegrationFactory(settings, client, rudderConfig);
        }

        @Override
        public String key() {
            return FACEBOOK_KEY;
        }
    };

    @VisibleForTesting
    FacebookIntegrationFactory() {
    }

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
            setup(AppEventsLogger.newLogger(client.getApplication()));
        } else {
            RudderLogger.logError("Facebook Factory is not initialized");
        }
    }

    @VisibleForTesting
    void setup(AppEventsLogger appEventsLogger) {
        this.instance = appEventsLogger;
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

                    track(element, eventName);
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
                        handleCustomScreen(element.getProperties(), screenProperties);
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

    private void track(RudderMessage element, String eventName) {
        Bundle params = Utils.getBundleForMap(element.getProperties());

        Map<String, Object> properties = element.getProperties();
        if (params == null || properties == null) {
            // If properties of an event doesn't exist
            instance.logEvent(eventName);
            return;
        }

        handleCustomTrack(element.getProperties(), params);
        Double revenue = Utils.getRevenue(properties);
        String currency = Utils.getCurrency(properties);
        // If revenue is present in the properties of an event
        if (revenue != null) {
            instance.logPurchase(BigDecimal.valueOf(revenue), Currency.getInstance(currency));
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
            instance.logEvent(eventName, revenue, params);
            return;
        }
        switch (eventName) {
            // Standard events, refer Facebook docs: https://developers.facebook.com/docs/app-events/reference#standard-events-2 for more info
            case AppEventsConstants.EVENT_NAME_ADDED_TO_CART:
            case AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST:
            case AppEventsConstants.EVENT_NAME_VIEWED_CONTENT:
                Double price = getValueToSum(element.getProperties(), RSKeys.Ecommerce.PRICE);
                if (price != null) {
                    instance.logEvent(eventName, params);
                    instance.logEvent(eventName, price, params);
                }
                break;

            case AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT:
            case AppEventsConstants.EVENT_NAME_SPENT_CREDITS:
                Double value = getValueToSum(element.getProperties(), RSKeys.Ecommerce.VALUE);
                if (value != null) {
                    instance.logEvent(eventName, params);
                    instance.logEvent(eventName, value, params);
                }
                break;

            case AppEventsConstants.EVENT_NAME_SEARCHED:
            case AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO:
            case AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION:
            case AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL:
            case AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL:
            case AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT:
            case AppEventsConstants.EVENT_NAME_SUBSCRIBE:
            case AppEventsConstants.EVENT_NAME_START_TRIAL:
            case AppEventsConstants.EVENT_NAME_AD_CLICK:
            case AppEventsConstants.EVENT_NAME_AD_IMPRESSION:
            default:
                instance.logEvent(eventName, params);
                break;
        }


    }

    public void handleStandard(Map<String, Object> properties, Bundle params) {
        if (properties == null) {
            return;
        }

        Object productId = properties.get(RSKeys.Ecommerce.PRODUCT_ID);
        if (productId != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, String.valueOf(productId));
        }

        Object rating = properties.get(RSKeys.Ecommerce.RATING);
        if (rating instanceof Integer) {
            params.putInt(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, (Integer) rating);
        }

        Object name = properties.get(RSKeys.Ecommerce.PROMOTION_NAME);
        if (name != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_AD_TYPE, String.valueOf(name));
        }

        Object orderId = properties.get(RSKeys.Ecommerce.ORDER_ID);
        if (orderId != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_ORDER_ID, String.valueOf(orderId));
        }

        Object currency = properties.get(RSKeys.Ecommerce.CURRENCY);
        if (currency != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, String.valueOf(currency));
        }


        Object description = properties.get(RSKeys.Other.DESCRIPTION);
        if (description != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, String.valueOf(description));
        }

        Object query = properties.get(RSKeys.Ecommerce.QUERY);
        if (query != null) {
            params.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, String.valueOf(query));
        }
    }


    public void handleCustomTrack(Map<String, Object> properties, Bundle params) {
        handleCustom(properties, params, false);
    }

    public void handleCustomScreen(Map<String, Object> properties, Bundle params) {
        handleCustom(properties, params, true);
    }

    public void handleCustom(Map<String, Object> properties, Bundle params, boolean isScreenEvent) {
        if (properties == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!isScreenEvent && TRACK_RESERVED_KEYWORDS.contains(key)) {
                continue;
            }

            params.putString(key, value.toString());
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
