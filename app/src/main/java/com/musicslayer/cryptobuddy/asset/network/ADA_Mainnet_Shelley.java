package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Bech32;

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
        return address.startsWith("addr1") && address.length() == 103 && Bech32.hasValidChecksum(address);
    }
}
