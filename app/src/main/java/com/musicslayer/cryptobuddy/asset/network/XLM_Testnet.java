package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base32;

public class XLM_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("XLM");
    }

    public String getName() {
        return "XLM_Testnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet";
    }

    public boolean isValid(String address) {
        return address.length() == 56 && address.startsWith("G") && Base32.isAddress(address);
    }
}
