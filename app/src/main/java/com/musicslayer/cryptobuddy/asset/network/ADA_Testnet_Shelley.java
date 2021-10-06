package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class ADA_Testnet_Shelley extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ADA");
    }

    public String getName() {
        return "ADA_Testnet_Shelley";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Shelley (Shelley Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("addr_test1") && address.length() == 108 && DecodeUtil.hasValidBech32Checksum(address);
    }
}
