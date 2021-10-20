package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class BCH_Testnet_CashAddr_P2SH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public String getName() {
        return "BCH_Testnet_CashAddr_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet CashAddr Script (p2sh)";
    }

    public String getPrefix() {
        return "bchtest:";
    }

    public boolean isValid(String address) {
        return address.startsWith("p");
    }
}
