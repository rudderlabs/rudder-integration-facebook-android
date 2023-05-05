package com.rudderlabs.android.integration.facebook;


import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.Map;

public class Utils {

    public static final String LIMITED_DATA_USE = "limitedDataUse";

    static String truncate(String value, int length) {
        if (value != null && value.length() > length)
            value = value.substring(0, length);
        return value;
    }

    static Double getRevenue(@Nullable Map<String, Object> eventProperties) {
        if (eventProperties == null) return null;
        if (eventProperties.containsKey(RSKeys.Ecommerce.REVENUE)) {
            Object revenue = eventProperties.get(RSKeys.Ecommerce.REVENUE);
            if (revenue instanceof String) {
                return Double.parseDouble((String) revenue);
            } else if (revenue instanceof Integer) {
                return Double.valueOf((Integer) revenue);
            }
            return (Double) revenue;
        }
        return null;
    }

    static String getCurrency(@Nullable Map<String, Object> eventProperties) {
        if (eventProperties == null) return null;
        if (eventProperties.containsKey(RSKeys.Ecommerce.CURRENCY)) {
            return String.valueOf(eventProperties.get(RSKeys.Ecommerce.CURRENCY));
        }
        return "USD";
    }

    static String getStringFromJsonObject(JsonObject json, String key) {
        if (json.has(key)) {
            return json.get(key).getAsString();
        }
        return null;
    }

    static Boolean getBooleanFromJsonObject(JsonObject json) {
        if (json.has(LIMITED_DATA_USE)) {
            return json.get(LIMITED_DATA_USE).getAsBoolean();
        }
        return null;
    }


    static Double getValueToSum(Map<String, Object> properties, String propertyKey) {
        if (properties == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equalsIgnoreCase(propertyKey)) {
                return Double.parseDouble(String.valueOf(value));
            }
        }

        return null;
    }

}
