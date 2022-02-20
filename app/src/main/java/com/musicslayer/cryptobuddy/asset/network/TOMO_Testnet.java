package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Ethereum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TOMO_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("TOMO");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Arrays.asList(TokenManager.getTokenManagerFromKey("TomoChainTokenManager"), TokenManager.getTokenManagerFromKey("TomoChainZTokenManager")));
    }

    public String getName() {
        return "TOMO_Testnet";
    }

    public String getDisplayName() {
        return "TomoChain Testnet";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
