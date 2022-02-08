package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BNBc_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return Coin.getCoinFromKey("BNBc");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Arrays.asList(TokenManager.getTokenManagerFromKey("BinanceChainMiniTokenManager"), TokenManager.getTokenManagerFromKey("BinanceChainTokenManager")));
    }

    public String getName() {
        return "BNBc_Testnet";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Testnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("tbnb1") && Bech32.hasValidChecksum(address);
    }
}
