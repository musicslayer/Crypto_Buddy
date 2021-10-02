package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddressPortfolioObj implements Serialization.SerializableToJSON {
    public String name;
    public ArrayList<CryptoAddress> cryptoAddressArrayList = new ArrayList<>();

    public AddressPortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof AddressPortfolioObj) && name.equals(((AddressPortfolioObj)other).name);
    }

    public void addData(CryptoAddress cryptoAddress) {
        cryptoAddressArrayList.add(cryptoAddress);
    }

    public boolean isSaved(CryptoAddress cryptoAddress) {
        return cryptoAddressArrayList.contains(cryptoAddress);
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("name", Serialization.string_serialize(name))
            .put("cryptoAddressArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(cryptoAddressArrayList)))
            .toStringOrNull();
    }

    public static AddressPortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String name = Serialization.string_deserialize(o.getString("name"));
        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);

        ArrayList<CryptoAddress> cryptoAddressArrayList = Serialization.deserializeArrayList(o.getJSONArray("cryptoAddressArrayList").toStringOrNull(), CryptoAddress.class);
        if(cryptoAddressArrayList != null) {
            for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                addressPortfolioObj.addData(cryptoAddress);
            }
        }

        return addressPortfolioObj;
    }
}
