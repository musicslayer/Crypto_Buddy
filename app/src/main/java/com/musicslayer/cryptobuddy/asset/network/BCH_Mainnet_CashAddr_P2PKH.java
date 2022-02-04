package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.ArrayList;

public class BCH_Mainnet_CashAddr_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BCH");
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>();
    }

    public String getName() {
        return "BCH_Mainnet_CashAddr_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet CashAddr Pubkey (p2pkh)";
    }

    public String getPrefix() {
        return "bitcoincash:";
    }

    public boolean isValid(String address) {
        return address.startsWith("q");
    }
}
