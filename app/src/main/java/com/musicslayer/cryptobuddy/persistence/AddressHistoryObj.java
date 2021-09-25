package com.musicslayer.cryptobuddy.persistence;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.network.Network;

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
        return cryptoAddress.address + "\n" + cryptoAddress.network.getKey() + "\n" + cryptoAddress.includeTokens;
    }

    public static AddressHistoryObj deserialize(String s) {
        String[] cryptoAddressArray = s.split("\n");
        return new AddressHistoryObj(new CryptoAddress(cryptoAddressArray[0], Network.getNetworkFromKey(cryptoAddressArray[1]), Boolean.parseBoolean(cryptoAddressArray[2])));
    }
}
