package com.musicslayer.cryptobuddy.transaction;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.filter.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Transaction implements Serializable {
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
                if(info == null || "".equals(info)) {
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
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = Action.compare(a.action, b.action);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "quantity":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = AssetQuantity.compare(a.actionedAssetQuantity, b.actionedAssetQuantity);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "other_quantity":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = AssetQuantity.compare(a.otherAssetQuantity, b.otherAssetQuantity);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "price":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = AssetPrice.compare(a.forwardPrice, b.forwardPrice);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "other_price":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = AssetPrice.compare(a.backwardPrice, b.backwardPrice);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "timestamp":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s = Timestamp.compare(a.timestamp, b.timestamp);
                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
                };
                break;
            case "info":
                comparator = new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction a, Transaction b) {
                        int s;
                        boolean isValidA = a.info != null;
                        boolean isValidB = b.info != null;

                        // Null is always smaller than a real action.
                        if(isValidA & isValidB) { s = a.info.compareTo(b.info); }
                        else { s = Boolean.compare(isValidA, isValidB); }

                        if(s == 0) { s = a.hash.compareTo(b.hash); }
                        return s;
                    }
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
            otherAssetString = otherAssetQuantity.toNumericString();
        }
        else {
            otherAssetString = "-";
        }

        return action.toString() + "|" + actionedAssetQuantity.toNumericString() + "|" + otherAssetString + "|" + forwardPrice.toString() + "|" + backwardPrice.toString() + "|" + timestamp.toString();
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
                t.subtract(deltaMap, t.actionedAssetQuantity);
            }
            else {
                t.add(deltaMap, t.actionedAssetQuantity);
            }

            if(t.otherAssetQuantity != null) {
                if(t.isOtherAssetLoss()) {
                    t.subtract(deltaMap, t.otherAssetQuantity);
                }
                else {
                    t.add(deltaMap, t.otherAssetQuantity);
                }
            }
        }

        return deltaMap;
    }

    public void add(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity) {
        AssetAmount oldValue = map.get(assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.add(assetQuantity.assetAmount);
        map.put(assetQuantity.asset, newValue);
    }

    public void subtract(HashMap<Asset, AssetAmount> map, AssetQuantity assetQuantity) {
        AssetAmount oldValue = map.get(assetQuantity.asset);
        if(oldValue == null) { oldValue = new AssetAmount("0"); }

        AssetAmount newValue = oldValue.subtract(assetQuantity.assetAmount);
        map.put(assetQuantity.asset, newValue);
    }
}
