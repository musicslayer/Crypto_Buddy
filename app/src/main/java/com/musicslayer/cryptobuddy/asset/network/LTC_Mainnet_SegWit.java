package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class LTC_Mainnet_SegWit extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("LTC");
    }

    public String getName() {
        return "LTC_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Segwit (p2wphk)";
    }

    public boolean isValid(String address) {
        return address.startsWith("ltc1");
    }
}
