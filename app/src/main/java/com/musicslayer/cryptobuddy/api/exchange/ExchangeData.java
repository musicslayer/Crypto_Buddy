package com.musicslayer.cryptobuddy.api.exchange;

import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetQuantityData;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.transaction.TransactionData;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class ExchangeData implements Serialization.SerializableToJSON {
    final public CryptoExchange cryptoExchange;
    final public ExchangeAPI exchangeAPI_currentBalance;
    final public ExchangeAPI exchangeAPI_transactions;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;
    final public Timestamp timestamp_currentBalance;
    final public Timestamp timestamp_transactions;

    final public AssetQuantityData currentBalanceData;
    final public TransactionData transactionData;
    final public AssetQuantityData discrepancyData;

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("cryptoExchange", cryptoExchange, CryptoExchange.class)
            .serialize("exchangeAPI_currentBalance", exchangeAPI_currentBalance, ExchangeAPI.class)
            .serialize("exchangeAPI_transactions", exchangeAPI_transactions, ExchangeAPI.class)
            .serializeArrayList("currentBalanceArrayList", currentBalanceArrayList, AssetQuantity.class)
            .serializeArrayList("transactionArrayList", transactionArrayList, Transaction.class)
            .serialize("timestamp_currentBalance", timestamp_currentBalance, Timestamp.class)
            .serialize("timestamp_transactions", timestamp_transactions, Timestamp.class)
            .toStringOrNull();
    }

    public static ExchangeData deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        CryptoExchange cryptoExchange = o.deserialize("cryptoExchange", CryptoExchange.class);
        ExchangeAPI exchangeAPI_currentBalance = o.deserialize("exchangeAPI_currentBalance", ExchangeAPI.class);
        ExchangeAPI exchangeAPI_transactions = o.deserialize("exchangeAPI_transactions", ExchangeAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = o.deserializeArrayList("currentBalanceArrayList", AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = o.deserializeArrayList("transactionArrayList", Transaction.class);
        Timestamp timestamp_currentBalance = o.deserialize("timestamp_currentBalance", Timestamp.class);
        Timestamp timestamp_transactions = o.deserialize("timestamp_transactions", Timestamp.class);
        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance, exchangeAPI_transactions, currentBalanceArrayList, transactionArrayList, timestamp_currentBalance, timestamp_transactions);
    }

    public ExchangeData(CryptoExchange CryptoExchange, ExchangeAPI exchangeAPI_currentBalance, ExchangeAPI exchangeAPI_transactions, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList, Timestamp timestamp_currentBalance, Timestamp timestamp_transactions) {
        this.cryptoExchange = CryptoExchange;
        this.exchangeAPI_currentBalance = exchangeAPI_currentBalance;
        this.exchangeAPI_transactions = exchangeAPI_transactions;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;
        this.timestamp_currentBalance = timestamp_currentBalance;
        this.timestamp_transactions = timestamp_transactions;

        currentBalanceData = new AssetQuantityData(currentBalanceArrayList);
        transactionData = new TransactionData(transactionArrayList);
        discrepancyData = new AssetQuantityData(getDiscrepancyMap());
    }

    public static ExchangeData getAllData(CryptoExchange cryptoExchange) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        ExchangeAPI exchangeAPI = cryptoExchange.exchangeAPI;
        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            currentBalanceArrayList_f = exchangeAPI.getCurrentBalance(cryptoExchange);
            if(currentBalanceArrayList_f != null) {
                // Sort currentBalanceArrayList_f so that Coins come before Tokens.
                AssetQuantity.sortAscendingByType(currentBalanceArrayList_f);
                exchangeAPI_currentBalance_f = exchangeAPI;
            }

            // Get transaction information.
            transactionArrayList_f = exchangeAPI.getTransactions(cryptoExchange);
            if(transactionArrayList_f != null) {
                exchangeAPI_transactions_f = exchangeAPI;
            }
        }

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ExchangeData getCurrentBalanceData(CryptoExchange cryptoExchange) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        ExchangeAPI exchangeAPI = cryptoExchange.exchangeAPI;
        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            currentBalanceArrayList_f = exchangeAPI.getCurrentBalance(cryptoExchange);
            if(currentBalanceArrayList_f != null) {
                // Sort currentBalanceArrayList_f so that Coins come before Tokens.
                AssetQuantity.sortAscendingByType(currentBalanceArrayList_f);
                exchangeAPI_currentBalance_f = exchangeAPI;
            }
        }

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ExchangeData getTransactionsData(CryptoExchange cryptoExchange) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get transaction information.
        ExchangeAPI exchangeAPI = cryptoExchange.exchangeAPI;
        if(exchangeAPI != null && exchangeAPI.isAuthorized()) {
            transactionArrayList_f = exchangeAPI.getTransactions(cryptoExchange);
            if(transactionArrayList_f != null) {
                exchangeAPI_transactions_f = exchangeAPI;
            }
        }

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ExchangeData getNoData(CryptoExchange cryptoExchange) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public boolean isComplete() {
        return isCurrentBalanceComplete() && isTransactionsComplete();
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
        Timestamp timestamp_currentBalance_f = oldExchangeData.timestamp_currentBalance;
        Timestamp timestamp_transactions_f = oldExchangeData.timestamp_transactions;

        if(newExchangeData.isCurrentBalanceComplete()) {
            exchangeAPI_currentBalance_f = newExchangeData.exchangeAPI_currentBalance;
            currentBalanceArrayList_f = newExchangeData.currentBalanceArrayList;
            timestamp_currentBalance_f = newExchangeData.timestamp_currentBalance;
        }

        if(newExchangeData.isTransactionsComplete()) {
            exchangeAPI_transactions_f = newExchangeData.exchangeAPI_transactions;
            transactionArrayList_f = newExchangeData.transactionArrayList;
            timestamp_transactions_f = newExchangeData.timestamp_transactions;
        }

        // Both ExchangeData objects should have the same cryptoExchange, but just in case we favor the newer one for consistency.
        return new ExchangeData(newExchangeData.cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, timestamp_currentBalance_f, timestamp_transactions_f);
    }

    public String getInfoString(PriceData priceData, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Exchange = " + cryptoExchange.exchange.toString());

        if(exchangeAPI_transactions == null || transactionArrayList == null) {
            s.appendRich("\n(Transaction information not present.)");
        }
        else {
            s.appendRich("\nTransaction Data Source = ").appendRich(exchangeAPI_transactions.getDisplayName());
            s.appendRich("\nTransaction Data Timestamp = ").appendRich(timestamp_transactions.toString());
            s.appendRich("\nNumber of Transactions = ").appendRich(Integer.toString(transactionArrayList.size()));
        }

        if(exchangeAPI_currentBalance == null || currentBalanceArrayList == null) {
            s.appendRich("\n(Current balance information not present.)");
        }
        else {
            s.appendRich("\nCurrent Balance Data Source = ").appendRich(exchangeAPI_currentBalance.getDisplayName());
            s.appendRich("\nCurrent Balance Data Timestamp = ").appendRich(timestamp_currentBalance.toString());

            if(currentBalanceArrayList.isEmpty()) {
                s.appendRich("\nNo Current Balances");
            }
            else {
                s.appendRich("\nCurrent Balances:");

                HashMap<Asset, AssetQuantity> priceMap = priceData == null ? null : priceData.priceHashMap;
                s.append(currentBalanceData.getAssetQuantityInfo(priceMap, isRich));

                if(priceData != null) {
                    s.appendRich("\n\nPrice Data Source = ").appendRich(priceData.priceAPI_price.getDisplayName());
                    s.appendRich("\nPrice Data Timestamp = ").appendRich(priceData.timestamp_price.toString());
                }
            }
        }

        return s.toString();
    }

    public String getRawFullInfoString() {
        // Get regular info and also the complete set of transactions and net transaction sums, and authorization info.
        StringBuilder s = new StringBuilder(getInfoString(null, false));

        if(exchangeAPI_transactions != null && transactionArrayList != null) {
            s.append("\n").append(transactionData.getAllTransactionInfo(null, false));
        }

        // Authorization
        s.append("\n\n").append("Exchange Authorization:");
        s.append("\n").append(cryptoExchange.getInfo());

        return s.toString();
    }

    public static String getRawFullInfoString(ArrayList<ExchangeData> exchangeDataArrayList) {
        if(exchangeDataArrayList == null) { return null; }

        StringBuilder s = new StringBuilder();
        for(int i = 0; i < exchangeDataArrayList.size(); i++) {
            ExchangeData exchangeData = exchangeDataArrayList.get(i);
            s.append(exchangeData.getRawFullInfoString());

            if(i < exchangeDataArrayList.size() - 1) {
                s.append("\n\n");
            }
        }

        return s.toString();
    }

    public String getDiscrepancyString(PriceData priceData, boolean isRich) {
        // Get discrepancy information. If the priceMap is not null, add in the prices of each asset in the map.
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Exchange = ").appendRich(cryptoExchange.toString()).appendRich("\n");

        if(!hasDiscrepancy()) {
            s.appendRich("\nThis exchange has no discrepancies.");
        }
        else {
            s.appendRich("\nDiscrepancies:");

            HashMap<Asset, AssetQuantity> priceHashMap = priceData == null ? null : priceData.priceHashMap;
            s.append(discrepancyData.getAssetQuantityInfo(priceHashMap, isRich));

            if(priceData != null) {
                s.appendRich("\n\nPrice Data Source = ").appendRich(priceData.priceAPI_price.getDisplayName());
                s.appendRich("\nPrice Data Timestamp = ").appendRich(priceData.timestamp_price.toString());
            }
        }

        return s.toString();
    }

    public HashMap<Asset, AssetAmount> getDiscrepancyMap() {
        // Create the discrepancy map. Add net transactions and subtract balances.
        // Note that all AssetAmounts have the correct signed value, so we don't need to check "isLoss".
        HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();

        if(transactionArrayList != null && currentBalanceArrayList != null) {
            for(Asset asset : transactionData.netTransactionsMap.keySet()) {
                AssetAmount assetAmount = transactionData.netTransactionsMap.get(asset);
                add(deltaMap, asset, assetAmount);
            }

            for(AssetQuantity assetQuantity : currentBalanceArrayList) {
                subtract(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
            }

            // If an amount is zero, we do not count that as a discrepancy, so let's remove it.
            // This also means that if an asset appears in one place with an amount of zero, and is absent from another place, it does not count as a discrepancy.
            for(Asset asset : new ArrayList<>(deltaMap.keySet())) {
                AssetAmount assetAmount = HashMapUtil.getValueFromMap(deltaMap, asset);
                if(assetAmount.amount.compareTo(BigDecimal.ZERO) == 0) {
                    HashMapUtil.removeValueFromMap(deltaMap, asset);
                }
            }
        }

        return deltaMap;
    }

    public boolean hasDiscrepancy() {
        // Return true if there is at least one discrepancy.
        return !discrepancyData.deltaMap.isEmpty();
    }

    public static boolean hasDiscrepancy(ArrayList<ExchangeData> exchangeDataArrayList) {
        for(ExchangeData exchangeData : exchangeDataArrayList) {
            if(exchangeData.hasDiscrepancy()) {
                return true;
            }
        }

        return false;
    }

    private static void add(HashMap<Asset, AssetAmount> map, Asset asset, AssetAmount assetAmount) {
        AssetAmount oldValue = map.get(asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.add(assetAmount);
        map.put(asset, newValue);
    }

    private static void subtract(HashMap<Asset, AssetAmount> map, Asset asset, AssetAmount assetAmount) {
        AssetAmount oldValue = map.get(asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.subtract(assetAmount);
        map.put(asset, newValue);
    }

    public String getProblem() {
        // Currently no exchanges have any problems.
        return null;
    }

    public boolean hasProblem() {
        return getProblem() != null;
    }

    public static boolean hasProblem(ArrayList<ExchangeData> exchangeDataArrayList) {
        // Return true if there is any info to show for any crypto.
        for(ExchangeData exchangeData : exchangeDataArrayList) {
            if(exchangeData.hasProblem()) {
                return true;
            }
        }

        return false;
    }
}