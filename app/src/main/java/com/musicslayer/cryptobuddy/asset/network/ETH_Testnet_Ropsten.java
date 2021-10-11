package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.decode.Ethereum;

public class ETH_Testnet_Ropsten extends Network {
    public boolean isMainnet() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("ETH");
    }

    public String getName() {
        return "ETH_Testnet_Ropsten";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Testnet Ropsten";
    }

    public boolean isValid(String address) {
        return Ethereum.isAddress(address);
    }
}
