package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base58;

public class XRP_Devnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("XRP");
    }

    public String getName() {
        return "XRP_Devnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Devnet";
    }

    public boolean isValid(String address) {
        return address.length() >= 25 && address.length() <= 35 && address.startsWith("r") && Base58.isAddress(address);
    }
}
