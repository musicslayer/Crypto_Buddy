package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base58;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TRX_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("TRX");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Arrays.asList(TokenManager.getTokenManagerFromKey("TronSmartTokenManager"), TokenManager.getTokenManagerFromKey("TronTokenManager")));
    }

    public String getName() {
        return "TRX_Mainnet";
    }

    public String getDisplayName() {
        return "Tron Mainnet";
    }

    public boolean isValid(String address) {
        return address.length() == 34 && address.startsWith("T") && Base58.isAddress(address);
    }
}
