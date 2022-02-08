package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class XTZ_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("XTZ");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("XTZTokenManager")));
    }

    public String getName() {
        return "XTZ_Mainnet";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet";
    }

    public boolean isValid(String address) {
        return address.length() == 36 && (address.startsWith("tz1") || address.startsWith("tz2") || address.startsWith("tz3"));
    }
}
