package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.settings.MaxNumberTransactionsSetting;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SoChain extends AddressAPI {
    public String getName() { return "SoChain"; }
    public String getDisplayName() { return "SoChain API V2"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        // SoChain supports Bitcoin, Dash, Zcash, Dogecoin, and Litecoin.
        return "BTC".equals(cryptoAddress.getCrypto().getName()) || "DASH".equals(cryptoAddress.getCrypto().getName()) || "ZEC".equals(cryptoAddress.getCrypto().getName()) || "DOGE".equals(cryptoAddress.getCrypto().getName()) || "LTC".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String urlPart;
        if(cryptoAddress.network.isMainnet()) {
            urlPart = "/";
        }
        else {
            urlPart = "test/";
        }

        String addressDataJSON = RESTUtil.get("https://chain.so/api/v2/get_address_balance/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject jsonAddress0 = new JSONObject(addressDataJSON);
            JSONObject jsonAddress0_2 = jsonAddress0.getJSONObject("data");
            String currentBalance = jsonAddress0_2.getString("confirmed_balance");

            currentBalanceArrayList.add(new AssetQuantity(currentBalance, cryptoAddress.getCrypto()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        String urlPart;
        if(cryptoAddress.network.isMainnet()) {
            urlPart = "/";
        }
        else {
            urlPart = "test/";
        }

        HashMap <String, Double> txnToValue = new HashMap<>();
        HashMap <String, Date> txnToDate = new HashMap<>();

        // Process all received.
        String lastReceivedID = "";
        for(;;) {
            String url = "https://chain.so/api/v2/get_tx_received/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address + "/" + lastReceivedID;
            lastReceivedID = processReceived(url, cryptoAddress, txnToValue, txnToDate);

            if(ERROR.equals(lastReceivedID)) {
                return null;
            }
            else if(DONE.equals(lastReceivedID)) {
                break;
            }
        }

        // Process all spent.
        String lastSpentID = "";
        for(;;) {
            String url = "https://chain.so/api/v2/get_tx_spent/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address + "/" + lastSpentID;
            lastSpentID = processSpent(url, cryptoAddress, txnToValue, txnToDate);

            if(ERROR.equals(lastSpentID)) {
                return null;
            }
            else if(DONE.equals(lastSpentID)) {
                break;
            }
        }

        // Fill in all transactions.
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        for(String key : txnToValue.keySet()) {
            Double sumValue_D = txnToValue.get(key);
            if(sumValue_D == null) { continue; }

            double sumValue = sumValue_D;

            String action = "Receive";
            if(sumValue < 0) {
                sumValue = -sumValue;
                action = "Send";
            }

            Date date = txnToDate.get(key);

            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(Double.toString(sumValue), cryptoAddress.getCrypto()), null, new Timestamp(date), "Transaction"));
            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processReceived(String url, CryptoAddress cryptoAddress, HashMap <String, Double> txnToValue, HashMap <String, Date> txnToDate) {
        String addressDataJSONReceived = RESTUtil.get(url);
        if(addressDataJSONReceived == null) {
            return ERROR;
        }

        try {
            String lastID = DONE;

            JSONObject jsonAddress1 = new JSONObject(addressDataJSONReceived);
            JSONObject jsonAddress1_2 = jsonAddress1.getJSONObject("data");
            JSONArray jsonAddress1_3 = jsonAddress1_2.getJSONArray("txs");
            for(int j = 0; j < jsonAddress1_3.length(); j++)
            {
                JSONObject o = jsonAddress1_3.getJSONObject(j);

                String txn = o.getString("txid");

                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = txn;

                String balance_diff_s = o.getString("value");
                double balance_diff_d = Double.parseDouble(balance_diff_s);
                double currentValue = 0;
                if(txnToValue.containsKey(txn)) {
                    currentValue = txnToValue.get(txn);
                }
                currentValue += balance_diff_d;
                txnToValue.put(txn, currentValue);

                Date block_time_date = null;
                BigInteger confirmations = new BigInteger(o.getString("confirmations"));
                if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                    BigInteger block_time = new BigInteger(o.getString("time"));
                    double block_time_d = block_time.doubleValue() * 1000;
                    block_time_date = new Date((long)block_time_d);
                }

                txnToDate.put(txn, block_time_date);

                if(txnToValue.size() == getMaxTransactions()) { return DONE; }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processSpent(String url, CryptoAddress cryptoAddress, HashMap <String, Double> txnToValue, HashMap <String, Date> txnToDate) {
        String addressDataJSONSpent = RESTUtil.get(url);
        if(addressDataJSONSpent == null) {
            return ERROR;
        }

        try {
            String lastID = DONE;

            JSONObject jsonAddress2 = new JSONObject(addressDataJSONSpent);
            JSONObject jsonAddress2_2 = jsonAddress2.getJSONObject("data");
            JSONArray jsonAddress2_3 = jsonAddress2_2.getJSONArray("txs");
            for(int j = 0; j < jsonAddress2_3.length(); j++)
            {
                JSONObject o = jsonAddress2_3.getJSONObject(j);

                String txn = o.getString("txid");

                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = txn;

                String balance_diff_s = o.getString("value");
                double balance_diff_d = Double.parseDouble(balance_diff_s);
                double currentValue = 0;
                if(txnToValue.containsKey(txn)) {
                    currentValue = txnToValue.get(txn);
                }
                currentValue -= balance_diff_d;
                txnToValue.put(txn, currentValue);

                Date block_time_date = null;
                int confirmations = o.getInt("confirmations");
                if(confirmations > 0) {
                    int block_time = o.getInt("time");
                    double block_time_d = (double)block_time * 1000;
                    block_time_date = new Date((long)block_time_d);
                }

                txnToDate.put(txn, block_time_date);

                if(txnToValue.size() == getMaxTransactions() * 2) { return DONE; }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
