package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class KAVA_Mainnet extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("KAVA");
    }

    public String getName() {
        return "KAVA_Mainnet";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet";
    }

    public boolean isValid(String address) {
        return address.startsWith("kava1") && DecodeUtil.hasValidBech32Checksum(address);
    }
}
