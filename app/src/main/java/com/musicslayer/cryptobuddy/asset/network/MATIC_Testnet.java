package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class MATIC_Testnet extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("MATIC");
    }

    public String getName() {
        return "MATIC_Testnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Mumbai";
    }

    public boolean isValid(String address) {
        return DecodeUtil.isValidBlockchainAddress(address);
    }
}
