package com.musicslayer.cryptobuddy.api.address;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class AddressData implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(cryptoAddress, flags);
        out.writeString(addressAPI_currentBalance.getKey());
        out.writeString(addressAPI_transactions.getKey());
        out.writeTypedList(currentBalanceArrayList);
        out.writeTypedList(transactionArrayList);
    }

    public static final Parcelable.Creator<AddressData> CREATOR = new Parcelable.Creator<AddressData>() {
        @Override
        public AddressData createFromParcel(Parcel in) {
            CryptoAddress cryptoAddress = in.readParcelable(CryptoAddress.class.getClassLoader());
            AddressAPI addressAPI_currentBalance_f = AddressAPI.getAddressAPIFromKey(in.readString());
            AddressAPI addressAPI_transactions_f = AddressAPI.getAddressAPIFromKey(in.readString());
            ArrayList<AssetQuantity> currentBalanceArrayList_f = in.createTypedArrayList(AssetQuantity.CREATOR);
            ArrayList<Transaction> transactionArrayList_f = in.createTypedArrayList(Transaction.CREATOR);

            return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
        }

        @Override
        public AddressData[] newArray(int size) {
            return new AddressData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    final public CryptoAddress cryptoAddress;
    final public AddressAPI addressAPI_currentBalance;
    final public AddressAPI addressAPI_transactions;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;

    // Calculate at construction since it never changes.
    HashMap<Asset, AssetAmount> netTransactionsMap;

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
        CryptoAddress cryptoAddress = Serialization.deserialize(o.getJSONObjectString("cryptoAddress"), CryptoAddress.class);
        AddressAPI addressAPI_currentBalance = Serialization.deserialize(o.getJSONObjectString("addressAPI_currentBalance"), AddressAPI.class);
        AddressAPI addressAPI_transactions = Serialization.deserialize(o.getJSONObjectString("addressAPI_transactions"), AddressAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("currentBalanceArrayList"), AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("transactionArrayList"), Transaction.class);
        return new AddressData(cryptoAddress, addressAPI_currentBalance, addressAPI_transactions, currentBalanceArrayList, transactionArrayList);
    }

    public AddressData(CryptoAddress cryptoAddress, AddressAPI addressAPI_currentBalance, AddressAPI addressAPI_transactions, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList) {
        this.cryptoAddress = cryptoAddress;
        this.addressAPI_currentBalance = addressAPI_currentBalance;
        this.addressAPI_transactions = addressAPI_transactions;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;

        netTransactionsMap = Transaction.resolveAssets(transactionArrayList);
    }

    public static AddressData getAllData(CryptoAddress cryptoAddress) {
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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static AddressData getCurrentBalanceData(CryptoAddress cryptoAddress) {
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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static AddressData getTransactionsData(CryptoAddress cryptoAddress) {
        AddressAPI addressAPI_currentBalance_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        AddressAPI addressAPI_transactions_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public static AddressData getNoData(CryptoAddress cryptoAddress) {
        AddressAPI addressAPI_currentBalance_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        AddressAPI addressAPI_transactions_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public boolean isComplete() {
        return !(addressAPI_currentBalance instanceof UnknownAddressAPI) && !(addressAPI_transactions instanceof UnknownAddressAPI) && currentBalanceArrayList != null && transactionArrayList != null;
    }

    public boolean isCurrentBalanceComplete() {
        return !(addressAPI_currentBalance instanceof UnknownAddressAPI) && currentBalanceArrayList != null;
    }

    public boolean isTransactionsComplete() {
        return !(addressAPI_transactions instanceof UnknownAddressAPI) && transactionArrayList != null;
    }

    public static AddressData merge(AddressData oldAddressData, AddressData newAddressData) {
        AddressAPI addressAPI_currentBalance_f = oldAddressData.addressAPI_currentBalance;
        AddressAPI addressAPI_transactions_f = oldAddressData.addressAPI_transactions;
        ArrayList<AssetQuantity> currentBalanceArrayList_f = oldAddressData.currentBalanceArrayList;
        ArrayList<Transaction> transactionArrayList_f = oldAddressData.transactionArrayList;

        if(newAddressData.isCurrentBalanceComplete()) {
            addressAPI_currentBalance_f = newAddressData.addressAPI_currentBalance;
            currentBalanceArrayList_f = newAddressData.currentBalanceArrayList;
        }

        if(newAddressData.isTransactionsComplete()) {
            addressAPI_transactions_f = newAddressData.addressAPI_transactions;
            transactionArrayList_f = newAddressData.transactionArrayList;
        }

        // Both AddressData objects should have the same cryptoAddress, but just in case we favor the newer one for consistency.
        return new AddressData(newAddressData.cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f);
    }

    public String getInfoString() {
        StringBuilder s = new StringBuilder("Address = " + cryptoAddress.toString());

        if(addressAPI_transactions == null || transactionArrayList == null) {
            s.append("\n(Transaction information not present.)");
        }
        else {
            s.append("\nTransaction Data Source = ").append(addressAPI_transactions.getDisplayName());
            s.append("\nNumber of Transactions = ").append(transactionArrayList.size());
        }

        if(addressAPI_currentBalance == null || currentBalanceArrayList == null) {
            s.append("\n(Current balance information not present.)");
        }
        else {
            s.append("\nCurrent Balance Data Source = ").append(addressAPI_currentBalance.getDisplayName());

            if(currentBalanceArrayList.isEmpty()) {
                s.append("\nNo Current Balances");
            }
            else {
                s.append("\nCurrent Balances:");
                for(AssetQuantity assetQuantity : currentBalanceArrayList) {
                    s.append("\n    ").append(assetQuantity.toString());
                }
            }
        }

        return s.toString();
    }

    public String getFullInfoString() {
        // Get regular info and also the complete set of transactions and net transaction sums.
        StringBuilder s = new StringBuilder(getInfoString());

        if(addressAPI_transactions != null && transactionArrayList != null) {
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
                    s.append("\n    ").append(assetQuantity.toString());
                }
            }
        }

        return s.toString();
    }

    public static String getFullInfoString(ArrayList<AddressData> addressDataArrayList) {
        if(addressDataArrayList == null) { return null; }

        StringBuilder s = new StringBuilder();
        for(int i = 0; i < addressDataArrayList.size(); i++) {
            AddressData addressData = addressDataArrayList.get(i);
            s.append(addressData.getFullInfoString());

            if(i < addressDataArrayList.size() - 1) {
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
        HashMap<Asset, AssetAmount> delta = new HashMap<>();

        for(Asset asset : netTransactionsMap.keySet()) {
            AssetAmount assetAmount = netTransactionsMap.get(asset);
            add(delta, asset, assetAmount);
        }

        for(AssetQuantity assetQuantity : currentBalanceArrayList) {
            subtract(delta, assetQuantity.asset, assetQuantity.assetAmount);
        }

        return delta;
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

    public static boolean hasDiscrepancy(ArrayList<AddressData> addressDataArrayList) {
        for(AddressData addressData : addressDataArrayList) {
            if(addressData.hasDiscrepancy()) {
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
        // TODO Add back in Solana Rent?
        String cryptoName = cryptoAddress.getCrypto().getName();
        boolean isMainnet = cryptoAddress.network.isMainnet();
        String cryptoDisplayName = cryptoAddress.getCrypto().getDisplayName();

        String info;
        switch(cryptoName) {
            case "ATOM":
                info = "Some liquidity pool token swap operations will not show up as transactions.";
                break;
            case "BNBc":
                if(isMainnet) {
                    return null;
                }
                else {
                    info = "Transactions for testnet addresses are not available.";
                }
                break;
            case "ETH":
                if(isMainnet) {
                    return null;
                }
                else {
                    info = "Token balances for testnet addresses are not available.";
                }
                break;
            case "VET":
                info = "VeThor (VTHO) balance includes generated rewards from holding VeChain (VET), but these rewards do not show up as transactions.";
                break;
            default:
                return null;
        }

        return cryptoDisplayName + " (" + cryptoName + ")" + ": " + info;
    }

    public boolean hasProblem() {
        return getProblem() != null;
    }

    public static boolean hasProblem(ArrayList<AddressData> addressDataArrayList) {
        // Return true if there is any info to show for any crypto.
        for(AddressData addressData : addressDataArrayList) {
            if(addressData.hasProblem()) {
                return true;
            }
        }

        return false;
    }
}