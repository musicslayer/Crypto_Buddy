package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base58;

public class DASH_Mainnet_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("DASH");
    }

    public String getName() {
        return "DASH_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Pubkey (p2pkh)";
    }

    public boolean isValid(String address) {
        String prefix = "dash:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!Base58.hasValidChecksum(address)) {
            return false;
        }

        int networkID = Base58.getAddressNetworkID(address);
        return networkID == 76;
    }
}
