package com.musicslayer.cryptobuddy.api.address;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.settings.setting.NetworksSetting;

import java.io.IOException;
import java.util.ArrayList;

public class CryptoAddress implements DataBridge.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(address);
        out.writeParcelable(network, flags);
        out.writeString(Boolean.toString(includeTokens)); // Boolean requires newer Android version.
    }

    public static final Parcelable.Creator<CryptoAddress> CREATOR = new Parcelable.Creator<CryptoAddress>() {
        @Override
        public CryptoAddress createFromParcel(Parcel in) {
            String address = in.readString();
            Network network = in.readParcelable(Network.class.getClassLoader());
            boolean includeTokens = Boolean.parseBoolean(in.readString());
            return new CryptoAddress(address, network, includeTokens);
        }

        @Override
        public CryptoAddress[] newArray(int size) {
            return new CryptoAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String address;
    public Network network;
    public boolean includeTokens;

    public CryptoAddress(String address, Network network, boolean includeTokens) {
        this.address = address;
        this.network = network;
        this.includeTokens = includeTokens;
    }

    public Coin getPrimaryCoin() {
        // Get the main coin that the network is known for.
        return network.getPrimaryCoin();
    }

    public Coin getFeeCoin() {
        // Get the main coin that the network is known for.
        return network.getFeeCoin();
    }

    public ArrayList<Coin> getCoins() {
        // Get all coins associated with the network.
        return network.getCoins();
    }

    public ArrayList<TokenManager> getTokenManagers() {
        return network.getTokenManagers();
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

    public boolean isSameAs(CryptoAddress cryptoAddress) {
        // Returns true if this CryptoAddress is effectively the same as the input CryptoAddress.
        // For example, we look at case insensitive matches when appropriate, and we don't take into account whether tokens are included or not.
        return network.equals(cryptoAddress.network) && matchesAddress(cryptoAddress.address);
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

    public String toSimpleString() {
        // Don't include the coins/tokens part.
        return "(" + network.getDisplayName() + ") " + address;
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("address", address, String.class)
                .serialize("network", network, Network.class)
                .serialize("includeTokens", includeTokens, Boolean.class)
                .endObject();
    }

    public static CryptoAddress deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        CryptoAddress cryptoAddress;

        if("2".equals(version)) {
            String address = o.deserialize("address", String.class);
            Network network = o.deserialize("network", Network.class);
            boolean includeTokens = o.deserialize("includeTokens", Boolean.class);
            o.endObject();

            cryptoAddress = new CryptoAddress(address, network, includeTokens);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return cryptoAddress;
    }
}
