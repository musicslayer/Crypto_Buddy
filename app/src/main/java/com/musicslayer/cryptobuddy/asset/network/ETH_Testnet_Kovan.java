package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class ETH_Testnet_Kovan extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ETH");
    }

    public String getName() {
        return "ETH_Testnet_Kovan";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Kovan";
    }

    public boolean isValid(String address) {
        return Decode.isValidBlockchainAddress(address);
    }
}
