package com.musicslayer.cryptobuddy.asset.fiat;

public class UnknownFiat extends Fiat {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_FIAT?";
        }
        else {
            return "?UNKNOWN_FIAT (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Fiat?";
        }
        else {
            return "?Unknown Fiat (" + key + ")?";
        }
    }

    public int getScale() { return 0; }

    public static UnknownFiat createUnknownFiat(String key) {
        return new UnknownFiat(key);
    }

    private UnknownFiat(String key) {
        this.key = key;
    }
}
