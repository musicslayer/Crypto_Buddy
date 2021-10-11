package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base58;

// The address of a normal account in Solana is a Base58-encoded string of a 256-bit ed25519 public key.

public class SOL_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("SOL");
    }

    public String getName() {
        return "SOL_Mainnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet (Beta)";
    }

    public boolean isValid(String address) {
        return address.length() >= 32 && address.length() <= 44 && Base58.isAddress(address) && Base58.hasByteLength(address, 32);
    }
}
