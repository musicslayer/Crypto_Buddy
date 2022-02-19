package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

public class AddressPortfolioObj implements Serialization.SerializableToJSON, Serialization.Versionable {
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

    public void removeData(CryptoAddress cryptoAddress) {
        cryptoAddressArrayList.remove(cryptoAddress);
    }

    public boolean isSaved(CryptoAddress cryptoAddress) {
        // An address is considered to be saved in a portfolio if it is present in any form (i.e. coins, coins + tokens, case insensitive match if applicable).
        for(CryptoAddress ca : cryptoAddressArrayList) {
            if(ca.isSameAs(cryptoAddress)) {
                return true;
            }
        }
        return false;
    }

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new JSONWithNull.JSONObjectWithNull()
            .put("name", name, String.class)
            .putArrayList("cryptoAddressArrayList", cryptoAddressArrayList, CryptoAddress.class)
            .toStringOrNull();
    }

    public static AddressPortfolioObj deserializeFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        String name = o.get("name", String.class);
        ArrayList<CryptoAddress> cryptoAddressArrayList = o.getArrayList("cryptoAddressArrayList", CryptoAddress.class);

        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);
        addressPortfolioObj.cryptoAddressArrayList = cryptoAddressArrayList;

        return addressPortfolioObj;
    }
}
