package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;

public class UnknownAddressAPI extends AddressAPI {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_ADDRESS_API?";
        }
        else {
            return "?UNKNOWN_ADDRESS_API (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Address API?";
        }
        else {
            return "?Unknown Address API (" + key + ")?";
        }
    }

    public boolean isSupported(CryptoAddress cryptoAddress) { return false; }
    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) { return null; }
    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) { return null; }

    public static UnknownAddressAPI createUnknownAddressAPI(String key) {
        return new UnknownAddressAPI(key);
    }

    private UnknownAddressAPI(String key) {
        this.key = key;
    }
}
