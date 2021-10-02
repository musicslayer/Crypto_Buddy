package com.musicslayer.cryptobuddy.asset.network;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Reflect;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

// TODO For now, SegWit is always bc1/ltc1, but in the future the number could change. There are also different kinds of SegWit.

abstract public class Network implements Serialization.SerializableToJSON {
    abstract public boolean isMainnet();
    abstract public Crypto getCrypto();
    abstract public String getName();
    abstract public String getDisplayName();
    abstract public boolean isValid(String address);

    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Network) && getClass().equals(other.getClass());
    }

    public static ArrayList<Network> networks;
    public static HashMap<String, Network> network_map;
    public static ArrayList<String> network_names;
    public static ArrayList<String> network_display_names;

    public static void initialize(Context context) {
        network_names = File.readFileIntoLines(context, R.raw.asset_network);

        networks = new ArrayList<>();
        network_map = new HashMap<>();
        network_display_names = new ArrayList<>();

        for(String networkName : network_names) {
            Network network = Reflect.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.network." + networkName);
            networks.add(network);
            network_map.put(networkName, network);
            network_display_names.add(network.getDisplayName());
        }
    }

    public static Network getNetworkFromKey(String key) {
        Network network = network_map.get(key);
        if(network == null) {
            network = UnknownNetwork.createUnknownNetwork(key);
        }

        return network;
    }

    private int compare(Network other) {
        // Mainnets come first, and then sort alphabetically.
        // We do not differentiate between testnets and devnets.
        boolean isMainnetA = this.isMainnet();
        boolean isMainnetB = other.isMainnet();

        int s = Boolean.compare(!isMainnetA, !isMainnetB);
        if(s != 0) {
            return s;
        }
        else {
            return this.getDisplayName().toLowerCase().compareTo(other.getDisplayName().toLowerCase());
        }
    }

    public static int compare(Network a, Network b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real action.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public String serializeToJSON() {
        return "{\"key\":\"" + getKey() + "\"}";
    }

    public static Network deserializeFromJSON(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        String key = o.getString("key");
        return Network.getNetworkFromKey(key);
    }
}
