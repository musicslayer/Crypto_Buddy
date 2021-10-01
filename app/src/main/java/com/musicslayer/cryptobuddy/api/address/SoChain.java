package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

        String addressDataJSON = REST.get("https://chain.so/api/v2/get_address_balance/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address);
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
            ExceptionLogger.processException(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String urlPart;
        if(cryptoAddress.network.isMainnet()) {
            urlPart = "/";
        }
        else {
            urlPart = "test/";
        }

        String addressDataJSONReceived = REST.get("https://chain.so/api/v2/get_tx_received/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address);
        String addressDataJSONSpent = REST.get("https://chain.so/api/v2/get_tx_spent/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address);
        if(addressDataJSONReceived == null || addressDataJSONSpent == null) {
            return null;
        }

        try {
            JSONObject jsonAddress1 = new JSONObject(addressDataJSONReceived);
            JSONObject jsonAddress2 = new JSONObject(addressDataJSONSpent);

            HashMap <String, Double> txnToValue = new HashMap<>();
            HashMap <String, Date> txnToDate = new HashMap<>();

            // Address1
            JSONObject jsonAddress1_2 = jsonAddress1.getJSONObject("data");
            JSONArray jsonAddress1_3 = jsonAddress1_2.getJSONArray("txs");
            for(int j = 0; j < jsonAddress1_3.length(); j++)
            {
                JSONObject o = jsonAddress1_3.getJSONObject(j);

                String txn = o.getString("txid");

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
            }

            // Address2
            JSONObject jsonAddress2_2 = jsonAddress2.getJSONObject("data");
            JSONArray jsonAddress2_3 = jsonAddress2_2.getJSONArray("txs");
            for(int j = 0; j < jsonAddress2_3.length(); j++)
            {
                JSONObject o = jsonAddress2_3.getJSONObject(j);

                String txn = o.getString("txid");

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
            }

            List<String> keysValue = new ArrayList<>(txnToValue.keySet());
            for (int k = 0; k < keysValue.size(); k++) {
                String key =  keysValue.get(k);

                double sumValue = txnToValue.get(key);

                String action = "Receive";
                if(sumValue < 0) {
                    sumValue = -sumValue;
                    action = "Send";
                }

                Date date = txnToDate.get(key);

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(Double.toString(sumValue), cryptoAddress.getCrypto()), null, new Timestamp(date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }

        return transactionArrayList;
    }
}
