package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
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

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new JSONWithNull.JSONObjectWithNull()
            .put("cryptoAddress", new JSONWithNull.JSONObjectWithNull(Serialization.serialize(cryptoAddress)))
            .toStringOrNull();
    }

    public static AddressHistoryObj deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        CryptoAddress cryptoAddress = Serialization.deserialize(o.getJSONObjectString("cryptoAddress"), CryptoAddress.class);
        return new AddressHistoryObj(cryptoAddress);
    }
}
