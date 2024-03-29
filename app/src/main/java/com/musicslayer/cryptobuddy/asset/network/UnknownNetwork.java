package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.UnknownTokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class UnknownNetwork extends Network {
    String key;

    public String getKey() { return key; }

    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return UnknownCoin.createUnknownCoin(null, null, null, 0, null, "?");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(UnknownTokenManager.createUnknownTokenManager("?", "?")));
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
