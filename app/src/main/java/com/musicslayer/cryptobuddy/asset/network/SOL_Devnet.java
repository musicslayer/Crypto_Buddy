package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class SOL_Devnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("SOL");
    }

    public String getName() {
        return "SOL_Devnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Devnet";
    }

    public boolean isValid(String address) {
        return address.length() >= 32 && address.length() <= 44 && Decode.isBase58(address);
    }
}
