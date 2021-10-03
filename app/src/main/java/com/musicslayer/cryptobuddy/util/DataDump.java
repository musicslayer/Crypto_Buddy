package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.persistence.Persistence;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to dump all app data in a user's installation.
public class DataDump {
    public static final String INDENT = "  ";

    public static String getAllData() {
        StringBuilder s = new StringBuilder();
        s.append("Data Dump:");

        // Add all user device data.

        // Add all persistent data.
        HashMap<String, HashMap<String, String>> allDataMap = Persistence.getAllData();
        ArrayList<String> allDataKeys = new ArrayList<>(allDataMap.keySet());
        for(String allDataKey : allDataKeys) {
            s.append("\n\n");
            s.append(allDataKey).append(":");

            HashMap<String, String> hashMap = allDataMap.get(allDataKey);

            // If we have no data at all, not even error data, then just skip this map.
            if(hashMap == null) {
                continue;
            }

            // Append data, including error data.
            ArrayList<String> keys = new ArrayList<>(hashMap.keySet());
            for(String key : keys) {
                s.append("\n").append(INDENT).append(key).append(" = ").append(hashMap.get(key));
            }
        }

        return s.toString();
    }
}
