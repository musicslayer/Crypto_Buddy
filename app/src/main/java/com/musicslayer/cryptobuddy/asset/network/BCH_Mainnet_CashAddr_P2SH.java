package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class BCH_Mainnet_CashAddr_P2SH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public String getName() {
        return "BCH_Mainnet_CashAddr_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet CashAddr Script (p2sh)";
    }

    public String getPrefix() {
        return "bitcoincash:";
    }

    public boolean isValid(String address) {
        return address.startsWith("p");
    }
}
