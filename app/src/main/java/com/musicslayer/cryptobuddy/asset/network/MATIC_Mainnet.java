package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Ethereum;

import java.util.ArrayList;
import java.util.Collections;

public class MATIC_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Coin getPrimaryCoin() {
        return Coin.getCoinFromKey("MATIC");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("PolygonTokenManager")));
    }

    public String getName() {
        return "MATIC_Mainnet";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
