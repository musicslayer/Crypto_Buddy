package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class ADA_Mainnet_Icarus extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ADA");
    }

    public String getName() {
        return "ADA_Mainnet_Icarus";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Icarus (Byron Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("Ae2") && address.length() == 59;
    }
}
