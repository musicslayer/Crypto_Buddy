package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;

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
    public String getUSDPrice(Crypto crypto) { return "?"; }
    public String getUSDMarketCap(Crypto crypto) { return "?"; }

    public static UnknownPriceAPI createUnknownPriceAPI(String key) {
        return new UnknownPriceAPI(key);
    }

    private UnknownPriceAPI(String key) {
        this.key = key;
    }
}
