package com.musicslayer.cryptobuddy.api.address;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.NetworksSetting;

import java.util.ArrayList;

public class CryptoAddress implements Serialization.SerializableToJSON {
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

    public boolean matchesAddress(String address) {
        // Compare addresses using the case sensitivity of the network.
        if(network.isCaseSensitive()) {
            return this.address.equals(address);
        }
        else {
            return this.address.equalsIgnoreCase(address);
        }
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoAddress) &&
            ((address == null && ((CryptoAddress)other).address == null) || (address != null && ((CryptoAddress) other).address != null && address.equals(((CryptoAddress) other).address))) &&
            ((network == null && ((CryptoAddress)other).network == null) || (network != null && ((CryptoAddress) other).network != null && network.equals(((CryptoAddress) other).network))) &&
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
        boolean rejectTestnet = "Mainnet".equals(NetworksSetting.value);

        // Search for prefix, and if found separate it from the address.
        String address_prefix = null;
        int idx = address.indexOf(":");
        if(idx != -1) {
            address_prefix = address.substring(0, idx + 1);
            if("algorand:".equalsIgnoreCase(address_prefix)) {
                // Special case for Algorand.
                idx += 2;
                address_prefix = address_prefix + "//";
            }

            address = address.substring(idx + 1);
        }

        // Find all valid cryptos.
        for(Network n : Network.networks) {
            // Only support Testnets if the setting says to.
            if(rejectTestnet && !n.isMainnet()) {
                continue;
            }

            // If address has a prefix, then only support networks with that prefix.
            if(address_prefix != null && !address_prefix.equalsIgnoreCase(n.getPrefix())) {
                continue;
            }

            boolean isValid;
            try {
                isValid = n.isValid(address);
            }
            catch(Exception ignored) {
                continue;
            }

            if(isValid) {
                cryptoAddressArrayList.add(new CryptoAddress(address, n, includeTokens));
            }
        }

        return cryptoAddressArrayList;
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("address", Serialization.string_serialize(address))
            .put("network", new Serialization.JSONObjectWithNull(Serialization.serialize(network)))
            .put("includeTokens", Serialization.boolean_serialize(includeTokens))
            .toStringOrNull();
    }

    public static CryptoAddress deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String address = Serialization.string_deserialize(o.getString("address"));
        Network network = Serialization.deserialize(o.getJSONObjectString("network"), Network.class);
        boolean includeTokens = Serialization.boolean_deserialize(o.getString("includeTokens"));
        return new CryptoAddress(address, network, includeTokens);
    }
}
