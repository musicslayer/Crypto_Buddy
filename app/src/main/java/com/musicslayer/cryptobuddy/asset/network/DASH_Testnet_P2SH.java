package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class DASH_Testnet_P2SH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("DASH");
    }

    public String getName() {
        return "DASH_Testnet_P2SH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Script (p2sh)";
    }

    public boolean isValid(String address) {
        String prefix = "dash:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!Decode.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = Decode.getAddressNetworkID(address);
        return networkID == 19;
    }
}
