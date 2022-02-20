package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Ethereum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class VET_Testnet extends Network {
    public boolean isMainnet() {
        return false;
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
        return "VET_Testnet";
    }

    public String getDisplayName() {
        return "VeChain Testnet";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
