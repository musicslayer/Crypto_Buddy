package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.BTC;
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

public class Blockstream extends AddressAPI {
    public String getName() { return "Blockstream"; }
    public String getDisplayName() { return "Blockstream Esplora HTTP API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "BTC".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String urlPart;
        if(cryptoAddress.network.isMainnet()) {
            urlPart = "/";
        }
        else {
            urlPart = "/testnet/";
        }

        String addressDataJSON = RESTUtil.get("https://blockstream.info" + urlPart + "api/address/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json0 = new JSONObject(addressDataJSON);
            JSONObject json10 = json0.getJSONObject("chain_stats");

            BigInteger currentBalance_intA = new BigInteger(json10.getString("funded_txo_sum"));
            BigInteger currentBalance_intB = new BigInteger(json10.getString("spent_txo_sum"));
            BigInteger currentBalance_int = currentBalance_intA.subtract(currentBalance_intB);

            double currentBalance_d = currentBalance_int.doubleValue();
            currentBalance_d = currentBalance_d * Math.pow(10, -8);
            String currentBalance = Double.toString(currentBalance_d);

            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new BTC()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String urlPart;
        if(cryptoAddress.network.isMainnet()) {
            urlPart = "/";
        }
        else {
            urlPart = "/testnet/";
        }

        String lastID = "";
        for(;;) {
            String url = "https://blockstream.info" + urlPart + "api/address/" + cryptoAddress.address + "/txs/chain/" + lastID;
            lastID = process(url, cryptoAddress, transactionArrayList);

            if(ERROR.equals(lastID)) {
                return null;
            }
            else if(DONE.equals(lastID)) {
                break;
            }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String process(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String lastID = DONE;

            JSONArray jsonA = new JSONArray(addressDataJSON);

            for(int i = 0; i < jsonA.length(); i++) {
                JSONObject json = jsonA.getJSONObject(i);

                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = json.getString("txid");

                // We don't have fee information. But in reality, we should count the fee even if the transaction fails.
                JSONObject status = json.getJSONObject("status");
                if(!status.getBoolean("confirmed")) {
                    continue;
                }

                Date block_time_date = null;

                JSONObject jsonStatus = json.getJSONObject("status");
                boolean jsonConfirmed = jsonStatus.getBoolean("confirmed");

                if(jsonConfirmed) {
                    BigInteger block_time = new BigInteger(jsonStatus.getString("block_time"));
                    double block_time_d = block_time.doubleValue() * 1000;
                    block_time_date = new Date((long)block_time_d);
                }

                BigInteger balance_diff = BigInteger.valueOf(0);
                boolean voutFound = false;
                boolean vinFound = false;

                JSONArray json2 = json.getJSONArray("vout");
                for(int j = 0; j < json2.length(); j++)
                {
                    JSONObject o = json2.getJSONObject(j);

                    String scriptpubkey_address = o.getString("scriptpubkey_address");
                    if(!cryptoAddress.matchesAddress(scriptpubkey_address)) { continue; }

                    voutFound = true;
                    BigInteger N = new BigInteger(o.getString("value"));
                    balance_diff = balance_diff.add(N);

                    break;
                }

                JSONArray json3 = json.getJSONArray("vin");
                for(int j = 0; j < json3.length(); j++)
                {
                    JSONObject o = json3.getJSONObject(j);
                    JSONObject o2 = o.getJSONObject("prevout");

                    String scriptpubkey_address = o2.getString("scriptpubkey_address");
                    if(!cryptoAddress.matchesAddress(scriptpubkey_address)) { continue; }

                    vinFound = true;
                    BigInteger N = new BigInteger(o2.getString("value"));
                    balance_diff = balance_diff.subtract(N);

                    break;
                }

                if(!voutFound && !vinFound) { continue; }

                String action = "Receive";
                if(balance_diff.compareTo(BigInteger.valueOf(0)) < 0) {
                    balance_diff = balance_diff.negate();
                    action = "Send";
                }

                double balance_diff_d = balance_diff.doubleValue();
                balance_diff_d = balance_diff_d * Math.pow(10, -8);
                String balance_diff_s = Double.toString(balance_diff_d);

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
