package com.musicslayer.cryptobuddy.asset.crypto.coin;

public class UnknownCoin extends Coin {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_COIN?";
        }
        else {
            return "?UNKNOWN_COIN (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Coin?";
        }
        else {
            return "?Unknown Coin (" + key + ")?";
        }
    }

    public int getScale() { return 0; }

    public String getID() { return "?"; }

    public static UnknownCoin createUnknownCoin(String key) {
        return new UnknownCoin(key);
    }

    private UnknownCoin(String key) {
        this.key = key;
    }
}
