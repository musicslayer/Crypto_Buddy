package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

// TODO should fields store strings, or custom objects?
// TODO addressData from API should tell us the date that the data is from.

public class AddressData implements Serializable {
    final public CryptoAddress cryptoAddress;
    final public AddressAPI addressAPI_currentBalance;
    final public AddressAPI addressAPI_transactions;
    //final public String date;
    final public ArrayList<AssetQuantity> currentBalanceArrayList; //BD
    final public ArrayList<Transaction> transactionArrayList;

    public String serialize() {
        StringBuilder s = new StringBuilder();

        // cryptoAddress
        s.append(cryptoAddress.address).append("|")
            .append(cryptoAddress.network.getKey()).append("|")
            .append(cryptoAddress.includeTokens);

        // addressAPI_currentBalance
        if(addressAPI_currentBalance == null) {
            s.append("\n")
                .append("null");
        }
        else {
            s.append("\n")
                .append(addressAPI_currentBalance.getName());
        }

        // addressAPI_transactions
        if(addressAPI_transactions == null) {
            s.append("\n")
                .append("null");
        }
        else {
            s.append("\n")
                .append(addressAPI_transactions.getName());
        }

        //currentBalanceArrayList
        if(currentBalanceArrayList == null) {
            s.append("\n")
                .append("null");
        }
        else {
            s.append("\n").append(currentBalanceArrayList.size());
            for(AssetQuantity assetQuantity : currentBalanceArrayList) {
                String actionedAssetTokenTypeString = getTokenType(assetQuantity.asset);
                String actionedAssetKeyString = assetQuantity.asset.getKey();
                String actionedAssetAmountString = assetQuantity.assetAmount.amount.toString();

                s.append("\n")
                        .append(actionedAssetTokenTypeString).append("|")
                        .append(actionedAssetKeyString).append("|")
                        .append(actionedAssetAmountString);
            }
        }

        //transactionArrayList
        if(transactionArrayList == null) {
            s.append("\n")
                    .append("null");
        }
        else {
            s.append("\n").append(transactionArrayList.size());
            for(Transaction transaction : transactionArrayList) {
                String actionString = transaction.action.toString();
                String actionedAssetTokenTypeString = getTokenType(transaction.actionedAssetQuantity.asset);
                String actionedAssetKeyString = transaction.actionedAssetQuantity.asset.getKey();
                String actionedAssetAmountString = transaction.actionedAssetQuantity.assetAmount.amount.toString();

                String otherAssetTokenTypeString;
                String otherAssetKeyString;
                String otherAssetAmountString;
                if(transaction.otherAssetQuantity == null) {
                    otherAssetTokenTypeString = "";
                    otherAssetKeyString = "";
                    otherAssetAmountString = "";
                }
                else {
                    otherAssetTokenTypeString = getTokenType(transaction.otherAssetQuantity.asset);
                    otherAssetKeyString = transaction.otherAssetQuantity.asset.getKey();
                    otherAssetAmountString = transaction.otherAssetQuantity.assetAmount.amount.toString();
                }

                String timestampString = DateTime.serialize(transaction.timestamp.date);
                String infoString = transaction.info;

                s.append("\n")
                        .append(actionString).append("|")
                        .append(actionedAssetTokenTypeString).append("|")
                        .append(actionedAssetKeyString).append("|")
                        .append(actionedAssetAmountString).append("|")
                        .append(otherAssetTokenTypeString).append("|")
                        .append(otherAssetKeyString).append("|")
                        .append(otherAssetAmountString).append("|")
                        .append(timestampString).append("|")
                        .append(infoString).append("|").append("END_MARKER");
            }
        }

        return s.toString();
    }

