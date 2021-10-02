package com.musicslayer.cryptobuddy.api.price;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Reflect;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class PriceAPI extends API {
    public static ArrayList<PriceAPI> price_apis;
    public static HashMap<String, PriceAPI> price_api_map;
    public static ArrayList<String> price_api_names;
    public static ArrayList<String> price_api_display_names;

    public static void initialize(Context context) {
        price_api_names = File.readFileIntoLines(context, R.raw.api_price);

        price_apis = new ArrayList<>();
        price_api_map = new HashMap<>();
        price_api_display_names = new ArrayList<>();

        for(String priceName : price_api_names) {
            PriceAPI priceAPI = Reflect.constructClassInstanceFromName("com.musicslayer.cryptobuddy.api.price." + priceName);
            price_apis.add(priceAPI);
            price_api_map.put(priceName, priceAPI);
            price_api_display_names.add(priceAPI.getDisplayName());
        }
    }

    abstract public boolean isSupported(Crypto crypto);
    abstract public String getUSDPrice(Crypto crypto);
    abstract public String getUSDMarketCap(Crypto crypto);

    public static PriceAPI getPriceAPIFromKey(String key) {
        PriceAPI priceAPI = price_api_map.get(key);
        if(priceAPI == null) {
            priceAPI = UnknownPriceAPI.createUnknownPriceAPI(key);
        }

        return priceAPI;
    }
}