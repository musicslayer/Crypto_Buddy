package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Bech32;

import java.util.ArrayList;
import java.util.Collections;

public class LTC_Mainnet_SegWit extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Coin getPrimaryCoin() {
        return CoinManager.getDefaultCoinManager().getHardcodedCoin("LTC");
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
        return "LTC_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return "Litecoin Mainnet Segwit (p2wphk)";
    }

    public boolean isValid(String address) {
        return address.startsWith("ltc1") && Bech32.hasValidChecksum(address);
    }
}
