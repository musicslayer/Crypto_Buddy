package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
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

    public boolean isSupported(CryptoPrice cryptoPrice) { return false; }
    public HashMap<Crypto, AssetQuantity> getPrice(CryptoPrice cryptoPrice) { return null; }
    public HashMap<Crypto, AssetQuantity> getMarketCap(CryptoPrice cryptoPrice) { return null; }

    public static UnknownPriceAPI createUnknownPriceAPI(String key) {
        return new UnknownPriceAPI(key);
    }

    private UnknownPriceAPI(String key) {
        this.key = key;
    }
}
