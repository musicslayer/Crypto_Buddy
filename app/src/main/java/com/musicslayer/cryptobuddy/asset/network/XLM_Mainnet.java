package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base32;

import java.util.ArrayList;
import java.util.Collections;

public class XLM_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("XLM");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("StellarTokenManager")));
    }

    public String getName() {
        return "XLM_Mainnet";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet (Public Network)";
    }

    public boolean isValid(String address) {
        return address.length() == 56 && address.startsWith("G") && Base32.isAddress(address);
    }
}
