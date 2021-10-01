package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;

import org.json.JSONObject;

import java.util.ArrayList;

public class AddressPortfolioObj {
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

    public String serialize() {
        return "{\"name\":\"" + name + "\",\"cryptoAddressArrayList\":" + CryptoAddress.serializeArray(cryptoAddressArrayList) + "}";
    }

    public static AddressPortfolioObj deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            String name = o.getString("name");
            AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);

            ArrayList<CryptoAddress> cryptoAddressArrayList = CryptoAddress.deserializeArray(o.getJSONArray("cryptoAddressArrayList").toString());
            for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
                addressPortfolioObj.addData(cryptoAddress);
            }

            return addressPortfolioObj;
        }
        catch(Exception e) {
            return null;
        }
    }
}
