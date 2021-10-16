package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class ZEC_Testnet_Transparent extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ZEC");
    }

    public String getName() {
        return "ZEC_Testnet_Transparent";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet (Transparent Address)";
    }

    public String getPrefix() {
        return "zcash:";
    }

    public boolean isValid(String address) {
        return address.length() == 35 && address.startsWith("tm");
    }
}
