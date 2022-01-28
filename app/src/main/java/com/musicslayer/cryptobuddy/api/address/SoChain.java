package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
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

        String addressDataJSON = WebUtil.get("https://chain.so/api/v2/get_address_balance/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address);
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

        HashMap <String, BigDecimal> txnToValue = new HashMap<>();
        HashMap <String, Date> txnToDate = new HashMap<>();

        // Process all received. There is an implicit limit of 100.
        // Only check "max transactions", even if txnToValue/txnToDate don't grow that large.
        String lastReceivedID = "";
        for(int i = 100; i <= getMaxTransactions(); i += 100) {
            String url = "https://chain.so/api/v2/get_tx_received/" + cryptoAddress.getCrypto().getName() + urlPart + cryptoAddress.address + "/" + lastReceivedID;
            lastReceivedID = processReceived(url, cryptoAddress, txnToValue, txnToDate);

            if(ERROR.equals(lastReceivedID)) {
                return null;
            }
            else if(DONE.equals(lastReceivedID)) {
                break;
            }
        }

        // Process all spent. There is an implicit limit of 100.
        // Only check "max transactions", even if txnToValue/txnToDate don't grow that large.
        String lastSpentID = "";
        for(int i = 100; i <= getMaxTransactions(); i += 100) {
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
            BigDecimal sumValue_D = txnToValue.get(key);
            if(sumValue_D == null) { continue; }

            String action = "Receive";
            if(sumValue_D.compareTo(BigDecimal.ZERO) < 0) {
                sumValue_D = sumValue_D.negate();
                action = "Send";
            }

            Date date = txnToDate.get(key);

            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(sumValue_D.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(date), "Transaction"));
            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processReceived(String url, CryptoAddress cryptoAddress, HashMap <String, BigDecimal> txnToValue, HashMap <String, Date> txnToDate) {
        String addressDataJSONReceived = WebUtil.get(url);
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
                BigDecimal balance_diff_d = new BigDecimal(balance_diff_s);
                BigDecimal currentValue = BigDecimal.ZERO;
                if(txnToValue.containsKey(txn)) {
                    currentValue = txnToValue.get(txn);
                }
                currentValue = currentValue.add(balance_diff_d);
                txnToValue.put(txn, currentValue);

                Date block_time_date = null;
                BigInteger confirmations = new BigInteger(o.getString("confirmations"));
                if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                    BigInteger block_time = new BigInteger(o.getString("time"));
                    block_time = block_time.multiply(new BigInteger("1000"));
                    block_time_date = new Date(block_time.longValue());
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
    private String processSpent(String url, CryptoAddress cryptoAddress, HashMap <String, BigDecimal> txnToValue, HashMap <String, Date> txnToDate) {
        String addressDataJSONSpent = WebUtil.get(url);
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
                BigDecimal balance_diff_d = new BigDecimal(balance_diff_s);
                BigDecimal currentValue = BigDecimal.ZERO;
                if(txnToValue.containsKey(txn)) {
                    currentValue = txnToValue.get(txn);
                }
                currentValue = currentValue.subtract(balance_diff_d);
                txnToValue.put(txn, currentValue);

                Date block_time_date = null;
                int confirmations = o.getInt("confirmations");
                if(confirmations > 0) {
                    BigInteger block_time = new BigInteger(o.getString("time"));
                    block_time = block_time.multiply(new BigInteger("1000"));
                    block_time_date = new Date(block_time.longValue());
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
