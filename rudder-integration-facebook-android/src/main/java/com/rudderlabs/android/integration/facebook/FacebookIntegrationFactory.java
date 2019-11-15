package com.rudderlabs.android.integration.facebook;


import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderMessage;

import java.util.HashMap;
import java.util.Map;

public class FacebookIntegrationFactory extends RudderIntegration<AppEventsLogger> {
    static final String FACEBOOK_KEY = "FB";
    static final String FACEBOOK_DISPLAY_NAME = "Facebook";
    private static final String FACEBOOK_TYPE = "type";
    private AppEventsLogger instance;
    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client) {
            return new FacebookIntegrationFactory(settings, client);
        }

        @Override
        public String key() {
            return FACEBOOK_KEY;
        }
    };

    private Map<String, String> eventMap = new HashMap<>();

    private FacebookIntegrationFactory(Object config, RudderClient client) {
        String facebookApplicationId = (String) ((Map<String, Object>) config).get("apiKey");
        FacebookSdk.setApplicationId(facebookApplicationId);
        FacebookSdk.sdkInitialize(client.getApplication());
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.setAutoLogAppEventsEnabled(true);
        FacebookSdk.fullyInitialize();
        AppEventsLogger.activateApp(client.getApplication(), facebookApplicationId);

        this.instance = AppEventsLogger.newLogger(client.getApplication());
    }

    private void processRudderEvent(RudderMessage element) {
        Bundle paramBundle = Utils.getBundleForMap(element.getProperties());
        Bundle userBundle = Utils.getBundleForMap(element.getUserProperties());

        if (paramBundle != null && userBundle != null) paramBundle.putAll(userBundle);

        if (paramBundle == null) instance.logEvent(element.getEventName());
        else instance.logEvent(element.getEventName(), paramBundle);
    }

    @Override
    public void track(RudderMessage track) {
        processRudderEvent(track);
    }

    @Override
    public void identify(RudderMessage identify) {
        processRudderEvent(identify);
    }

    @Override
    public void group(RudderMessage group) {
        processRudderEvent(group);
    }

    @Override
    public void alias(RudderMessage alias) {
        processRudderEvent(alias);
    }

    @Override
    public void screen(RudderMessage screen) {
        processRudderEvent(screen);
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void dump(RudderMessage element) {
        processRudderEvent(element);
    }

    @Override
    public AppEventsLogger getUnderlyingInstance() {
        return instance;
    }
}
