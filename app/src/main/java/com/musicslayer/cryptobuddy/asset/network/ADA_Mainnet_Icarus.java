package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class ADA_Mainnet_Icarus extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("ADA");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("ADATokenManager")));
    }

    public String getName() {
        return "ADA_Mainnet_Icarus";
    }

    public String getDisplayName() {
        return "Cardano Mainnet Icarus (Byron Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("Ae2") && address.length() == 59;
    }
}
