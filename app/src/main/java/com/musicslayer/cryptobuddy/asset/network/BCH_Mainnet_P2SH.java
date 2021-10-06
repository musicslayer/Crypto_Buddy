package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class BCH_Mainnet_P2SH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public String getName() {
        return "BCH_Mainnet_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Script (p2sh)";
    }

    public boolean isValid(String address) {
        if(!DecodeUtil.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = DecodeUtil.getAddressNetworkID(address);
        return networkID == 5;
    }
}
