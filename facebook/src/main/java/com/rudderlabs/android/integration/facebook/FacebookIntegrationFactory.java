package com.rudderlabs.android.integration.facebook;

import static com.rudderlabs.android.integration.facebook.Utils.getCurrency;
import static com.rudderlabs.android.integration.facebook.Utils.getValueToSum;

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderTraits;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FacebookIntegrationFactory extends RudderIntegration<AppEventsLogger> {
    public static final String APP_ID = "appID";
    public static final String DPO_COUNTRY = "dpoCountry";
    private static final String FACEBOOK_KEY = "Facebook App Events";
    public static final String PRODUCTS_SEARCHED = "Products Searched";
    public static final String PRODUCT_VIEWED = "Product Viewed";
    public static final String PRODUCT_ADDED = "Product Added";
    public static final String PRODUCT_ADDED_TO_WISHLIST = "Product Added to Wishlist";
    public static final String PAYMENT_INFO_ENTERED = "Payment Info Entered";
    public static final String CHECKOUT_STARTED = "Checkout Started";
    public static final String COMPLETE_REGISTRATION = "Complete Registration";
    public static final String ACHIEVE_LEVEL = "Achieve Level";
    public static final String COMPLETE_TUTORIAL = "Complete Tutorial";
    public static final String UNLOCK_ACHIEVEMENT = "Unlock Achievement";
    public static final String SUBSCRIBE = "Subscribe";
    public static final String START_TRIAL = "Start Trial";
    public static final String PROMOTION_CLICKED = "Promotion Clicked";
    public static final String PROMOTION_VIEWED = "Promotion Viewed";
    public static final String SPEND_CREDITS = "Spend Credits";
    public static final String PRODUCT_REVIEWED = "Product Reviewed";
    public static final String ORDER_COMPLETED = "Order Completed";
    public static final String DPO_STATE = "dpoState";
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
            return new FacebookIntegrationFactory(settings, rudderConfig);
        }

        @Override
        public String key() {
            return FACEBOOK_KEY;
        }
    };

    @VisibleForTesting
    FacebookIntegrationFactory() {
    }

    private FacebookIntegrationFactory(Object config, RudderConfig rudderConfig) {
        if (RudderClient.getApplication() != null) {
            // deserialize the destination config json into FacebookDestinationConfig object
            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonDeserializer<FacebookDestinationConfig> deserializer =
                    (json, typeOfT, context) -> {
                        JsonObject jsonObject = json.getAsJsonObject();
                        return new FacebookDestinationConfig(
                                Utils.getStringFromJsonObject(jsonObject, APP_ID),
                                Utils.getBooleanFromJsonObject(jsonObject),
                                Utils.getStringFromJsonObject(jsonObject, DPO_COUNTRY),
                                Utils.getStringFromJsonObject(jsonObject, DPO_STATE)
                        );
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
            AppEventsLogger.activateApp(RudderClient.getApplication(), destinationConfig.appID);
            setup(AppEventsLogger.newLogger(RudderClient.getApplication()));
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
                    if (eventName == null || eventName.length() == 0) {
                        RudderLogger.logDebug("Facebook: Dropping track event as eventName is null.");
                        return;
                    }
                    track(element, getFacebookEvent(eventName));
                    break;
                case MessageType.SCREEN:
                    // FB Event Names must be <= 40 characters
                    // 'Viewed' and 'Screen' with spaces take up 14
                    String screenName = Utils.truncate(element.getEventName(), 26);
                    if (screenName == null || screenName.length() == 0) {
                        RudderLogger.logDebug("Facebook: Dropping screen event as eventName is null.");
                        return;
                    }
                    if (element.getProperties() != null && element.getProperties().size() != 0) {
                        Bundle screenProperties = new Bundle();
                        handleCustomScreenProperties(element.getProperties(), screenProperties);
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
        Map<String, Object> properties = element.getProperties();
        Bundle params = new Bundle();
        handleCustomTrackProperties(properties, params);

        switch (eventName) {
            // Standard events, refer Facebook docs: https://developers.facebook.com/docs/app-events/reference#standard-events-2 for more info
            case AppEventsConstants.EVENT_NAME_ADDED_TO_CART:
            case AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST:
            case AppEventsConstants.EVENT_NAME_VIEWED_CONTENT:
                handleStandardTrackProperties(properties, params, eventName);
                Double price = getValueToSum(element.getProperties(), RSKeys.Ecommerce.PRICE);
                if (price != null) {
                    instance.logEvent(eventName, price, params);
                }
                break;
            case AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT:
            case AppEventsConstants.EVENT_NAME_SPENT_CREDITS:
                handleStandardTrackProperties(properties, params, eventName);
                Double value = getValueToSum(element.getProperties(), RSKeys.Ecommerce.VALUE);
                if (value != null) {
                    instance.logEvent(eventName, value, params);
                }
                break;
            case ORDER_COMPLETED:
                handleStandardTrackProperties(element.getProperties(), params, eventName);
                Double revenue = Utils.getRevenue(properties);
                String currency = getCurrency(properties);
                if (revenue != null) {
                    instance.logPurchase(BigDecimal.valueOf(revenue), Currency.getInstance(currency), params);
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
            case AppEventsConstants.EVENT_NAME_RATED:
                handleStandardTrackProperties(properties, params, eventName);
                instance.logEvent(eventName, params);
                break;
            default:
                instance.logEvent(eventName, params);
                break;
        }
    }

    public String getFacebookEvent(String eventName) {
        switch (eventName) {
            case PRODUCTS_SEARCHED:
                return AppEventsConstants.EVENT_NAME_SEARCHED;
            case PRODUCT_VIEWED:
                return AppEventsConstants.EVENT_NAME_VIEWED_CONTENT;
            case PRODUCT_ADDED:
                return AppEventsConstants.EVENT_NAME_ADDED_TO_CART;
            case PRODUCT_ADDED_TO_WISHLIST:
                return AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST;
            case PAYMENT_INFO_ENTERED:
                return AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO;
            case CHECKOUT_STARTED:
                return AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT;
            case COMPLETE_REGISTRATION:
                return AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION;
            case ACHIEVE_LEVEL:
                return AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL;
            case COMPLETE_TUTORIAL:
                return AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL;
            case UNLOCK_ACHIEVEMENT:
                return AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT;
            case SUBSCRIBE:
                return AppEventsConstants.EVENT_NAME_SUBSCRIBE;
            case START_TRIAL:
                return AppEventsConstants.EVENT_NAME_START_TRIAL;
            case PROMOTION_CLICKED:
                return AppEventsConstants.EVENT_NAME_AD_CLICK;
            case PROMOTION_VIEWED:
                return AppEventsConstants.EVENT_NAME_AD_IMPRESSION;
            case SPEND_CREDITS:
                return AppEventsConstants.EVENT_NAME_SPENT_CREDITS;
            case PRODUCT_REVIEWED:
                return AppEventsConstants.EVENT_NAME_RATED;
            default:
                return eventName;
        }
    }

    public void handleStandardTrackProperties(Map<String, Object> properties, Bundle params, String eventName) {
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

        if (!eventName.equals(ORDER_COMPLETED)) {
            params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, Utils.getCurrency(properties));
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

    public void handleCustomTrackProperties(Map<String, Object> properties, Bundle params) {
        handleCustomProperties(properties, params, false);
    }

    public void handleCustomScreenProperties(Map<String, Object> properties, Bundle params) {
        handleCustomProperties(properties, params, true);
    }

    public void handleCustomProperties(Map<String, Object> properties, Bundle params, boolean isScreenEvent) {
        if (properties == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!isScreenEvent && TRACK_RESERVED_KEYWORDS.contains(key)) {
                continue;
            }
            if (value instanceof String) {
                params.putString(key, (String) value);
            } else if (value instanceof Integer) {
                params.putInt(key, (Integer) value);
            } else if (value instanceof Short) {
                params.putShort(key, (Short) value);
            } else if (value instanceof Float) {
                params.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                params.putDouble(key, (Double) value);
            } else {
                params.putString(key, new Gson().toJson(value));
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
