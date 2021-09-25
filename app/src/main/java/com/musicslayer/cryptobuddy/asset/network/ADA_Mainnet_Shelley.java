package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.Decode;

// TODO ADA addresses can be in BECH32 or in original format.

public class ADA_Mainnet_Shelley extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ADA");
    }

    public String getName() {
        return "ADA_Mainnet_Shelley";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Shelley (Shelley Era)";
    }

    public boolean isValid(String address) {
        return address.startsWith("addr1") && address.length() == 103 && Decode.hasValidBech32Checksum(address);
    }
}
