package com.musicslayer.cryptobuddy.api.price;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class PriceAPI extends API {
    public static ArrayList<PriceAPI> price_apis;
    public static HashMap<String, PriceAPI> price_api_map;
    public static ArrayList<String> price_api_names;
    public static ArrayList<String> price_api_display_names;

    public static void initialize(Context context) {
        price_api_names = FileUtil.readFileIntoLines(context, R.raw.api_price);

        price_apis = new ArrayList<>();
        price_api_map = new HashMap<>();
        price_api_display_names = new ArrayList<>();

        for(String priceName : price_api_names) {
            PriceAPI priceAPI = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.api.price." + priceName);
            price_apis.add(priceAPI);
            price_api_map.put(priceName, priceAPI);
            price_api_display_names.add(priceAPI.getDisplayName());
        }
    }

    public boolean isSupported(ArrayList<Crypto> cryptoArrayList) {
        // Everything in the array must be supported.
        for(Crypto crypto : cryptoArrayList) {
            if(!isSupported(crypto)) { return false; }
        }

        return true;
    }

    abstract public boolean isSupported(Crypto crypto);

    abstract public HashMap<Crypto, AssetQuantity> getBulkPrice(ArrayList<Crypto> cryptoArrayList);
    abstract public AssetQuantity getPrice(Crypto crypto);
    abstract public HashMap<Crypto, AssetQuantity> getBulkMarketCap(ArrayList<Crypto> cryptoArrayList);
    abstract public AssetQuantity getMarketCap(Crypto crypto);

    public static PriceAPI getPriceAPIFromKey(String key) {
        PriceAPI priceAPI = price_api_map.get(key);
        if(priceAPI == null) {
            priceAPI = UnknownPriceAPI.createUnknownPriceAPI(key);
        }

        return priceAPI;
    }

    public String getAPIType() {
        return "!PRICEAPI!";
    }
}