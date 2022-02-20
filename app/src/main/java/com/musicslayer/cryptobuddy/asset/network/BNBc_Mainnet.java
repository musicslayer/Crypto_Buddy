package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BNBc_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("BNBc");
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
        return "BNBc_Mainnet";
    }

    public String getDisplayName() {
        return "Binance Coin Mainnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("bnb1") && Bech32.hasValidChecksum(address);
    }
}
