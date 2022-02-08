package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.decode.Base58;

import java.util.ArrayList;
import java.util.Collections;

public class LTC_Mainnet_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    public Coin getPrimaryCoin() {
        return Coin.getCoinFromKey("LTC");
    }

    public Coin getFeeCoin() {
        return getPrimaryCoin();
    }

    public ArrayList<Coin> getCoins() {
        return new ArrayList<>(Collections.singletonList(getPrimaryCoin()));
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return new ArrayList<>();
    }

    public String getName() {
        return "LTC_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getPrimaryCoin().getDisplayName() + " Mainnet Pubkey (p2pkh)";
    }

    public String getPrefix() {
        return "litecoin:";
    }

    public boolean isValid(String address) {
        if(!Base58.hasValidChecksum(address)) {
            return false;
        }

        int networkID = Base58.getAddressNetworkID(address);
        return networkID == 48;
    }
}
