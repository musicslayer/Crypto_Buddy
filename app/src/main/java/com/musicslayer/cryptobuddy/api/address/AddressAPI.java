package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.MaxNumberTransactionsSetting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class AddressAPI extends API {
    final public static String DONE = "!DONE!";
    final public static String NOTDONE = "!NOTDONE!";
    final public static String ERROR = "!ERROR!";

    public static ArrayList<AddressAPI> address_apis;
    public static HashMap<String, AddressAPI> address_api_map;
    public static ArrayList<String> address_api_names;
    public static ArrayList<String> address_api_display_names;

    public static void initialize() {
        address_api_names = FileUtil.readFileIntoLines(R.raw.api_address);

        address_apis = new ArrayList<>();
        address_api_map = new HashMap<>();
        address_api_display_names = new ArrayList<>();

        for(String addressName : address_api_names) {
            AddressAPI addressAPI = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.api.address." + addressName);
            address_apis.add(addressAPI);
            address_api_map.put(addressName, addressAPI);
            address_api_display_names.add(addressAPI.getDisplayName());
        }
    }

    abstract public boolean isSupported(CryptoAddress cryptoAddress);
    abstract public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress);
    abstract public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress);

    // Most APIs don't support getting single data, so by default just get all data and filter it for the crypto we want.
    // This may be inefficient because we have to process data involving cryptos we don't care about.
    // Subclasses can override these methods with specific APIs to only process the specific crypto.
    public ArrayList<AssetQuantity> getSingleCurrentBalance(CryptoAddress cryptoAddress, Crypto crypto) {
        // Get all balances and just filter for the one we want.
        ArrayList<AssetQuantity> currentBalanceArrayList = getCurrentBalance(cryptoAddress);
        ArrayList<AssetQuantity> singleCurrentBalanceArrayList = new ArrayList<>();
        for(AssetQuantity currentBalance : currentBalanceArrayList) {
            if(crypto.equals(currentBalance.asset)) {
                singleCurrentBalanceArrayList.add(currentBalance);
            }
        }
        return singleCurrentBalanceArrayList;
    }

    public ArrayList<Transaction> getSingleTransactions(CryptoAddress cryptoAddress, Crypto crypto) {
        // Get all transactions and just filter for the one we want.
        ArrayList<Transaction> transactionArrayList = getTransactions(cryptoAddress);
        ArrayList<Transaction> singleTransactionArrayList = new ArrayList<>();
        for(Transaction transaction : transactionArrayList) {
            // Only the actioned AssetQuantity is non-null.
            if(crypto.equals(transaction.actionedAssetQuantity.asset)) {
                singleTransactionArrayList.add(transaction);
            }
        }
        return singleTransactionArrayList;
    }

    public static AddressAPI getAddressAPIFromKey(String key) {
        AddressAPI addressAPI = address_api_map.get(key);
        if(addressAPI == null) {
            addressAPI = UnknownAddressAPI.createUnknownAddressAPI(key);
        }

        return addressAPI;
    }

    public boolean shouldIncludeTokens(CryptoAddress cryptoAddress) {
        // Exclude tokens if the user did not purchase "Unlock Tokens", or if they chose not to analyze them.
        return Purchases.isUnlockTokensPurchased() && cryptoAddress.includeTokens;
    }

    public static int getMaxTransactions() {
        return MaxNumberTransactionsSetting.value;
    }

    public String getAPIType() {
        return "!ADDRESSAPI!";
    }
}
