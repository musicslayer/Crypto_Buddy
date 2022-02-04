package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base58;

import java.util.ArrayList;
import java.util.Collections;

public class SOL_Devnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("SOL");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>(Collections.singletonList(TokenManager.getTokenManagerFromKey("SPLTokenManager")));
    }

    public String getName() {
        return "SOL_Devnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Devnet";
    }

    public boolean isValid(String address) {
        return address.length() >= 32 && address.length() <= 44 && Base58.isAddress(address) && Base58.hasByteLength(address, 32);
    }
}
