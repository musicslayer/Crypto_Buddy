package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetQuantityData;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.transaction.TransactionData;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class AddressData implements DataBridge.SerializableToJSON {
    final public CryptoAddress cryptoAddress;
    final public AddressAPI addressAPI_currentBalance;
    final public AddressAPI addressAPI_transactions;
    final public ArrayList<AssetQuantity> currentBalanceArrayList;
    final public ArrayList<Transaction> transactionArrayList;
    final public Timestamp timestamp_currentBalance;
    final public Timestamp timestamp_transactions;

    final public AssetQuantityData currentBalanceData;
    final public TransactionData transactionData;
    final public AssetQuantityData discrepancyData;

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("cryptoAddress", cryptoAddress, CryptoAddress.class)
                .serialize("addressAPI_currentBalance", addressAPI_currentBalance, AddressAPI.class)
                .serialize("addressAPI_transactions", addressAPI_transactions, AddressAPI.class)
                .serializeArrayList("currentBalanceArrayList", currentBalanceArrayList, AssetQuantity.class)
                .serializeArrayList("transactionArrayList", transactionArrayList, Transaction.class)
                .serialize("timestamp_currentBalance", timestamp_currentBalance, Timestamp.class)
                .serialize("timestamp_transactions", timestamp_transactions, Timestamp.class)
                .endObject();
    }

    public static AddressData deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        CryptoAddress cryptoAddress = o.deserialize("cryptoAddress", CryptoAddress.class);
        AddressAPI addressAPI_currentBalance = o.deserialize("addressAPI_currentBalance", AddressAPI.class);
        AddressAPI addressAPI_transactions = o.deserialize("addressAPI_transactions", AddressAPI.class);
        ArrayList<AssetQuantity> currentBalanceArrayList = o.deserializeArrayList("currentBalanceArrayList", AssetQuantity.class);
        ArrayList<Transaction> transactionArrayList = o.deserializeArrayList("transactionArrayList", Transaction.class);
        Timestamp timestamp_currentBalance = o.deserialize("timestamp_currentBalance", Timestamp.class);
        Timestamp timestamp_transactions = o.deserialize("timestamp_transactions", Timestamp.class);
        o.endObject();

        return new AddressData(cryptoAddress, addressAPI_currentBalance, addressAPI_transactions, currentBalanceArrayList, transactionArrayList, timestamp_currentBalance, timestamp_transactions);
    }

    public AddressData(CryptoAddress cryptoAddress, AddressAPI addressAPI_currentBalance, AddressAPI addressAPI_transactions, ArrayList<AssetQuantity> currentBalanceArrayList, ArrayList<Transaction> transactionArrayList, Timestamp timestamp_currentBalance, Timestamp timestamp_transactions) {
        this.cryptoAddress = cryptoAddress;
        this.addressAPI_currentBalance = addressAPI_currentBalance;
        this.addressAPI_transactions = addressAPI_transactions;
        this.currentBalanceArrayList = currentBalanceArrayList;
        this.transactionArrayList = transactionArrayList;
        this.timestamp_currentBalance = timestamp_currentBalance;
        this.timestamp_transactions = timestamp_transactions;

        currentBalanceData = new AssetQuantityData(currentBalanceArrayList);
        transactionData = new TransactionData(transactionArrayList);
        discrepancyData = new AssetQuantityData(getDiscrepancyMap());
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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
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

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public static AddressData getNoData(CryptoAddress cryptoAddress) {
        AddressAPI addressAPI_currentBalance_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        AddressAPI addressAPI_transactions_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public static AddressData getSingleAllData(CryptoAddress cryptoAddress, Crypto crypto) {
        AddressAPI addressAPI_currentBalance_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        AddressAPI addressAPI_transactions_f = UnknownAddressAPI.createUnknownAddressAPI(null);
        ArrayList<AssetQuantity> currentBalanceArrayList_f = null;
        ArrayList<Transaction> transactionArrayList_f = null;

        // Get current balance information.
        for(AddressAPI addressAPI : AddressAPI.address_apis) {
            if(!addressAPI.isSupported(cryptoAddress)) {
                continue;
            }

            currentBalanceArrayList_f = addressAPI.getSingleCurrentBalance(cryptoAddress, crypto);
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

            transactionArrayList_f = addressAPI.getSingleTransactions(cryptoAddress, crypto);
            if(transactionArrayList_f != null) {
                addressAPI_transactions_f = addressAPI;
                break;
            }
        }

        return new AddressData(cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, new Timestamp(), new Timestamp());
    }

    public boolean isComplete() {
        return isCurrentBalanceComplete() && isTransactionsComplete();
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
        Timestamp timestamp_currentBalance_f = oldAddressData.timestamp_currentBalance;
        Timestamp timestamp_transactions_f = oldAddressData.timestamp_transactions;

        if(newAddressData.isCurrentBalanceComplete()) {
            addressAPI_currentBalance_f = newAddressData.addressAPI_currentBalance;
            currentBalanceArrayList_f = newAddressData.currentBalanceArrayList;
            timestamp_currentBalance_f = newAddressData.timestamp_currentBalance;
        }

        if(newAddressData.isTransactionsComplete()) {
            addressAPI_transactions_f = newAddressData.addressAPI_transactions;
            transactionArrayList_f = newAddressData.transactionArrayList;
            timestamp_transactions_f = newAddressData.timestamp_transactions;
        }

        // Both AddressData objects should have the same cryptoAddress, but just in case we favor the newer one for consistency.
        return new AddressData(newAddressData.cryptoAddress, addressAPI_currentBalance_f, addressAPI_transactions_f, currentBalanceArrayList_f, transactionArrayList_f, timestamp_currentBalance_f, timestamp_transactions_f);
    }

    public String getInfoString(PriceData priceData, boolean isRich) {
        // Get address information. If the priceMap is not null, add in the prices of each asset in the map.
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Address = " + cryptoAddress.toString());

        if(addressAPI_transactions == null || transactionArrayList == null) {
            s.appendRich("\n(Transaction information not present.)");
        }
        else {
            s.appendRich("\nTransaction Data Source = ").appendRich(addressAPI_transactions.getDisplayName());
            s.appendRich("\nTransaction Data Timestamp = ").appendRich(timestamp_transactions.toString());
            s.appendRich("\nNumber of Transactions = ").appendRich(Integer.toString(transactionArrayList.size()));
        }

        if(addressAPI_currentBalance == null || currentBalanceArrayList == null) {
            s.appendRich("\n(Current balance information not present.)");
        }
        else {
            s.appendRich("\nCurrent Balance Data Source = ").appendRich(addressAPI_currentBalance.getDisplayName());
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
        // Get regular info (without prices) and also the complete set of transactions and net transaction sums.
        StringBuilder s = new StringBuilder(getInfoString(null, false));

        if(addressAPI_transactions != null && transactionArrayList != null) {
            s.append("\n").append(transactionData.getAllTransactionInfo(null, false));
        }

        return s.toString();
    }

    public static String getRawFullInfoString(ArrayList<AddressData> addressDataArrayList) {
        if(addressDataArrayList == null) { return null; }

        StringBuilder s = new StringBuilder();
        for(int i = 0; i < addressDataArrayList.size(); i++) {
            AddressData addressData = addressDataArrayList.get(i);
            s.append(addressData.getRawFullInfoString());

            if(i < addressDataArrayList.size() - 1) {
                s.append("\n\n");
            }
        }

        return s.toString();
    }

    public String getDiscrepancyString(PriceData priceData, boolean isRich) {
        // Get discrepancy information. If the priceMap is not null, add in the prices of each asset in the map.
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Address = ").appendRich(cryptoAddress.toString()).appendRich("\n");

        if(!hasDiscrepancy()) {
            s.appendRich("\nThis address has no discrepancies.");
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
        String cryptoName = cryptoAddress.getPrimaryCoin().getKey();
        boolean isMainnet = cryptoAddress.network.isMainnet();
        String cryptoDisplayName = cryptoAddress.getPrimaryCoin().getDisplayName();

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
            case "SOL":
                info = "Some block-level rent payments may not show up as transactions.";
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