package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.network.Network;

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
        StringBuilder s = new StringBuilder();
        s.append(name);

        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            s.append("\n")
                .append(cryptoAddress.address).append("|")
                .append(cryptoAddress.network.getKey()).append("|")
                .append(cryptoAddress.includeTokens);
        }

        return s.toString();
    }

    public static AddressPortfolioObj deserialize(String s) {
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
