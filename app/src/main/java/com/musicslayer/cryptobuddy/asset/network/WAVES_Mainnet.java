package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base58;

import java.util.ArrayList;
import java.util.Collections;

public class WAVES_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("WAVES");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("WavesTokenManager")));
    }

    public String getName() {
        return "WAVES_Mainnet";
    }

    public String getDisplayName() {
        return "Waves Mainnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("3P") && address.length() <= 44 && Base58.isAddress(address);
    }
}
