package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Bech32;

public class BNBc_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BNBc");
    }

    public String getName() {
        return "BNBc_Testnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("tbnb") && Bech32.hasValidChecksum(address);
    }
}
