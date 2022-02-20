package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Collections;

public class KAVA_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("KAVA");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("KavaTokenManager")));
    }

    public String getName() {
        return "KAVA_Testnet";
    }

    public String getDisplayName() {
        return "Kava Testnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("kava1") && Bech32.hasValidChecksum(address);
    }
}
