package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class XTZ_Testnet_Granadanet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("XTZ");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("XTZTokenManager")));
    }

    public String getName() {
        return "XTZ_Testnet_Granadanet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Granadanet";
    }

    public boolean isValid(String address) {
        return address.length() == 36 && (address.startsWith("tz1") || address.startsWith("tz2") || address.startsWith("tz3"));
    }
}
