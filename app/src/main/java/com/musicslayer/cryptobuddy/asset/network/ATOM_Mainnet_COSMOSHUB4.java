package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Bech32;

public class ATOM_Mainnet_COSMOSHUB4 extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ATOM");
    }

    public String getName() {
        return "ATOM_Mainnet_COSMOSHUB4";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet (cosmoshub-4)";
    }

    public boolean isValid(String address) {
        return address.length() <= 255 && address.startsWith("cosmos1") && Bech32.hasValidChecksum(address);
    }
}
