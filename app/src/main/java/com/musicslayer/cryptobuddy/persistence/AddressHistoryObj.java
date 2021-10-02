package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.util.Serialization;

public class AddressHistoryObj implements Serialization.SerializableToJSON {
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
        return new Serialization.JSONObjectWithNull()
            .put("cryptoAddress", Serialization.serialize(cryptoAddress))
            .toStringOrNull();
    }

    public static AddressHistoryObj deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        CryptoAddress cryptoAddress = Serialization.deserialize(o.getJSONObject("cryptoAddress").toStringOrNull(), CryptoAddress.class);
        return new AddressHistoryObj(cryptoAddress);
    }
}
