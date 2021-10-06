package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class LTC_Mainnet_P2SH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("LTC");
    }

    public String getName() {
        return "LTC_Mainnet_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Script (p2sh)";
    }

    public boolean isValid(String address) {
        String prefix = "litecoin:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!DecodeUtil.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = DecodeUtil.getAddressNetworkID(address);
        return networkID == 5 || networkID == 50;
    }
}
