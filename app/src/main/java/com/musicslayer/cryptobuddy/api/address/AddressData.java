package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

// TODO addressData from API should tell us the date that the data is from.

public class AddressData implements Serialization.SerializableToJSON {
    final public CryptoAddress cryptoAddress;
    final public AddressAPI addressAPI_currentBalance;
    final public AddressAPI addressAPI_transactions;
    //final public String date;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("cryptoAddress", new Serialization.JSONObjectWithNull(Serialization.serialize(cryptoAddress)))
            .put("addressAPI_currentBalance", new Serialization.JSONObjectWithNull(Serialization.serialize(addressAPI_currentBalance)))
            .put("addressAPI_transactions", new Serialization.JSONObjectWithNull(Serialization.serialize(addressAPI_transactions)))
            .put("currentBalanceArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(currentBalanceArrayList)))
            .put("transactionArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(transactionArrayList)))
            .toStringOrNull();
    }

    public static AddressData deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        CryptoAddress cryptoAddress = Serialization.deserialize(o.getJSONObject("cryptoAddress").toStringOrNull(), CryptoAddress.class);
        AddressAPI addressAPI_currentBalance = Serialization.deserialize(o.getJSONObject("addressAPI_currentBalance").toStringOrNull(), AddressAPI.class);
        AddressAPI addressAPI_transactions = Serialization.deserialize(o.getJSONObject("addressAPI_transactions").toStringOrNull(), AddressAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = Serialization.deserializeArrayList(o.getJSONArray("currentBalanceArrayList").toStringOrNull(), AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArray("transactionArrayList").toStringOrNull(), Transaction.class);
        return new AddressData(cryptoAddress, addressAPI_currentBalance, addressAPI_transactions, DateTime.toDateString(new Date()), currentBalanceArrayList, transactionArrayList);
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