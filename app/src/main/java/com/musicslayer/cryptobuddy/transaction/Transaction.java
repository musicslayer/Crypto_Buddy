package com.musicslayer.cryptobuddy.transaction;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.filter.Filter;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Transaction implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
    public Action action;
    public AssetQuantity actionedAssetQuantity;
    public AssetQuantity otherAssetQuantity;
    public AssetPrice forwardPrice;
    public AssetPrice backwardPrice;
    public Timestamp timestamp;
    public String info; // These are added by the app or the user.

    public String hash; // Used to break ties in sorting.

    // "otherAsset" must be non-null for Buy and Sell, and null for everything else.
    public Transaction(Action action, AssetQuantity actionedAssetQuantity, AssetQuantity otherAssetQuantity, Timestamp timestamp, String info) {
        this.action = action;
        this.actionedAssetQuantity = actionedAssetQuantity;
        this.otherAssetQuantity = otherAssetQuantity;
        this.forwardPrice = new AssetPrice(this.actionedAssetQuantity, this.otherAssetQuantity);
        this.backwardPrice = new AssetPrice(this.otherAssetQuantity, this.actionedAssetQuantity);
        this.timestamp = timestamp;
        this.info = info;

        if(isActionedAssetLoss()) { this.actionedAssetQuantity.assetAmount.isLoss = true; }
        if(otherAssetQuantity != null && isOtherAssetLoss()) { this.otherAssetQuantity.assetAmount.isLoss = true; }

        this.hash = getHash();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Transaction && hash.equals(((Transaction)other).hash);
    }

    public static void sortAscendingByType(ArrayList<Transaction> transactionArrayList, String sortType) {
        Comparator<Transaction> comparator = Transaction.getComparatorForType(sortType);
        Collections.sort(transactionArrayList, comparator);
    }

    public static void sortDescendingByType(ArrayList<Transaction> transactionArrayList, String sortType) {
        Comparator<Transaction> comparator = Transaction.getComparatorForType(sortType);
        Collections.sort(transactionArrayList, Collections.reverseOrder(comparator));
    }

    public boolean isFiltered(ArrayList<Filter> filterArrayList, ArrayList<String> columnTypes) {
        for(int i = 0; i < filterArrayList.size(); i++) {
            Filter filter = filterArrayList.get(i);

            if(filter == null) {
                continue;
            }

            if(!filter.isIncluded(getFilterDataForType(columnTypes.get(i)))) {
                return true;
            }
        }
        return false;
    }

    public String getFilterDataForType(String filterType) {
        String data;

        switch(filterType) {
            case "action":
                data = action.toString();
                break;
            case "quantity":
                data = actionedAssetQuantity.asset.getSettingName();
                break;
            case "other_quantity":
                if(otherAssetQuantity == null) {
                    data = "-";
                }
                else {
                    data = otherAssetQuantity.asset.getSettingName();
                }
                break;
            case "price":
                if(otherAssetQuantity == null) {
                    data = "-";
                }
                else {
                    data = actionedAssetQuantity.asset.getSettingName() + " / " + otherAssetQuantity.asset.getSettingName();
                }
                break;
            case "other_price":
                if(otherAssetQuantity == null) {
                    data = "-";
                }
                else {
                    data = otherAssetQuantity.asset.getSettingName() + " / " + actionedAssetQuantity.asset.getSettingName();
                }
                break;
            case "timestamp":
                if(timestamp.date == null) {
                    data = null;
                }
                else {
                    data = Long.toString(timestamp.date.getTime());
                }
                break;
            case "info":
                if(info == null || info.isEmpty()) {
                    data = "(No Info)";
                }
                else {
                    data = info;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + filterType);
        }

        return data;
    }

    public static ArrayList<String> getFilterDataForType(ArrayList<Transaction> transactionArrayList, String filterType) {
        ArrayList<Transaction> sortedTransactionArrayList = new ArrayList<>(transactionArrayList);
        Transaction.sortAscendingByType(sortedTransactionArrayList, filterType);

        ArrayList<String> data = new ArrayList<>();
        for(Transaction transaction : sortedTransactionArrayList) {
            data.add(transaction.getFilterDataForType(filterType));
        }

        return data;
    }

    public static Comparator<Transaction> getComparatorForType(String sortType) {
        Comparator<Transaction> comparator;

        switch(sortType) {
            case "action":
                comparator = (a, b) -> {
                    int s = Action.compare(a.action, b.action);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "quantity":
                comparator = (a, b) -> {
                    int s = AssetQuantity.compare(a.actionedAssetQuantity, b.actionedAssetQuantity);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "other_quantity":
                comparator = (a, b) -> {
                    int s = AssetQuantity.compare(a.otherAssetQuantity, b.otherAssetQuantity);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "price":
                comparator = (a, b) -> {
                    int s = AssetPrice.compare(a.forwardPrice, b.forwardPrice);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "other_price":
                comparator = (a, b) -> {
                    int s = AssetPrice.compare(a.backwardPrice, b.backwardPrice);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "timestamp":
                comparator = (a, b) -> {
                    int s = Timestamp.compare(a.timestamp, b.timestamp);
                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            case "info":
                comparator = (a, b) -> {
                    int s;
                    boolean isValidA = a.info != null;
                    boolean isValidB = b.info != null;

                    // Null is always smaller than a real action.
                    if(isValidA & isValidB) { s = a.info.compareTo(b.info); }
                    else { s = Boolean.compare(isValidA, isValidB); }

                    if(s == 0) { s = a.hash.compareTo(b.hash); }
                    return s;
                };
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + sortType);
        }

        return comparator;
    }

    public String getHash() {
        String otherAssetString;
        if(numAssets() == 2) {
            otherAssetString = otherAssetQuantity.toString();
        }
        else {
            otherAssetString = "-";
        }

        return action.toString() + "|" + actionedAssetQuantity.toString() + "|" + otherAssetString + "|" + forwardPrice.toString() + "|" + backwardPrice.toString() + "|" + timestamp.toString() + "|" + info;
    }

    public int numAssets() {
        return action.numAssets();
    }

    public boolean isActionedAssetLoss() {
        return action.isLoss();
    }

    public boolean isOtherAssetLoss() {
        return !action.isLoss();
    }

    public static HashMap<Asset, AssetAmount> resolveAssets(ArrayList<Transaction> transactionArrayList) {
        return resolveAssets(transactionArrayList, BigDecimal.ONE, BigDecimal.ONE);
    }

    public static HashMap<Asset, AssetAmount> resolveAssets(ArrayList<Transaction> transactionArrayList, BigDecimal receiveTaxMultiplier, BigDecimal sendTaxMultiplier) {
        // Factor in potential taxes. For example, if a token has a send tax of 10%, then 1.1 is the sendTaxMultiplier.
        // With tokenomics, the amount actually sent is greater than what the recorded transaction states.
        // Typically, receiveTaxMultiplier is always 1.
        HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();

        if(transactionArrayList != null) {
            for(Transaction t : transactionArrayList) {
                // Assume all loses are from sends, and apply the tax.
                if(t.isActionedAssetLoss()) {
                    subtractWithSendTax(deltaMap, t.actionedAssetQuantity, sendTaxMultiplier);
                }
                else {
                    addWithReceiveTax(deltaMap, t.actionedAssetQuantity, receiveTaxMultiplier);
                }

                if(t.otherAssetQuantity != null) {
                    if(t.isOtherAssetLoss()) {
                        subtractWithSendTax(deltaMap, t.otherAssetQuantity, sendTaxMultiplier);
                    }
                    else {
                        addWithReceiveTax(deltaMap, t.otherAssetQuantity, receiveTaxMultiplier);
                    }
                }
            }
        }

        return deltaMap;
    }

    public static void addWithReceiveTax(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity, BigDecimal receiveTaxMultiplier) {
        AssetAmount oldValue = HashMapUtil.getValueFromMap(map, assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.add(assetQuantity.assetAmount.multiply(new AssetAmount(receiveTaxMultiplier.toPlainString())));
        HashMapUtil.putValueInMap(map, assetQuantity.asset, newValue);
    }

    public static void subtractWithSendTax(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity, BigDecimal sendTaxMultiplier) {
        AssetAmount oldValue = HashMapUtil.getValueFromMap(map, assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.subtract(assetQuantity.assetAmount.multiply(new AssetAmount(sendTaxMultiplier.toPlainString())));
        HashMapUtil.putValueInMap(map, assetQuantity.asset, newValue);
    }

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("action", action, Action.class)
            .serialize("actionedAssetQuantity", actionedAssetQuantity, AssetQuantity.class)
            .serialize("otherAssetQuantity", otherAssetQuantity, AssetQuantity.class)
            .serialize("timestamp", timestamp, Timestamp.class)
            .serialize("info", info, String.class)
            .toStringOrNull();
    }

    public static Transaction legacy_deserializeFromJSON(String s, String version) throws JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        Action action = o.deserialize("action", Action.class);
        AssetQuantity actionedAssetQuantity = o.deserialize("actionedAssetQuantity", AssetQuantity.class);
        AssetQuantity otherAssetQuantity = o.deserialize("otherAssetQuantity", AssetQuantity.class);
        Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
        String info = o.deserialize("info", String.class);
        return new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info);
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("action", action, Action.class)
                .serialize("actionedAssetQuantity", actionedAssetQuantity, AssetQuantity.class)
                .serialize("otherAssetQuantity", otherAssetQuantity, AssetQuantity.class)
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("info", info, String.class)
                .endObject();
    }

    public static Transaction deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Transaction transaction;

        if("2".equals(version)) {
            Action action = o.deserialize("action", Action.class);
            AssetQuantity actionedAssetQuantity = o.deserialize("actionedAssetQuantity", AssetQuantity.class);
            AssetQuantity otherAssetQuantity = o.deserialize("otherAssetQuantity", AssetQuantity.class);
            Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
            String info = o.deserialize("info", String.class);
            o.endObject();

            transaction = new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info);
        }
        else {
            throw new IllegalStateException();
        }

        return transaction;
    }
}
