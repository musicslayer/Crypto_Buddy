package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.BCH;
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

public class ActorForth extends AddressAPI {
    public String getName() { return "ActorForth"; }
    public String getDisplayName() { return "ActorForth BCH REST API V2"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "BCH".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://rest.bitcoin.com";
        }
        else {
            baseURL = "https://trest.bitcoin.com";
        }

        String addressDataJSON = WebUtil.get(baseURL + "/v2/address/details/" + cryptoAddress.address);
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

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://rest.bitcoin.com";
        }
        else {
            baseURL = "https://trest.bitcoin.com";
        }

        for(int page = 0; ; page++) {
            String url = baseURL + "/v2/address/transactions/" + cryptoAddress.address + "?page=" + page;
            String status = process(url, cryptoAddress, transactionArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String process(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = WebUtil.get(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("txs");

            // Currently, transaction data still uses legacy addresses, which are always case sensitive.
            // Thus, we do not use CryptoAddress.matchesAddress here.
            String legacyAddress = json.getString("legacyAddress");

            for(int i = 0; i < jsonData.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                BigDecimal balance = BigDecimal.ZERO;

                JSONArray vinArray = jsonTransaction.getJSONArray("vin");
                for(int ii = 0; ii < vinArray.length(); ii++) {
                    JSONObject vin = vinArray.getJSONObject(ii);

                    if(!vin.has("addr")) { continue; }
                    String addr = vin.getString("addr");
                    if(!legacyAddress.equals(addr)) { continue; }

                    balance = balance.subtract(new BigDecimal(vin.getString("value")));
                }

                JSONArray voutArray = jsonTransaction.getJSONArray("vout");
                for(int ii = 0; ii < voutArray.length(); ii++) {
                    JSONObject vout = voutArray.getJSONObject(ii);

                    if(!vout.has("scriptPubKey") || !vout.getJSONObject("scriptPubKey").has("addresses")) {
                        continue;
                    }

                    String addr = vout.getJSONObject("scriptPubKey").getJSONArray("addresses").getString(0);
                    if(!legacyAddress.equals(addr)) { continue; }

                    balance = balance.add(new BigDecimal(vout.getString("value")));
                }

                String action;
                if(balance.compareTo(BigDecimal.ZERO) > 0) {
                    action = "Receive";
                }
                else if(balance.compareTo(BigDecimal.ZERO) < 0) {
                    balance = balance.negate();
                    action = "Send";
                }
                else {
                    // Don't bother showing this transaction since there was no balance change.
                    continue;
                }

                BigInteger block_time = new BigInteger(jsonTransaction.getString("blocktime"));
                block_time = block_time.multiply(new BigInteger("1000"));
                Date block_time_date = new Date(block_time.longValue());

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
