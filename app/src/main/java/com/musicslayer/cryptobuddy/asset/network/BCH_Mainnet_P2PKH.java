package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class BCH_Mainnet_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public String getName() {
        return "BCH_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Pubkey (p2pkh)";
    }

    public boolean isValid(String address) {
        if(!Decode.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = Decode.getAddressNetworkID(address);
        return networkID == 0;
    }
}
