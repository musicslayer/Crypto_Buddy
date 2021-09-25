package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;

public class UnknownNetwork extends Network {
    String key;

    public String getKey() { return key; }

    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return UnknownCoin.createUnknownCoin(null);
    }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_NETWORK?";
        }
        else {
            return "?UNKNOWN_NETWORK (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Network?";
        }
        else {
            return "?Unknown Network (" + key + ")?";
        }
    }

    public boolean isValid(String address) {
        return true;
    }

    public static UnknownNetwork createUnknownNetwork(String key) {
        return new UnknownNetwork(key);
    }

    private UnknownNetwork(String key) {
        this.key = key;
    }
}
