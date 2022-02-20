package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class BCH_Mainnet_CashAddr_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
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
        return "BCH_Mainnet_CashAddr_P2PKH";
    }

    public String getDisplayName() {
        return "Bitcoin Cash Mainnet CashAddr Pubkey (p2pkh)";
    }

    public String getPrefix() {
        return "bitcoincash:";
    }

    public boolean isValid(String address) {
        return address.startsWith("q");
    }
}