    public static AddressData deserialize(String s) {
        String[] sArray = s.split("\n");

        String[] cryptoAddressStringArray = sArray[0].split("\\|");
        CryptoAddress cryptoAddress = new CryptoAddress(cryptoAddressStringArray[0], Network.getNetworkFromKey(cryptoAddressStringArray[1]), Boolean.parseBoolean(cryptoAddressStringArray[2]));

        AddressAPI addressAPI_currentBalance_f;
        if("null".equals(sArray[1])) {
            addressAPI_currentBalance_f = null;
        }
        else {
            addressAPI_currentBalance_f = AddressAPI.getAddressAPIFromName(sArray[1]);
        }

        AddressAPI addressAPI_transactions_f;
        if("null".equals(sArray[2])) {
            addressAPI_transactions_f = null;
        }
        else {
            addressAPI_transactions_f = AddressAPI.getAddressAPIFromName(sArray[2]);
        }

        ArrayList<AssetQuantity> currentBalanceArrayList_f;
        int x = 4;
        if("null".equals(sArray[3])) {
            currentBalanceArrayList_f = null;
        }
        else {
            currentBalanceArrayList_f = new ArrayList<>();
            int size_currentBalanceArrayList = Integer.parseInt(sArray[3]);
            for(int i = 0; i < size_currentBalanceArrayList; i++) {
                String[] currentBalanceStringArray = sArray[x].split("\\|");

                Asset asset = getAsset(currentBalanceStringArray[0], currentBalanceStringArray[1]);
                AssetQuantity assetQuantity = new AssetQuantity(new AssetAmount(currentBalanceStringArray[2]), asset);
                currentBalanceArrayList_f.add(assetQuantity);

                x++;
            }
        }

        ArrayList<Transaction> transactionArrayList_f;
        if("null".equals(sArray[x])) {
            transactionArrayList_f = null;
        }
        else {
            transactionArrayList_f = new ArrayList<>();
            int size_transactionArrayList = Integer.parseInt(sArray[x]);
            x++;
            for(int i = 0; i < size_transactionArrayList; i++) {
                String[] transactionStringArray = sArray[x].split("\\|");

                Action action = new Action(transactionStringArray[0]);

                Asset asset = getAsset(transactionStringArray[1], transactionStringArray[2]);
                AssetQuantity actionedAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[3]), asset);

                AssetQuantity otherAssetQuantity;
                if(transactionStringArray[4].isEmpty() && transactionStringArray[5].isEmpty()) {
                    otherAssetQuantity = null;
                }
                else {
                    Asset otherAsset = getAsset(transactionStringArray[4], transactionStringArray[5]);
                    otherAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[6]), otherAsset);
                }

                Timestamp timestamp = new Timestamp(DateTime.deserialize(transactionStringArray[7]));
                String info = transactionStringArray[8];

                transactionArrayList_f.add(new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info));

                x++;
            }
        }

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, DateTime.toDateString(new Date()), currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static String serializeArray(ArrayList<AddressData> addressDataArrayList) {
        StringBuilder s = new StringBuilder();
        s.append(addressDataArrayList.size());
        for(AddressData addressData : addressDataArrayList) {
            String sA = addressData.serialize();

            int numLines = 1;
            for (int pos = sA.indexOf("\n"); pos >= 0; pos = sA.indexOf("\n", pos + 1)) {
                numLines++;
            }

            s.append("\n").append(numLines).append("\n").append(sA);
        }

        return s.toString();
    }

    public static ArrayList<AddressData> deserializeArray(String s) {
        String[] sArray = s.split("\n");

        int size_addressDataArrayList = Integer.parseInt(sArray[0]);
        ArrayList<AddressData> addressDataArrayList = new ArrayList<>();
        int x = 1;
        for(int i = 0; i < size_addressDataArrayList; i++) {
            int numLines = Integer.parseInt(sArray[x]);
            StringBuilder sA = new StringBuilder();
            for(int j = 0; j < numLines; j++) {
                x++;
                sA.append(sArray[x]).append("\n");
            }

            addressDataArrayList.add(AddressData.deserialize(sA.toString()));
            x++;
        }
        return addressDataArrayList;
    }

    public static String getTokenType(Asset asset) {
        if(asset instanceof Fiat) {
            return "!FIAT!";
        }
        else if(asset instanceof Coin) {
            return "!COIN!";
        }
        else {
            return ((Token)asset).getTokenType();
        }
    }

    public static Asset getAsset(String tokenType, String key) {
        if("!FIAT!".equals(tokenType)) {
            return Fiat.getFiatFromKey(key);
        }
        else if("!COIN!".equals(tokenType)) {
            return Coin.getCoinFromKey(key);
        }
        else {
            return TokenManager.getTokenManagerFromTokenType(tokenType).getToken(key, null, null, 0, null);
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
        AddressAPI addressAPI_currentBalance_f = null;
        AddressAPI addressAPI_transactions_f = null;
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
        return addressAPI_currentBalance != null && addressAPI_transactions != null && currentBalanceArrayList != null && transactionArrayList != null;
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