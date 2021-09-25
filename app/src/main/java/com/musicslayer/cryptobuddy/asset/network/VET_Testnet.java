package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class VET_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("VET");
    }

    public String getName() {
        return "VET_Testnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet";
    }

    public boolean isValid(String address) {
        return Decode.isValidBlockchainAddress(address);
    }
}
