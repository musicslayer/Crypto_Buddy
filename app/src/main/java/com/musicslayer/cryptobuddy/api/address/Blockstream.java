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

public class Blockstream extends AddressAPI {
    public String getName() { return "Blockstream"; }
    public String getDisplayName() { return "Blockstream Esplora HTTP API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "BTC".equals(cryptoAddress.getPrimaryCoin().getKey());
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

        String addressDataJSON = WebUtil.get("https://blockstream.info" + urlPart + "api/address/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json0 = new JSONObject(addressDataJSON);
            JSONObject json10 = json0.getJSONObject("chain_stats");

            BigDecimal currentBalanceA = new BigDecimal(json10.getString("funded_txo_sum"));
            BigDecimal currentBalanceB = new BigDecimal(json10.getString("spent_txo_sum"));
            BigDecimal currentBalance = currentBalanceA.subtract(currentBalanceB);
            currentBalance = currentBalance.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());

            currentBalanceArrayList.add(new AssetQuantity(currentBalance.toPlainString(), cryptoAddress.getPrimaryCoin()));
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
        String addressDataJSON = WebUtil.get(url);
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
                    block_time = block_time.multiply(new BigInteger("1000"));
                    block_time_date = new Date(block_time.longValue());
                }

                BigDecimal balance_diff = BigDecimal.ZERO;
                boolean voutFound = false;
                boolean vinFound = false;

                JSONArray json2 = json.getJSONArray("vout");
                for(int j = 0; j < json2.length(); j++)
                {
                    JSONObject o = json2.getJSONObject(j);

                    if(!o.has("scriptpubkey_address") || !cryptoAddress.matchesAddress(o.getString("scriptpubkey_address"))) {
                        continue;
                    }

                    voutFound = true;
                    BigDecimal N = new BigDecimal(o.getString("value"));
                    balance_diff = balance_diff.add(N);
                }

                JSONArray json3 = json.getJSONArray("vin");
                for(int j = 0; j < json3.length(); j++)
                {
                    JSONObject o = json3.getJSONObject(j);

                    if(o.isNull("prevout")) {
                        continue;
                    }

                    JSONObject o2 = o.getJSONObject("prevout");

                    if(!o2.has("scriptpubkey_address") || !cryptoAddress.matchesAddress(o2.getString("scriptpubkey_address"))) {
                        continue;
                    }

                    vinFound = true;
                    BigDecimal N = new BigDecimal(o2.getString("value"));
                    balance_diff = balance_diff.subtract(N);
                }

                if(!voutFound && !vinFound) { continue; }

                String action = "Receive";
                if(balance_diff.compareTo(BigDecimal.ZERO) < 0) {
                    balance_diff = balance_diff.negate();
                    action = "Send";
                }

                balance_diff = balance_diff.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff.toPlainString(), cryptoAddress.getPrimaryCoin()), null, new Timestamp(block_time_date), "Transaction"));
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
