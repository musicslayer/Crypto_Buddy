package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Ethereum;

public class CLO_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("CLO");
    }

    public String getName() {
        return "CLO_Testnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
