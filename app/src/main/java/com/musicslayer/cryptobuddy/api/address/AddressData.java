package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

// TODO should fields store strings, or custom objects?
// TODO addressData from API should tell us the date that the data is from.

public class AddressData {
    final public CryptoAddress cryptoAddress;
    final public AddressAPI addressAPI_currentBalance;
    final public AddressAPI addressAPI_transactions;
    //final public String date;
    final public ArrayList<AssetQuantity> currentBalanceArrayList; //BD
    final public ArrayList<Transaction> transactionArrayList;

    public String serialize() {
        return "{\"cryptoAddress\":" + cryptoAddress.serialize() + ",\"addressAPI_currentBalance\":" + addressAPI_currentBalance.serialize() + ",\"addressAPI_transactions\":" + addressAPI_transactions.serialize() + ",\"currentBalanceArrayList\":" + AssetQuantity.serializeArray(currentBalanceArrayList) + ",\"transactionArrayList\":" + Transaction.serializeArray(transactionArrayList) + "}";
    }

    public static String serializeArray(ArrayList<AddressData> arrayList) {
        StringBuilder s = new StringBuilder();
        s.append("[");

        for(int i = 0; i < arrayList.size(); i++) {
            s.append(arrayList.get(i).serialize());

            if(i < arrayList.size() - 1) {
                s.append(",");
            }
        }

        s.append("]");
        return s.toString();
    }

    public static AddressData deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            CryptoAddress cryptoAddress = CryptoAddress.deserialize(o.getJSONObject("cryptoAddress").toString());
            AddressAPI addressAPI_currentBalance = (AddressAPI)API.deserialize(o.getJSONObject("addressAPI_currentBalance").toString());
            AddressAPI addressAPI_transactions = (AddressAPI)API.deserialize(o.getJSONObject("addressAPI_transactions").toString());
            ArrayList<AssetQuantity> currentBalanceArrayList = AssetQuantity.deserializeArray(o.getJSONArray("currentBalanceArrayList").toString());
            ArrayList<Transaction> transactionArrayList = Transaction.deserializeArray(o.getJSONArray("transactionArrayList").toString());
            return new AddressData(cryptoAddress, addressAPI_currentBalance, addressAPI_transactions, DateTime.toDateString(new Date()), currentBalanceArrayList, transactionArrayList);
        }
        catch(Exception e) {
            return null;
        }
    }

    public static ArrayList<AddressData> deserializeArray(String s) {
        try {
            ArrayList<AddressData> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                arrayList.add(AddressData.deserialize(o.toString()));
            }

            return arrayList;
        }
        catch(Exception e) {
            return null;
        }
    }

    public AddressData(CryptoAddress cryptoAddress, AddressAPI addressAPI_currentBalance, AddressAPI addressAPI_transactions, String date, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList) {
        this.cryptoAddress = cryptoAddress;
        this.addressAPI_currentBalance = addressAPI_currentBalance;
        this.addressAPI_transactions = addressAPI_transactions;
        //this.date = date;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;
    }

    public static AddressData getAddressData(CryptoAddress cryptoAddress) {
        AddressAPI addressAPI_currentBalance_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        AddressAPI addressAPI_transactions_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        for(AddressAPI addressAPI : AddressAPI.address_apis) {
            if(!addressAPI.isSupported(cryptoAddress)) {
                continue;
            }

            currentBalanceArrayList_f = addressAPI.getCurrentBalance(cryptoAddress);
            if(currentBalanceArrayList_f != null) {
                // Sort currentBalanceArrayList_f so that Coins come before Tokens.
                AssetQuantity.sortAscendingByType(currentBalanceArrayList_f);
                addressAPI_currentBalance_f = addressAPI;
                break;
            }
        }

        // Get transaction information.
        for(AddressAPI addressAPI : AddressAPI.address_apis) {
            if(!addressAPI.isSupported(cryptoAddress)) {
                continue;
            }

            transactionArrayList_f = addressAPI.getTransactions(cryptoAddress);
            if(transactionArrayList_f != null) {
                addressAPI_transactions_f = addressAPI;
                break;
            }
        }

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, DateTime.toDateString(new Date()), currentBalanceArrayList_f, transactionArrayList_f);
    }

    public boolean isComplete() {
        return !(addressAPI_currentBalance instanceof UnknownAddressAPI) && !(addressAPI_transactions instanceof UnknownAddressAPI) && currentBalanceArrayList != null && transactionArrayList != null;
    }

    public boolean alertUser() {
        // Show a toast if some information could not be found.
        // Return true if any toast was shown, or false if nothing had to be shown.
        if(isComplete()) {
            return false;
        }
        else {
            Toast.showToast("no_address_data");
            return true;
        }
    }

    public static void alertUser(ArrayList<AddressData> addressDataArrayList) {
        // Show a toast if some information could bot be found.
        for(AddressData addressData : addressDataArrayList) {
            if(addressData.alertUser()) {
                // Only alert once. Others would be redundant.
                break;
            }
        }
    }
}