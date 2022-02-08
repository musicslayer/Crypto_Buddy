package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Collections;

public class ADA_Testnet_Shelley extends Network {
    public boolean isMainnet() {
        return false;
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
        return "ADA_Testnet_Shelley";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Testnet Shelley (Shelley Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("addr_test1") && address.length() == 108 && Bech32.hasValidChecksum(address);
    }
}
