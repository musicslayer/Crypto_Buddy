package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class LTC_Testnet_SegWit extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("LTC");
    }

    public String getName() {
        return "LTC_Testnet_SegWit";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Segwit (p2wphk)";
    }

    public boolean isValid(String address) {
        return address.startsWith("tltc1");
    }
}
