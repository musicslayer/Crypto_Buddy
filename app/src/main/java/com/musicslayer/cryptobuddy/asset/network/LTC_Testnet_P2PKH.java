package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

public class LTC_Testnet_P2PKH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("LTC");
    }

    public String getName() {
        return "LTC_Testnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Pubkey (p2pkh)";
    }

    public boolean isValid(String address) {
        String prefix = "litecoin:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!Decode.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = Decode.getAddressNetworkID(address);
        return networkID == 111;
    }
}
