package com.musicslayer.cryptobuddy.asset.exchange;

public class UnknownExchange extends Exchange {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_EXCHANGE?";
        }
        else {
            return "?UNKNOWN_EXCHANGE (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Exchange?";
        }
        else {
            return "?Unknown Exchange (" + key + ")?";
        }
    }

    public static UnknownExchange createUnknownExchange(String key) {
        return new UnknownExchange(key);
    }

    private UnknownExchange(String key) {
        this.key = key;
    }
}
