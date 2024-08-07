package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

// Bitquery has no pagination on its results.

public class Bitquery_ETC extends AddressAPI {
    final String APIKEYNAME = "X-API-KEY";
    final String APIKEY = "BQYLR11ACrzwoU3N6iTNHKtZfgoNdWfI";

    public String getName() { return "Bitquery_ETC"; }
    public String getDisplayName() { return "Bitquery HTTP API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ETC".equals(cryptoAddress.getPrimaryCoin().getKey());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String body = "{\"query\" : \"" +
            "query{" +
            "  ethereum(network: ethclassic) {" +
            "    address(address: {is:\\\"" + cryptoAddress.address + "\\\"}) {" +
            "      balance" +
            "    }" +
            "  }" +
            "}" +
            "\"\n}";

        String addressDataJSON = WebUtil.post("https://graphql.bitquery.io", body, APIKEYNAME, APIKEY);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            String currentBalance = json.getJSONObject("data").getJSONObject("ethereum").getJSONArray("address").getJSONObject(0).getString("balance");
            currentBalanceArrayList.add(new AssetQuantity(currentBalance, cryptoAddress.getPrimaryCoin()));
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

        String bodyR = "{\"query\" : \"" +
            "query{" +
            "  ethereum(network: ethclassic) {" +
            "    transfers(options: {desc: \\\"block.timestamp.time\\\"}, receiver: {is: \\\"" + cryptoAddress.address + "\\\"}) {" +
            "      amount" +
            "      currency {" +
            "        name" +
            "      }" +
            "      transaction {" +
            "        gas" +
            "        gasPrice" +
            "      }" +
            "      block {" +
            "        timestamp {" +
            "          time(format: \\\"%Y-%m-%dT%H:%M:%SZ\\\")" +
            "        }" +
            "        height" +
            "      }" +
            "    }" +
            "  }" +
            "}" +
            "\"\n}";

        String bodyS = "{\"query\" : \"" +
                "query{" +
                "  ethereum(network: ethclassic) {" +
                "    transfers(options: {desc: \\\"block.timestamp.time\\\"}, sender: {is: \\\"" + cryptoAddress.address + "\\\"}) {" +
                "      amount" +
                "      currency {" +
                "        name" +
                "      }" +
                "      transaction {" +
                "        gas" +
                "        gasPrice" +
                "      }" +
                "      block {" +
                "        timestamp {" +
                "          time(format: \\\"%Y-%m-%dT%H:%M:%SZ\\\")" +
                "        }" +
                "        height" +
                "      }" +
                "    }" +
                "  }" +
                "}" +
                "\"\n}";

        String addressDataJSONReceive = WebUtil.post("https://graphql.bitquery.io", bodyR, APIKEYNAME, APIKEY);
        String addressDataJSONSend = WebUtil.post("https://graphql.bitquery.io", bodyS, APIKEYNAME, APIKEY);

        if(addressDataJSONReceive == null || addressDataJSONSend == null) {
            return null;
        }

        try {
            // Receive
            JSONObject jsonR = new JSONObject(addressDataJSONReceive);
            JSONArray jsonArrayR = jsonR.getJSONObject("data").getJSONObject("ethereum").getJSONArray("transfers");

            for(int j = 0; j < jsonArrayR.length(); j++) {
                JSONObject o = jsonArrayR.getJSONObject(j);

                BigDecimal balance_diff_d;
                balance_diff_d = new BigDecimal(o.getString("amount"));

                String action = "Receive";
                String balance_diff_s = balance_diff_d.toPlainString();

                String block_time = o.getJSONObject("block").getJSONObject("timestamp").getString("time");
                Date block_time_date = DateTimeUtil.parseStandard(block_time);

                Crypto crypto = cryptoAddress.getPrimaryCoin();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Send
            JSONObject jsonS = new JSONObject(addressDataJSONSend);
            JSONArray jsonArrayS = jsonS.getJSONObject("data").getJSONObject("ethereum").getJSONArray("transfers");

            for(int j = 0; j < jsonArrayS.length(); j++) {
                JSONObject o = jsonArrayS.getJSONObject(j);

                BigDecimal balance_diff_d;

                balance_diff_d = new BigDecimal(o.getString("amount"));

                String action = "Send";

                BigDecimal gas = new BigDecimal(o.getJSONObject("transaction").getString("gas"));
                BigDecimal gasPrice = new BigDecimal(o.getJSONObject("transaction").getString("gasPrice"));
                BigDecimal fee = gas.multiply(gasPrice);

                // Fee needs to be shifted by 9 decimal places, not the scale of ETC.
                fee = fee.movePointLeft(9);

                String fee_s = fee.toPlainString();
                String balance_diff_s = balance_diff_d.toPlainString();

                String block_time = o.getJSONObject("block").getJSONObject("timestamp").getString("time");
                Date block_time_date = DateTimeUtil.parseStandard(block_time);

                Crypto crypto = cryptoAddress.getPrimaryCoin();
                Crypto cryptoFee = cryptoAddress.getFeeCoin();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoFee), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return transactionArrayList;
    }
}
