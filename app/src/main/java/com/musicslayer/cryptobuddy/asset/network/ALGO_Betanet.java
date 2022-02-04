package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base32;

import java.util.ArrayList;
import java.util.Collections;

public class ALGO_Betanet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ALGO");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("AlgoTokenManager")));
    }

    public String getName() {
        return "ALGO_Betanet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Betanet";
    }

    public String getPrefix() {
        return "algorand://";
    }

    public boolean isValid(String address) {
        return address.length() == 58 && Base32.isAddress(address);
    }
}
