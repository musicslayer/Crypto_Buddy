package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Ethereum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class VET_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("VET");
    }

    public Coin getFeeCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("VTHO");
    }

    public ArrayList<Coin> getCoins() {
        // VET has two main coins on its blockchain.
        return new ArrayList<>(Arrays.asList(getPrimaryCoin(), getFeeCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("VETTokenManager")));
    }

    public String getName() {
        return "VET_Mainnet";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
