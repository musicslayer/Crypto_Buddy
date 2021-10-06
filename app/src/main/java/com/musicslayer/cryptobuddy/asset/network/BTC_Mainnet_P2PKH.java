package com.musicslayer.cryptobuddy.asset.network;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.util.DecodeUtil;

public class BTC_Mainnet_P2PKH extends Network {
    public boolean isMainnet() {
        return true;
    }

    public Crypto getCrypto() {
        return Coin.getCoinFromKey("BTC");
    }

    public String getName() {
        return "BTC_Mainnet_P2PKH";
    }

    public String getDisplayName() {
        return this.getCrypto().getDisplayName() + " Mainnet Pubkey (p2pkh)";
    }

    public boolean isValid(String address) {
        String prefix = "bitcoin:";
        if(address.startsWith(prefix)) {
            address = address.substring(prefix.length());
        }

        if(!DecodeUtil.hasValidBase58Checksum(address)) {
            return false;
        }

        int networkID = DecodeUtil.getAddressNetworkID(address);
        return networkID == 0;
    }
}
