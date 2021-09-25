package com.musicslayer.cryptobuddy.api.address;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.persistence.Settings;

import java.io.Serializable;
import java.util.ArrayList;

// A string address could belong to more than one network
public class CryptoAddress implements Serializable {
    public String address;
    public Network network;
    public boolean includeTokens;

    public CryptoAddress(String address, Network network, boolean includeTokens) {
        this.address = address;
        this.network = network;
        this.includeTokens = includeTokens;
    }

    // For now, just get the crypto from the network.
    public Crypto getCrypto() {
        return network.getCrypto();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoAddress) &&
                ((this.address == null && ((CryptoAddress)other).address == null) || address.equals(((CryptoAddress) other).address)) &&
                ((this.network == null && ((CryptoAddress)other).network == null) || network.equals(((CryptoAddress) other).network)) &&
                (includeTokens == ((CryptoAddress) other).includeTokens);

    }

    @NonNull
    @Override
    public String toString() {
        if(includeTokens) {
            return "(" + network.getDisplayName() + ") (Coins + Tokens) " + address;
        }
        else {
            return "(" + network.getDisplayName() + ") (Coins) " + address;
        }
    }

    public static ArrayList<CryptoAddress> getAllValidCryptoAddress(String address, boolean includeTokens) {
        ArrayList<CryptoAddress> cryptoAddressArrayList = new ArrayList<>();
        boolean rejectTestnet = "Mainnet".equals(Settings.setting_network);

        // Find all valid cryptos.
        for(Network n : Network.networks) {
            // Only support Testnets if the setting says to.
            if(rejectTestnet && !n.isMainnet()) {
                continue;
            }

            boolean isValid;
            try {
                isValid = n.isValid(address);
            }
            catch(java.lang.Exception ignored) {
                continue;
            }

            if(isValid) {
                cryptoAddressArrayList.add(new CryptoAddress(address, n, includeTokens));
            }
        }

        return cryptoAddressArrayList;
    }
}
