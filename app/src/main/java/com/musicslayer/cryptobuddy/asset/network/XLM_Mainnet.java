package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class XLM_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("XLM");
    }

    public String getName() {
        return "XLM_Mainnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet (Public Network)";
    }

    public boolean isValid(String address) {
        return address.length() == 56 && address.startsWith("G") && Decode.hasValidBase32Checksum(address);
    }
}
