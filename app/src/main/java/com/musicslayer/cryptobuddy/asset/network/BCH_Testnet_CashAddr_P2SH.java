package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class BCH_Testnet_CashAddr_P2SH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("BCH");
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
        return "BCH_Testnet_CashAddr_P2SH";
    }

    public String getDisplayName() {
        return "Bitcoin Cash Testnet CashAddr Script (p2sh)";
    }

    public String getPrefix() {
        return "bchtest:";
    }

    public boolean isValid(String address) {
        return address.startsWith("p");
    }
}
