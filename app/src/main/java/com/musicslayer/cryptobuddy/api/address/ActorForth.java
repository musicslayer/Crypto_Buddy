package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.BCH;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

public class ActorForth extends AddressAPI {
    public String getName() { return "ActorForth"; }
    public String getDisplayName() { return "ActorForth BCH REST API V2"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "BCH".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

/*
        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://rest.bch.actorforth.org";
        }
        else {
            baseURL = "https://trest.bch.actorforth.org";
        }

 */

/*
        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://rest.bitcoin.com";
        }
        else {
            baseURL = "https://trest.bitcoin.com";
        }

 */

        String baseURL = "https://rest.bitcoin.com";

        String addressDataJSON = REST.get(baseURL + "/v2/address/details/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            currentBalanceArrayList.add(new AssetQuantity(json.getString("balance"), new BCH()));
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

        String baseURL = "https://rest.bitcoin.com";

        String addressDataJSON = REST.get(baseURL + "/v2/address/transactions/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("txs");
            String legacyAddress = json.getString("legacyAddress");

            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                JSONObject vin = jsonTransaction.getJSONArray("vin").getJSONObject(0);

                String action;
                BigDecimal fee;
                boolean isReceive;
                if("0".equals(vin.getString("vout"))) {
                    action = "Receive";
                    isReceive = true;
                    fee = BigDecimal.ZERO;
                }
                else {
                    action = "Send";
                    isReceive = false;
                    fee = new BigDecimal(jsonTransaction.getString("fees"));
                }

                JSONArray vout = jsonTransaction.getJSONArray("vout");
                for(int ii = 0; ii < vout.length(); ii++) {
                    JSONObject o2 = vout.getJSONObject(ii);

                    String a = o2.getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);

                    // For a receive, we want the matching one.
                    // For a send, we want the other one.
                    boolean match = a.equals(legacyAddress);
                    boolean want = (isReceive && match) || (!isReceive && !match);

                    if(!want) {
                        continue;
                    }

                    BigDecimal value = new BigDecimal(o2.getString("value"));

                    BigInteger block_time = new BigInteger(jsonTransaction.getString("blocktime"));
                    double block_time_d = block_time.doubleValue() * 1000;
                    Date block_time_date = new Date((long)block_time_d);

                    String balance_diff_s = value.toString();
                    String fee_s = fee.toPlainString();
                    Crypto crypto = cryptoAddress.getCrypto();

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, crypto), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
            }
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }

        return transactionArrayList;
    }
}
