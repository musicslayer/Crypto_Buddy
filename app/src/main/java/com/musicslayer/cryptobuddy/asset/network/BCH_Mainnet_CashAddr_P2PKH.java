package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;

public class BCH_Mainnet_CashAddr_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public String getName() {
        return "BCH_Mainnet_CashAddr_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet CashAddr Pubkey (p2pkh)";
    }

    public boolean isValid(String address) {
        String prefix = "bitcoincash:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        return address.startsWith("q");
    }
}
