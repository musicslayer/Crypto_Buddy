package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class ETH_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ETH");
    }

    public String getName() {
        return "ETH_Mainnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet";
    }

    public boolean isValid(String address) {
        return Decode.isValidBlockchainAddress(address);
    }
}
