package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;

import org.json.JSONObject;

public class AddressHistoryObj {
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

    public String serialize() {
        return "{\"cryptoAddress\":" + cryptoAddress.serialize() + "}";
    }

    public static AddressHistoryObj deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            CryptoAddress cryptoAddress = CryptoAddress.deserialize(o.getJSONObject("cryptoAddress").toString());
            return new AddressHistoryObj(cryptoAddress);
        }
        catch(Exception e) {
            return null;
        }
    }
}
