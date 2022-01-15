package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.serialize.Serialization;

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
        ArrayList<CryptoAddress> cryptoAddressArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("cryptoAddressArrayList"), CryptoAddress.class);

        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);
        addressPortfolioObj.cryptoAddressArrayList = cryptoAddressArrayList;

        return addressPortfolioObj;
    }

    // TODO This should be removed soon.
    public static AddressPortfolioObj deserializeFromJSON0(String s) {
        String[] sArray = s.split("\n");

        String name = sArray[0];
        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);

        for(int i = 1; i < sArray.length; i++) {
            String[] cryptoAddressStringArray = sArray[i].split("\\|");
            CryptoAddress cryptoAddress = new CryptoAddress(cryptoAddressStringArray[0], Network.getNetworkFromKey(cryptoAddressStringArray[1]), Boolean.parseBoolean(cryptoAddressStringArray[2]));
            addressPortfolioObj.addData(cryptoAddress);
        }

        return addressPortfolioObj;
    }
}
