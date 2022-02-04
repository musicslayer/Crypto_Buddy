package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;
import java.util.Collections;

public class ADA_Mainnet_Daedalus extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ADA");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("ADATokenManager")));
    }

    public String getName() {
        return "ADA_Mainnet_Daedalus";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Daedalus (Byron Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("DdzFFz") && address.length() == 104;
    }
}
