package com.musicslayer.cryptobuddy.asset.network;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// For now, SegWit is always bc1/ltc1, but in the future the number could change. There are also different kinds of SegWit.

// Cardano ADA_Shelley has addresses that are 58 characters instead of 103
// addr1v9s96gdnn9nmhmyz2duu0ghgnt6wvzdjkavkcv92smj69uc4rsp5h
// CardanoExplorer.java can get balance but not transactions.

abstract public class Network implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getKey());
    }

    public static final Parcelable.Creator<Network> CREATOR = new Parcelable.Creator<Network>() {
        @Override
        public Network createFromParcel(Parcel in) {
            return Network.getNetworkFromKey(in.readString());
        }

        @Override
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public boolean isMainnet();
    abstract public boolean isCaseSensitive();
    abstract public Coin getPrimaryCoin();
    abstract public Coin getFeeCoin();
    abstract public ArrayList<Coin> getCoins();
    abstract public ArrayList<TokenManager> getTokenManagers();
    abstract public String getName();
    abstract public String getDisplayName();
    abstract public boolean isValid(String address);

    // Most coins have no prefixes but some could.
    public String getPrefix() {
        return null;
    }

    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    public boolean matchesAddress(String address, String other) {
        // Compare addresses using the case sensitivity of the network.
        if(isCaseSensitive()) {
            return address.equals(other);
        }
        else {
            return address.equalsIgnoreCase(other);
        }
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Network) && getKey().equals(((Network)other).getKey());
    }

    public static ArrayList<Network> networks;
    public static HashMap<String, Network> network_map;
    public static ArrayList<String> network_names;
    public static ArrayList<String> network_display_names;

    public static void initialize() {
        network_names = FileUtil.readFileIntoLines(R.raw.asset_network);

        networks = new ArrayList<>();
        network_map = new HashMap<>();
        network_display_names = new ArrayList<>();

        for(String networkName : network_names) {
            Network network = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.network." + networkName);
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

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws org.json.JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("key", getKey(), String.class)
            .toStringOrNull();
    }

    public static Network legacy_deserializeFromJSON(String s, String version) throws org.json.JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        String key = o.deserialize("key", String.class);
        return Network.getNetworkFromKey(key);
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("key", getKey(), String.class)
                .endObject();
    }

    public static Network deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Network network;

        if("2".equals(version)) {
            String key = o.deserialize("key", String.class);
            o.endObject();

            network = Network.getNetworkFromKey(key);
        }
        else {
            throw new IllegalStateException();
        }

        return network;
    }
}
