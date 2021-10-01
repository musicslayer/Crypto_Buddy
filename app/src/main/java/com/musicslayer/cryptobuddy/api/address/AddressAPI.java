package com.musicslayer.cryptobuddy.api.address;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Reflect;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

// TODO Unconfirmed transactions should only appear if they actually affect balance???
// TODO Which network addresses are case sensitive/insensitive?

abstract public class AddressAPI extends API {
    public static ArrayList<AddressAPI> address_apis;
    public static HashMap<String, AddressAPI> address_api_map;
    public static ArrayList<String> address_api_names;
    public static ArrayList<String> address_api_display_names;

    public static void initialize(Context context) {
        address_api_names = File.readFileIntoLines(context, R.raw.api_address);

        address_apis = new ArrayList<>();
        address_api_map = new HashMap<>();
        address_api_display_names = new ArrayList<>();

        for(String addressName : address_api_names) {
            AddressAPI addressAPI = Reflect.constructSubclassInstanceFromName("com.musicslayer.cryptobuddy.api.address." + addressName);
            address_apis.add(addressAPI);
            address_api_map.put(addressName, addressAPI);
            address_api_display_names.add(addressAPI.getDisplayName());
        }
    }

    abstract public boolean isSupported(CryptoAddress cryptoAddress);
    abstract public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress);
    abstract public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress);

    public static AddressAPI getAddressAPIFromKey(String key) {
        return address_api_map.get(key);
    }

    public boolean shouldIncludeTokens(CryptoAddress cryptoAddress) {
        // Exclude tokens if person did not purchase "Unlock Tokens", or if they chose not to analyze them.
        return Purchases.isUnlockTokensPurchased && cryptoAddress.includeTokens;
    }

    public static int getMaxTransactions() {
        return Settings.setting_max_transactions;
    }

    public String serialize() {
        return "{\"key\":\"" + getKey() + "\"}";
    }

    public static AddressAPI deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            String key = o.getString("key");
            return AddressAPI.getAddressAPIFromKey(key);
        }
        catch(Exception e) {
            return null;
        }
    }
}
