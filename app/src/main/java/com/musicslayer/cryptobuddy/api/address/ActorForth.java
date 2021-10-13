package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.BCH;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

// Rate limit: 4 requests per 3 seconds.

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

        String addressDataJSON = RESTUtil.get(baseURL + "/v2/address/details/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            currentBalanceArrayList.add(new AssetQuantity(json.getString("balance"), new BCH()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL = "https://rest.bitcoin.com";
        for(int page = 0; ; page++) {
            String url = baseURL + "/v2/address/transactions/" + cryptoAddress.address + "?page=" + page;
            String status = process(url, page, cryptoAddress, transactionArrayList);

            if(status == null) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String process(String url, int page, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.get(url);
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
                    if(!o2.has("scriptPubKey") || !o2.getJSONObject("scriptPubKey").has("addresses")) {
                        continue;
                    }

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
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }

                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, crypto), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
            }

            // See if we have more pages. They are indexed from 0 to pagesTotal - 1.
            int pagesTotal = json.getInt("pagesTotal");
            if(page < pagesTotal - 1) {
                // Just return anything.
                return "NotDone";
            }
            else {

                return DONE;
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }
    }
}
