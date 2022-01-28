package com.musicslayer.cryptobuddy.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.filter.Filter;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Transaction implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(action, flags);
        out.writeParcelable(actionedAssetQuantity, flags);
        out.writeParcelable(otherAssetQuantity, flags);
        out.writeParcelable(timestamp, flags);
        out.writeString(info);
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            Action action = in.readParcelable(Action.class.getClassLoader());
            AssetQuantity actionedAssetQuantity = in.readParcelable(AssetQuantity.class.getClassLoader());
            AssetQuantity otherAssetQuantity = in.readParcelable(AssetQuantity.class.getClassLoader());
            Timestamp timestamp = in.readParcelable(Timestamp.class.getClassLoader());
            String info = in.readString();
            return new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

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

        if(isActionedAssetLoss()) { this.actionedAssetQuantity.setLoss(); }
        if(otherAssetQuantity != null && isOtherAssetLoss()) { this.otherAssetQuantity.setLoss(); }

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
        HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
        for(Transaction t : transactionArrayList) {
            if(t.isActionedAssetLoss()) {
                subtract(deltaMap, t.actionedAssetQuantity);
            }
            else {
                add(deltaMap, t.actionedAssetQuantity);
            }

            if(t.otherAssetQuantity != null) {
                if(t.isOtherAssetLoss()) {
                    subtract(deltaMap, t.otherAssetQuantity);
                }
                else {
                    add(deltaMap, t.otherAssetQuantity);
                }
            }
        }

        return deltaMap;
    }

    public static void add(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity) {
        AssetAmount oldValue = map.get(assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.add(assetQuantity.assetAmount);
        map.put(assetQuantity.asset, newValue);
    }

    public static void subtract(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity) {
        AssetAmount oldValue = map.get(assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.subtract(assetQuantity.assetAmount);
        map.put(assetQuantity.asset, newValue);
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("action", new Serialization.JSONObjectWithNull(Serialization.serialize(action)))
            .put("actionedAssetQuantity", new Serialization.JSONObjectWithNull(Serialization.serialize(actionedAssetQuantity)))
            .put("otherAssetQuantity", new Serialization.JSONObjectWithNull(Serialization.serialize(otherAssetQuantity)))
            .put("timestamp", new Serialization.JSONObjectWithNull(Serialization.serialize(timestamp)))
            .put("info", Serialization.string_serialize(info))
            .toStringOrNull();
    }

    public static Transaction deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        Action action = Serialization.deserialize(o.getJSONObjectString("action"), Action.class);
        AssetQuantity actionedAssetQuantity = Serialization.deserialize(o.getJSONObjectString("actionedAssetQuantity"), AssetQuantity.class);
        AssetQuantity otherAssetQuantity = Serialization.deserialize(o.getJSONObjectString("otherAssetQuantity"), AssetQuantity.class);
        Timestamp timestamp = Serialization.deserialize(o.getJSONObjectString("timestamp"), Timestamp.class);
        String info = Serialization.string_deserialize(o.getString("info"));
        return new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info);
    }
}
