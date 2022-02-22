package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;

import java.io.IOException;
import java.util.ArrayList;

public class AddressPortfolioObj implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
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

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws org.json.JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("name", name, String.class)
            .serializeArrayList("cryptoAddressArrayList", cryptoAddressArrayList, CryptoAddress.class)
            .toStringOrNull();
    }

    public static AddressPortfolioObj legacy_deserializeFromJSON(String s, String version) throws org.json.JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        String name = o.deserialize("name", String.class);
        ArrayList<CryptoAddress> cryptoAddressArrayList = o.deserializeArrayList("cryptoAddressArrayList", CryptoAddress.class);

        AddressPortfolioObj addressPortfolioObj = new AddressPortfolioObj(name);
        addressPortfolioObj.cryptoAddressArrayList = cryptoAddressArrayList;

        return addressPortfolioObj;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("name", name, String.class)
                .serializeArrayList("cryptoAddressArrayList", cryptoAddressArrayList, CryptoAddress.class)
                .endObject();
    }

    public static AddressPortfolioObj deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        AddressPortfolioObj addressPortfolioObj;

        if("2".equals(version)) {
            String name = o.deserialize("name", String.class);
            ArrayList<CryptoAddress> cryptoAddressArrayList = o.deserializeArrayList("cryptoAddressArrayList", CryptoAddress.class);
            o.endObject();

            addressPortfolioObj = new AddressPortfolioObj(name);
            addressPortfolioObj.cryptoAddressArrayList = cryptoAddressArrayList;
        }
        else {
            throw new IllegalStateException();
        }

        return addressPortfolioObj;
    }
}
