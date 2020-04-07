package com.rudderlabs.android.integration.facebook;

import android.os.Bundle;

import com.google.gson.Gson;

import java.util.Map;

public class Utils {
    static Bundle getBundleForMap(Map<String, Object> objectMap) {
        if (objectMap == null) return null;
        Bundle bundle = new Bundle();
        for (String key : objectMap.keySet()) {
            Object value = objectMap.get(key);
            if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer || value instanceof Short) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof Float || value instanceof Double) {
                bundle.putFloat(key, (Float) value);
            } else {
                bundle.putString(key, new Gson().toJson(value));
            }
        }
        return bundle;
    }
}
