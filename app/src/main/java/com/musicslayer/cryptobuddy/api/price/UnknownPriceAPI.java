package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.UnknownFiat;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

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
    public AssetQuantity getPrice(Crypto crypto) { return new AssetQuantity("1", UnknownFiat.createUnknownFiat(null)); }
    public AssetQuantity getMarketCap(Crypto crypto) { return new AssetQuantity("1", UnknownFiat.createUnknownFiat(null)); }

    public static UnknownPriceAPI createUnknownPriceAPI(String key) {
        return new UnknownPriceAPI(key);
    }

    private UnknownPriceAPI(String key) {
        this.key = key;
    }
}
