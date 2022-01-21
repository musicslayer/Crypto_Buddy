package com.musicslayer.cryptobuddy.util;

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

public class URLUtil {
    public static HashMap<String, String> parseURL(String url) {
        // Returns a HashMap of all of the parameter names and values.
        try {
            HashMap<String, String> parameters = new HashMap<>();
            String query = new URL(url).getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                parameters.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            return parameters;
        }
        catch(Exception ignored) {
            return new HashMap<>();
        }
    }
}
