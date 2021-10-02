package com.musicslayer.cryptobuddy.api.address;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

// A string address could belong to more than one network
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
        return new JSONObject()
            .put("address", Serialization.string_serialize(address))
            .put("network", new JSONObject(Serialization.serialize(network)))
            .put("includeTokens", Serialization.boolean_serialize(includeTokens))
            .toString();
    }

    public static CryptoAddress deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        String address = Serialization.string_deserialize(o.getString("address"));
        Network network = Serialization.deserialize(o.getJSONObject("network").toString(), Network.class);
        boolean includeTokens = Serialization.boolean_deserialize(o.getString("includeTokens"));
        return new CryptoAddress(address, network, includeTokens);
    }
}
