package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Collections;

public class ADA_Mainnet_Shelley extends Network {
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

    public String getName() {
        return "ADA_Mainnet_Shelley";
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("ADATokenManager")));
    }

    public String getDisplayName() {
        return "Cardano Mainnet Shelley (Shelley Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("addr1") && address.length() == 103 && Bech32.hasValidChecksum(address);
    }
}
