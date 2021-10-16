package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Base58;

public class DASH_Testnet_P2PKH extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("DASH");
    }

    public String getName() {
        return "DASH_Testnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Pubkey (p2pkh)";
    }

    public String getPrefix() {
        return "dash:";
    }

    public boolean isValid(String address) {
        if(!Base58.hasValidChecksum(address)) {
            return false;
        }

        int networkID = Base58.getAddressNetworkID(address);
        return networkID == 140;
    }
}
