package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.CLO;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

// Alternative
// https://explorer.callisto.network/graphiql

public class Callisto extends AddressAPI {
    public final String APIKEY = "ZHZ4Y7XKI9JD6XT8HV9HDZJMA8RHY7Y6DP";

    public String getName() { return "Callisto"; }
    public String getDisplayName() { return "Callisto RPC API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "CLO".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explorer.callisto.network";
        }
        else {
            baseURL = "https://testnet-explorer.callisto.network";
        }

        String addressDataJSON = REST.get(baseURL + "/api?module=account&action=balance&address=" + cryptoAddress.address + "&apikey=" + APIKEY);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // CLO
            JSONObject json = new JSONObject(addressDataJSON);
            String currentBalance = new BigDecimal(json.getString("result")).movePointLeft(cryptoAddress.getCrypto().getScale()).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new CLO()));
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressTokenDataJSON = REST.get(baseURL + "/api?module=account&action=tokenlist&address=" + cryptoAddress.address + "&apikey=" + APIKEY);
            if(addressTokenDataJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressTokenDataJSON);
                JSONArray tokenDataArray = jsonToken.getJSONArray("result");
                for(int j = 0; j < tokenDataArray.length(); j++) {
                    JSONObject tokenData = tokenDataArray.getJSONObject(j);

                    String name = tokenData.getString("symbol");
                    String display_name = tokenData.getString("name");
                    int scale = new BigInteger(tokenData.getString("decimals")).intValue();
                    String key = tokenData.getString("contractAddress");
                    String id = key.toLowerCase();

                    Token token = TokenManager.getTokenManagerFromKey("CallistoTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    BigDecimal value = new BigDecimal(tokenData.getString("balance")); //
                    value = value.movePointLeft(token.getScale());
                    currentBalanceArrayList.add(new AssetQuantity(value.toPlainString(), token));
                }
            }
            catch(Exception e) {
                ThrowableLogger.processThrowable(e);
                return null;
            }
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explorer.callisto.network";
        }
        else {
            baseURL = "https://testnet-explorer.callisto.network";
        }

        // Normal Transactions - These are all CLO
        String addressDataJSON = REST.get(baseURL + "/api?module=account&action=txlist&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY);

        // Internal Transactions - These are all CLO
        String addressDataInternalJSON = REST.get(baseURL + "/api?module=account&action=txlistinternal&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY);

        if(addressDataJSON == null || addressDataInternalJSON == null) {
            return null;
        }

        try {
            // Normal
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonArray = json.getJSONArray("result");

            for(int j = 0; j < jsonArray.length(); j++) {
                JSONObject o = jsonArray.getJSONObject(j);

                Date block_time_date = null;

                BigInteger confirmations = new BigInteger(o.getString("confirmations"));
                if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                    BigInteger block_time = new BigInteger(o.getString("timeStamp"));
                    double block_time_d = block_time.doubleValue() * 1000;
                    block_time_date = new Date((long)block_time_d);
                }

                String from = o.getString("from");
                String to = o.getString("to");

                String action;
                BigDecimal fee;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    // We are sending crypto away.
                    action = "Send";

                    // We also have to add in the fee to the amount sent.
                    BigDecimal gasAmount = new BigDecimal(o.getString("gasUsed"));
                    BigDecimal gasPrice = new BigDecimal(o.getString("gasPrice"));
                    fee = gasAmount.multiply(gasPrice);
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip it.
                if("1".equals(o.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(o.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Internal
            JSONObject jsonInternal = new JSONObject(addressDataInternalJSON);
            JSONArray jsonInternalArray = jsonInternal.getJSONArray("result");

            for(int j = 0; j < jsonInternalArray.length(); j++) {
                JSONObject oI = jsonInternalArray.getJSONObject(j);

                // Internal transfers do not use confirmations.
                BigInteger block_time = new BigInteger(oI.getString("timeStamp"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = oI.getString("from");
                String to = oI.getString("to");

                String action;
                BigDecimal fee;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    // We are sending crypto away.
                    action = "Send";

                    // Internal transactions have no new fees (they are already counted above).
                    fee = BigDecimal.ZERO;
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip it.
                if("1".equals(oI.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(oI.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            // CLO-20 Transactions - Various Tokens
            String addressDataTokenJSON = REST.get(baseURL + "/api?module=account&action=tokentx&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY);

            if(addressDataTokenJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONArray("result");

                for(int j = 0; j < jsonTokenArray.length(); j++) {
                    JSONObject oT = jsonTokenArray.getJSONObject(j);

                    // Token transactions don't have an error flag or fees.

                    String from = oT.getString("from");
                    String to = oT.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    String action;

                    if(cryptoAddress.address.equalsIgnoreCase(from)) {
                        // We are sending crypto away.
                        action = "Send";
                    }
                    else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                        // We are receiving crypto. No fee.
                        action = "Receive";
                    }
                    else {
                        // We shouldn't get here...
                        continue;
                    }

                    BigInteger balance_diff = new BigInteger(oT.getString("value"));
                    BigDecimal balance_diff_d = new BigDecimal(balance_diff);

                    // Shift by token decimal
                    BigInteger tokenDecimal = new BigInteger(oT.getString("tokenDecimal"));
                    balance_diff_d = balance_diff_d.movePointLeft(tokenDecimal.intValue());
                    String balance_diff_s = balance_diff_d.toPlainString();

                    Date block_time_date = null;

                    BigInteger confirmations = new BigInteger(oT.getString("confirmations"));
                    if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                        BigInteger block_time = new BigInteger(oT.getString("timeStamp"));
                        double block_time_d = block_time.doubleValue() * 1000;
                        block_time_date = new Date((long)block_time_d);
                    }

                    String name = oT.getString("tokenSymbol");
                    String display_name = oT.getString("tokenName");
                    int scale = tokenDecimal.intValue();
                    String key = oT.getString("contractAddress");
                    String id = key.toLowerCase();

                    Token token = TokenManager.getTokenManagerFromKey("CallistoTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date), "Token Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
            catch(Exception e) {
                ThrowableLogger.processThrowable(e);
                return null;
            }
        }

        return transactionArrayList;
    }
}
