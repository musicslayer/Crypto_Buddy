package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.serialize.Serialization;

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
            .put("cryptoAddress", new Serialization.JSONObjectWithNull(Serialization.serialize(cryptoAddress)))
            .toStringOrNull();
    }

    public static AddressHistoryObj deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        CryptoAddress cryptoAddress = Serialization.deserialize(o.getJSONObjectString("cryptoAddress"), CryptoAddress.class);
        return new AddressHistoryObj(cryptoAddress);
    }

    // TODO This should be removed soon.
    public static AddressHistoryObj deserializeFromJSON0(String s) {
        String[] cryptoAddressArray = s.split("\n");
        return new AddressHistoryObj(new CryptoAddress(cryptoAddressArray[0], Network.getNetworkFromKey(cryptoAddressArray[1]), Boolean.parseBoolean(cryptoAddressArray[2])));
    }
}
