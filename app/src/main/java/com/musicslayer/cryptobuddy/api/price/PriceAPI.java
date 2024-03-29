package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.asset.Asset;
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

    public static void initialize() {
        price_api_names = FileUtil.readFileIntoLines(R.raw.api_price);

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

    abstract public boolean isSupported(CryptoPrice cryptoPrice);
    abstract public HashMap<Asset, AssetQuantity> getPrice(CryptoPrice cryptoPrice);
    abstract public HashMap<Asset, AssetQuantity> getMarketCap(CryptoPrice cryptoPrice);

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