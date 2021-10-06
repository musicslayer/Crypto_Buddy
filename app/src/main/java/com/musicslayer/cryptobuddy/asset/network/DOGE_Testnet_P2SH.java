package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class DOGE_Testnet_P2SH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("DOGE");
    }

    public String getName() {
        return "DOGE_Testnet_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Script (p2sh)";
    }

    public boolean isValid(String address) {
        String prefix = "dogecoin:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!DecodeUtil.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = DecodeUtil.getAddressNetworkID(address);
        return networkID == 196;
    }
}
