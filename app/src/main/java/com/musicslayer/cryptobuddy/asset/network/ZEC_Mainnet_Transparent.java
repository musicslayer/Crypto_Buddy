package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;

public class ZEC_Mainnet_Transparent extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ZEC");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>();
    }

    public String getName() {
        return "ZEC_Mainnet_Transparent";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet (Transparent Address)";
    }

    public String getPrefix() {
        return "zcash:";
    }

    public boolean isValid(String address) {
        return address.length() == 35 && address.startsWith("t");
    }
}
