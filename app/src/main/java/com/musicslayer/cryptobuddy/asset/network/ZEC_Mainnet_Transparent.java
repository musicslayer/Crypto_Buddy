package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class ZEC_Mainnet_Transparent extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("ZEC");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>();
    }

    public String getName() {
        return "ZEC_Mainnet_Transparent";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet (Transparent Address)";
    }

    public String getPrefix() {
        return "zcash:";
    }

    public boolean isValid(String address) {
        return address.length() == 35 && address.startsWith("t");
    }
}
