package com.musicslayer.cryptobuddy.data.persistent.user;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;

import org.json.JSONException;

import java.io.IOException;

public class AddressHistoryObj implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
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

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("cryptoAddress", cryptoAddress, CryptoAddress.class)
            .toStringOrNull();
    }

    public static AddressHistoryObj legacy_deserializeFromJSON(String s, String version) throws JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        CryptoAddress cryptoAddress = o.deserialize("cryptoAddress", CryptoAddress.class);
        return new AddressHistoryObj(cryptoAddress);
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("cryptoAddress", cryptoAddress, CryptoAddress.class)
                .endObject();
    }

    public static AddressHistoryObj deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        AddressHistoryObj addressHistoryObj;

        if("2".equals(version)) {
            CryptoAddress cryptoAddress = o.deserialize("cryptoAddress", CryptoAddress.class);
            o.endObject();

            addressHistoryObj = new AddressHistoryObj(cryptoAddress);
        }
        else {
            throw new IllegalStateException();
        }

        return addressHistoryObj;
    }
}
