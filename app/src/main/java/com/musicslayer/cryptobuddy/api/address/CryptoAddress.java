package com.musicslayer.cryptobuddy.api.address;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.persistence.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

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
            catch(Exception ignored) {
                continue;
            }

            if(isValid) {
                cryptoAddressArrayList.add(new CryptoAddress(address, n, includeTokens));
            }
        }

        return cryptoAddressArrayList;
    }

    public String serialize() {
        return "{\"address\":\"" + address + "\",\"network\":" + network.serialize() + ",\"includeTokens\":\"" + Boolean.toString(includeTokens) + "\"}";
    }

    public static String serializeArray(ArrayList<CryptoAddress> arrayList) {
        StringBuilder s = new StringBuilder();
        s.append("[");

        for(int i = 0; i < arrayList.size(); i++) {
            s.append(arrayList.get(i).serialize());

            if(i < arrayList.size() - 1) {
                s.append(",");
            }
        }

        s.append("]");
        return s.toString();
    }

    public static CryptoAddress deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            String address = o.getString("address");
            Network network = Network.deserialize(o.getJSONObject("network").toString());
            boolean includeTokens = Boolean.parseBoolean(o.getString("includeTokens"));
            return new CryptoAddress(address, network, includeTokens);
        }
        catch(Exception e) {
            return null;
        }
    }

    public static ArrayList<CryptoAddress> deserializeArray(String s) {
        try {
            ArrayList<CryptoAddress> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                arrayList.add(CryptoAddress.deserialize(o.toString()));
            }

            return arrayList;
        }
        catch(Exception e) {
            return null;
        }
    }
}
