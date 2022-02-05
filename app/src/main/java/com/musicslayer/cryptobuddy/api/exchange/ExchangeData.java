package com.musicslayer.cryptobuddy.api.exchange;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ExchangeData implements Serialization.SerializableToJSON {
    final public CryptoExchange cryptoExchange;
    final public ExchangeAPI exchangeAPI_currentBalance;
    final public ExchangeAPI exchangeAPI_transactions;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;

    HashMap<Asset, AssetAmount> netTransactionsMap;
    final public Date timestamp;

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("cryptoExchange", new Serialization.JSONObjectWithNull(Serialization.serialize(cryptoExchange)))
            .put("exchangeAPI_currentBalance", new Serialization.JSONObjectWithNull(Serialization.serialize(exchangeAPI_currentBalance)))
            .put("exchangeAPI_transactions", new Serialization.JSONObjectWithNull(Serialization.serialize(exchangeAPI_transactions)))
            .put("currentBalanceArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(currentBalanceArrayList)))
            .put("transactionArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(transactionArrayList)))
            .toStringOrNull();
    }

    public static ExchangeData deserializeFromJSON(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        CryptoExchange cryptoExchange = Serialization.deserialize(o.getJSONObjectString("cryptoExchange"), CryptoExchange.class);
        ExchangeAPI exchangeAPI_currentBalance = Serialization.deserialize(o.getJSONObjectString("exchangeAPI_currentBalance"), ExchangeAPI.class);
        ExchangeAPI exchangeAPI_transactions = Serialization.deserialize(o.getJSONObjectString("exchangeAPI_transactions"), ExchangeAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("currentBalanceArrayList"), AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("transactionArrayList"), Transaction.class);
        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance, exchangeAPI_transactions, currentBalanceArrayList, transactionArrayList);
    }

    public ExchangeData(CryptoExchange CryptoExchange, ExchangeAPI exchangeAPI_currentBalance, ExchangeAPI exchangeAPI_transactions, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList) {
        this.cryptoExchange = CryptoExchange;
        this.exchangeAPI_currentBalance = exchangeAPI_currentBalance;
        this.exchangeAPI_transactions = exchangeAPI_transactions;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;

        netTransactionsMap = Transaction.resolveAssets(transactionArrayList);
        timestamp = new Date();
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

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
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

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
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

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static ExchangeData getNoData(CryptoExchange cryptoExchange) {
        // For exchanges, there will only be one API that can get the information.
        // But if it fails, we will still use the Unknown object.
        ExchangeAPI exchangeAPI_currentBalance_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ExchangeAPI exchangeAPI_transactions_f = UnknownExchangeAPI.createUnknownExchangeAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        return new ExchangeData(cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
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

        // Both ExchangeData objects should have the same cryptoExchange, but just in case we favor the newer one for consistency.
        return new ExchangeData(newExchangeData.cryptoExchange, exchangeAPI_currentBalance_f, exchangeAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public String getInfoString(HashMap<Asset, AssetQuantity> priceMap, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Exchange = " + cryptoExchange.exchange.toString());

        if(exchangeAPI_transactions == null || transactionArrayList == null) {
            s.appendRich("\n(Transaction information not present.)");
        }
        else {
            s.appendRich("\nTransaction Data Source = ").appendRich(exchangeAPI_transactions.getDisplayName());
            s.appendRich("\nNumber of Transactions = ").appendRich(Integer.toString(transactionArrayList.size()));
        }

        if(exchangeAPI_currentBalance == null || currentBalanceArrayList == null) {
            s.appendRich("\n(Current balance information not present.)");
        }
        else {
            s.appendRich("\nCurrent Balance Data Source = ").appendRich(exchangeAPI_currentBalance.getDisplayName());

            if(currentBalanceArrayList.isEmpty()) {
                s.appendRich("\nNo Current Balances");
            }
            else {
                s.appendRich("\nCurrent Balances:");
                s.append(AssetQuantity.getAssetInfo(currentBalanceArrayList, priceMap, isRich));

                if(priceMap != null && !priceMap.isEmpty()) {
                    s.appendRich("\n\nData Source = CoinGecko API V3");
                }
            }
        }

        return s.toString();
    }

    public String getRawFullInfoString() {
        // Get regular info and also the complete set of transactions and net transaction sums.
        StringBuilder s = new StringBuilder(getInfoString(null, false));

        if(exchangeAPI_transactions != null && transactionArrayList != null) {
            if(transactionArrayList.isEmpty()) {
                s.append("\nNo Transactions");
            }
            else {
                s.append("\nTransactions:\n");
                s.append(Serialization.serializeArrayList(transactionArrayList));

                s.append("\n\nNet Transaction Sums:");
                for(Asset asset : netTransactionsMap.keySet()) {
                    AssetAmount assetAmount = netTransactionsMap.get(asset);
                    AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);
                    s.append("\n    ").append(assetQuantity);
                }
            }
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

    public HashMap<Asset, AssetAmount> getDiscrepancyMap() {
        if(transactionArrayList == null || currentBalanceArrayList == null) {
            return new HashMap<>();
        }

        // Create the discrepancy map. Add anything in "netTransactionsMap", and subtract anything in "balancesMap".
        // Note that all AssetAmounts have the correct signed value, so we don't need to check "isLoss".
        HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();

        for(Asset asset : netTransactionsMap.keySet()) {
            AssetAmount assetAmount = netTransactionsMap.get(asset);
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

        return deltaMap;
    }

    public boolean hasDiscrepancy() {
        // Return true if there is at least one discrepancy.
        HashMap<Asset, AssetAmount> delta = getDiscrepancyMap();
        for(Asset asset : delta.keySet()) {
            AssetAmount assetAmount = delta.get(asset);
            if(assetAmount.amount.compareTo(BigDecimal.ZERO) != 0) {
                return true;
            }
        }

        return false;
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