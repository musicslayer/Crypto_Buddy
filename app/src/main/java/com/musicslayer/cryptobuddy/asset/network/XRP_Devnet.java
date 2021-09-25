package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class XRP_Devnet extends Network {
    public boolean isMainnet() {
        return false;
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
        return address.length() >= 25 && address.length() <= 35 && address.startsWith("r") && Decode.isBase58(address);
    }
}
