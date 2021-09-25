package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class ALGO_Betanet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ALGO");
    }

    public String getName() {
        return "ALGO_Betanet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Betanet";
    }

    public boolean isValid(String address) {
        return address.length() == 58 && Decode.hasValidBase32Checksum(address);
    }
}
