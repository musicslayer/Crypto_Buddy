package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

import java.util.ArrayList;
import java.util.HashMap;

public class UnknownPriceAPI extends PriceAPI {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_PRICE_API?";
        }
        else {
            return "?UNKNOWN_PRICE_API (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Price API?";
        }
        else {
            return "?Unknown Price API (" + key + ")?";
        }
    }

    public boolean isSupported(Crypto crypto) { return false; }
    public HashMap<Crypto, AssetQuantity> getBulkPrice(ArrayList<Crypto> cryptoArrayList) { return null; }
    public HashMap<Crypto, AssetQuantity> getBulkMarketCap(ArrayList<Crypto> cryptoArrayList) { return null; }
    public AssetQuantity getPrice(Crypto crypto) { return null; }
    public AssetQuantity getMarketCap(Crypto crypto) { return null; }

    public static UnknownPriceAPI createUnknownPriceAPI(String key) {
        return new UnknownPriceAPI(key);
    }

    private UnknownPriceAPI(String key) {
        this.key = key;
    }
}
