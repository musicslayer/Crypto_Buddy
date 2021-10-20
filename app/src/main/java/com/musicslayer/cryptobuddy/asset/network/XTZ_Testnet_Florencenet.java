package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class XTZ_Testnet_Florencenet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("XTZ");
    }

    public String getName() {
        return "XTZ_Testnet_Florencenet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Florencenet";
    }

    public boolean isValid(String address) {
        return address.length() == 36 && (address.startsWith("tz1") || address.startsWith("tz2") || address.startsWith("tz3"));
    }
}
