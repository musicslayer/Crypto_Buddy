package com.musicslayer.cryptobuddy.data.persistent.user;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;

public class AddressHistoryObj implements DataBridge.SerializableToJSON {
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
            throw new IllegalStateException("version = " + version);
        }

        return addressHistoryObj;
    }
}
