package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.util.Serialization;

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

    public String serializeToJSON() {
        return "{\"name\":\"" + name + "\",\"cryptoAddressArrayList\":" + Serialization.serializeArrayList(cryptoAddressArrayList) + "}";
    }

    public static AddressPortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        String name = o.getString("name");
        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);

        ArrayList<CryptoAddress> cryptoAddressArrayList = CryptoAddress.deserializeArray(o.getJSONArray("cryptoAddressArrayList").toString());
        if(cryptoAddressArrayList != null) {
            for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                addressPortfolioObj.addData(cryptoAddress);
            }
        }

        return addressPortfolioObj;
    }
}
