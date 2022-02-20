package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Serialization;

public class AddressHistoryObj implements Serialization.SerializableToJSON, Serialization.Versionable {
    public CryptoAddress cryptoAddress;

    public AddressHistoryObj(CryptoAddress cryptoAddress) {
        this.cryptoAddress = cryptoAddress;
    }

    @NonNull
    @Override
    public String toString() {
        return cryptoAddress.toString();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof AddressHistoryObj) && cryptoAddress.equals(((AddressHistoryObj) other).cryptoAddress);
    }

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("cryptoAddress", cryptoAddress, CryptoAddress.class)
            .toStringOrNull();
    }

    public static AddressHistoryObj deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        CryptoAddress cryptoAddress = o.deserialize("cryptoAddress", CryptoAddress.class);
        return new AddressHistoryObj(cryptoAddress);
    }
}
