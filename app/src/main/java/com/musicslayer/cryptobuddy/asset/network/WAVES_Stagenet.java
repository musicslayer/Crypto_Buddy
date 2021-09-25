package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class WAVES_Stagenet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("WAVES");
    }

    public String getName() {
        return "WAVES_Stagenet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Stagenet";
    }

    public boolean isValid(String address) {
        return address.startsWith("3N") && address.length() <= 44 && Decode.isBase58(address);
    }
}
