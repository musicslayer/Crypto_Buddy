package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.PrivacyPolicy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;

import java.util.ArrayList;
import java.util.HashMap;

// Methods to quickly manipulate all persistent app data on a user's device.
public class DataDump {
    public static final String INDENT = "  ";

    public static String getAllData() {
        // Note that the app state may be corrupt, so tread carefully!
        StringBuilder s = new StringBuilder();
        s.append("Data Dump:");

        // TokenList has too much data so omit that one.
        appendData(s, "AddressHistory", AddressHistory.class);
        appendData(s, "AddressPortfolio", AddressPortfolio.class);
        appendData(s, "PrivacyPolicy", PrivacyPolicy.class);
        appendData(s, "Purchases", Purchases.class);
        appendData(s, "Review", Review.class);
        appendData(s, "Settings", Settings.class);
        appendData(s, "TransactionPortfolio", TransactionPortfolio.class);

        return s.toString();
    }

    public static <T> void appendData(StringBuilder s, String name, Class<T> clazz) {
        s.append("\n\n");

        try {
            HashMap<String, String> hashMap = Reflect.callStaticMethodOrError(clazz, "getDataDump");

            s.append(name).append(":");
            ArrayList<String> keys = new ArrayList<>(hashMap.keySet());
            for(String key : keys) {
                s.append("\n").append(INDENT).append(key).append(" = ").append(hashMap.get(key));
            }
        }
        catch(Exception e) {
            try {
                ExceptionLogger.processException(e);
                s.append("Error Getting ").append(name).append(":");
                s.append("\n").append(ExceptionLogger.getExceptionText(e));
            }
            catch(Exception ignored) {
                // Even the error logging code failed! Just give up at this point.
            }
        }
    }
}
