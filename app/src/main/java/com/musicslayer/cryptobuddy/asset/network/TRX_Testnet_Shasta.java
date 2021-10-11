package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base58;

public class TRX_Testnet_Shasta extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("TRX");
    }

    public String getName() {
        return "TRX_Testnet_Shasta";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Shasta";
    }

    public boolean isValid(String address) {
        return address.length() == 34 && address.startsWith("T") && Base58.isAddress(address);
    }
}
