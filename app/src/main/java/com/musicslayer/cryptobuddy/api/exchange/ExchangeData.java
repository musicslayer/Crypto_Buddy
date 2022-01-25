package com.musicslayer.cryptobuddy.api.exchange;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;

public class ExchangeData implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(exchange, flags);
        out.writeString(exchangeAPI_currentBalance.getKey());
        out.writeString(exchangeAPI_transactions.getKey());
        out.writeTypedList(currentBalanceArrayList);
        out.writeTypedList(transactionArrayList);
    }

    public static final Creator<ExchangeData> CREATOR = new Creator<ExchangeData>() {
        @Override
        public ExchangeData createFromParcel(Parcel in) {
            Exchange exchange = in.readParcelable(Exchange.class.getClassLoader());
            ExchangeAPI exchangeAPI_currentBalance_f = ExchangeAPI.getExchangeAPIFromKey(in.readString());
            ExchangeAPI exchangeAPI_transactions_f = ExchangeAPI.getExchangeAPIFromKey(in.readString());
            ArrayList<AssetQuantity> currentBalanceArrayList_f = in.createTypedArrayList(AssetQuantity.CREATOR);
            ArrayList<Transaction> transactionArrayList_f = in.createTypedArrayList(Transaction.CREATOR);

            return new ExchangeData(exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
        }

        @Override
        public ExchangeData[] newArray(int size) {
            return new ExchangeData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    final public Exchange exchange;
    final public ExchangeAPI exchangeAPI_currentBalance;
    final public ExchangeAPI exchangeAPI_transactions;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("exchange", new Serialization.JSONObjectWithNull(Serialization.serialize(exchange)))
            .put("exchangeAPI_currentBalance", new Serialization.JSONObjectWithNull(Serialization.serialize(exchangeAPI_currentBalance)))
            .put("exchangeAPI_transactions", new Serialization.JSONObjectWithNull(Serialization.serialize(exchangeAPI_transactions)))
            .put("currentBalanceArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(currentBalanceArrayList)))
            .put("transactionArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(transactionArrayList)))
            .toStringOrNull();
    }

    public static ExchangeData deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        Exchange exchange = Serialization.deserialize(o.getJSONObjectString("exchange"), Exchange.class);
        ExchangeAPI exchangeAPI_currentBalance = Serialization.deserialize(o.getJSONObjectString("exchangeAPI_currentBalance"), ExchangeAPI.class);
        ExchangeAPI exchangeAPI_transactions = Serialization.deserialize(o.getJSONObjectString("exchangeAPI_transactions"), ExchangeAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("currentBalanceArrayList"), AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("transactionArrayList"), Transaction.class);
        return new ExchangeData(exchange, exchangeAPI_currentBalance, exchangeAPI_transactions, currentBalanceArrayList, transactionArrayList);
    }

    public ExchangeData(Exchange exchange, ExchangeAPI exchangeAPI_currentBalance, ExchangeAPI exchangeAPI_transactions, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList) {
        this.exchange = exchange;
        this.exchangeAPI_currentBalance = exchangeAPI_currentBalance;
        this.exchangeAPI_transactions = exchangeAPI_transactions;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;
    }

    public static ExchangeAPI getExchangeAPI(Exchange exchange) {
        // Figures out which API can provide an authentication token to later query data from.
        // Unlike other APIs, once an ExchangeAPI is chosen, data must come from that same api later, since other APIs will not use the same token.
        ExchangeAPI exchangeAPI_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);

        for(ExchangeAPI exchangeAPI : ExchangeAPI.exchange_apis) {
            if(!exchangeAPI.isSupported(exchange)) {
                continue;
            }

            exchangeAPI_f = exchangeAPI;
            break;
        }

        return exchangeAPI_f;
    }

    public static ExchangeData getAllData(Exchange exchange, ExchangeAPI exchangeAPI) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            currentBalanceArrayList_f = exchangeAPI.getCurrentBalance(exchange);
            if(currentBalanceArrayList_f != null) {
                // Sort currentBalanceArrayList_f so that Coins come before Tokens.
                AssetQuantity.sortAscendingByType(currentBalanceArrayList_f);
                exchangeAPI_currentBalance_f = exchangeAPI;
            }

            // Get transaction information.
            transactionArrayList_f = exchangeAPI.getTransactions(exchange);
            if(transactionArrayList_f != null) {
                exchangeAPI_transactions_f = exchangeAPI;
            }
        }

        return new ExchangeData(exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static ExchangeData getCurrentBalanceData(Exchange exchange, ExchangeAPI exchangeAPI) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            currentBalanceArrayList_f = exchangeAPI.getCurrentBalance(exchange);
            if(currentBalanceArrayList_f != null) {
                // Sort currentBalanceArrayList_f so that Coins come before Tokens.
                AssetQuantity.sortAscendingByType(currentBalanceArrayList_f);
                exchangeAPI_currentBalance_f = exchangeAPI;
            }
        }

        return new ExchangeData(exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static ExchangeData getTransactionsData(Exchange exchange, ExchangeAPI exchangeAPI) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            transactionArrayList_f = exchangeAPI.getTransactions(exchange);
            if(transactionArrayList_f != null) {
                exchangeAPI_transactions_f = exchangeAPI;
            }
        }

        return new ExchangeData(exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static ExchangeData getNoData(Exchange exchange, ExchangeAPI exchangeAPI) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        return new ExchangeData(exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public boolean isComplete() {
        return !(exchangeAPI_currentBalance instanceof UnknownExchangeAPI) && !(exchangeAPI_transactions instanceof UnknownExchangeAPI) && currentBalanceArrayList != null && transactionArrayList != null;
    }

    public boolean isCurrentBalanceComplete() {
        return !(exchangeAPI_currentBalance instanceof UnknownExchangeAPI) && currentBalanceArrayList != null;
    }

    public boolean isTransactionsComplete() {
        return !(exchangeAPI_transactions instanceof UnknownExchangeAPI) && transactionArrayList != null;
    }

    public static ExchangeData merge(ExchangeData oldExchangeData, ExchangeData newExchangeData) {
        ExchangeAPI exchangeAPI_currentBalance_f = oldExchangeData.exchangeAPI_currentBalance;
        ExchangeAPI exchangeAPI_transactions_f = oldExchangeData.exchangeAPI_transactions;
        ArrayList<AssetQuantity> currentBalanceArrayList_f = oldExchangeData.currentBalanceArrayList;
        ArrayList<Transaction> transactionArrayList_f = oldExchangeData.transactionArrayList;

        if(newExchangeData.isCurrentBalanceComplete()) {
            exchangeAPI_currentBalance_f = newExchangeData.exchangeAPI_currentBalance;
            currentBalanceArrayList_f = newExchangeData.currentBalanceArrayList;
        }

        if(newExchangeData.isTransactionsComplete()) {
            exchangeAPI_transactions_f = newExchangeData.exchangeAPI_transactions;
            transactionArrayList_f = newExchangeData.transactionArrayList;
        }

        // Both ExchangeData objects should have the same exchange, but just in case we favor the newer one for consistency.
        return new ExchangeData(newExchangeData.exchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public String getInfoString() {
        StringBuilder s = new StringBuilder("Exchange = " + exchange.toString());

        if(exchangeAPI_transactions == null || transactionArrayList == null) {
            s.append("\n(Transaction information not present.)");
        }
        else {
            s.append("\nTransaction Data Source = ").append(exchangeAPI_transactions.getDisplayName());
            s.append("\nNumber of Transactions = ").append(transactionArrayList.size());
        }

        if(exchangeAPI_currentBalance == null || currentBalanceArrayList == null) {
            s.append("\n(Current balance information not present.)");
        }
        else {
            s.append("\nCurrent Balance Data Source = ").append(exchangeAPI_currentBalance.getDisplayName());

            if(currentBalanceArrayList.isEmpty()) {
                s.append("\nNo Current Balances");
            }
            else {
                s.append("\nCurrent Balances:");
                for(AssetQuantity a : currentBalanceArrayList) {
                    s.append("\n    ").append(a.toString());
                }
            }
        }

        return s.toString();
    }
}