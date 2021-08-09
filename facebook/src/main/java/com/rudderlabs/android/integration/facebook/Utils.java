package com.rudderlabs.android.integration.facebook;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;

public class Utils {
    static Bundle getBundleForMap(Map<String, Object> objectMap) {
        if (objectMap == null) return null;
        Bundle bundle = new Bundle();
        for (String key : objectMap.keySet()) {
            Object value = objectMap.get(key);
            if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof Short) {
                bundle.putShort(key, (Short) value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else {
                bundle.putString(key, new Gson().toJson(value));
            }
        }
        return bundle;
    }

    static String truncate(String value, int length) {
        if (value != null && value.length() > length)
            value = value.substring(0, length);
        return value;
    }

    static Double getRevenue(Map<String, Object> eventProperties) {
        if (eventProperties.containsKey("revenue")) {
            Object revenue = eventProperties.get("revenue");
            if (revenue instanceof String) {
                return Double.parseDouble((String) revenue);
            } else if (revenue instanceof Integer) {
                return Double.valueOf((Integer) revenue);
            }
            return (Double) revenue;
        }
        return null;
    }

    static String getCurrency(Map<String, Object> eventProperties) {
        if (eventProperties.containsKey("currency")) {
            return (String) eventProperties.get("currency");
        }
        return "USD";
    }

    static String getStringFromJsonObject(JsonObject json, String key)
    {
        if(json.has(key))
        {
            return json.get(key).getAsString();
        }
        return null;
    }

    static Boolean getBooleanFromJsonObject(JsonObject json, String key)
    {
       if(json.has(key))
       {
           return json.get(key).getAsBoolean();
       }
       return null;
    }
}
