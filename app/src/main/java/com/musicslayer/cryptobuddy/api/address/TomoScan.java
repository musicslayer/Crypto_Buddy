package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.coin.TOMO;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// 0x1f4540338ced73d5c7a7a986e32df91f6902357d
// Take into account trades.

public class TomoScan extends AddressAPI {
    public String getName() { return "TomoScan"; }
    public String getDisplayName() { return "TomoScan API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "TOMO".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://scan.tomochain.com";
        }
        else {
            baseURL = "https://scan.testnet.tomochain.com";
        }

        String addressDataJSON = RESTUtil.get(baseURL + "/api/accounts/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // TOMO
            JSONObject json = new JSONObject(addressDataJSON);
            String currentBalance = new BigDecimal(json.getString("balance")).movePointLeft(cryptoAddress.getCrypto().getScale()).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new TOMO()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressTRC20DataJSON = RESTUtil.get(baseURL + "/api/tokens/holding/TRC20/" + cryptoAddress.address);
            String addressTRC21DataJSON = RESTUtil.get(baseURL + "/api/tokens/holding/TRC21/" + cryptoAddress.address);
            if(addressTRC20DataJSON == null || addressTRC21DataJSON == null) {
                return null;
            }

            try {
                // TRC20
                JSONObject jsonTRC20 = new JSONObject(addressTRC20DataJSON);
                JSONArray jsonTRC20Array = jsonTRC20.getJSONArray("items");
                for(int i = 0; i < jsonTRC20Array.length(); i++) {
                    JSONObject tokenData = jsonTRC20Array.getJSONObject(i);
                    if("NaN".equals(tokenData.getString("quantity"))) { continue; }

                    JSONObject tokenObj = tokenData.getJSONObject("tokenObj");

                    String key = tokenData.getString("token");
                    String name = tokenObj.getString("symbol");
                    String display_name = tokenObj.getString("name");
                    int scale = tokenObj.getInt("decimals");
                    //String id = tokenObj.getString("id"); // What is this???
                    String id = key;

                    Token token = TokenManager.getTokenManagerFromKey("TomoChainTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    String currentTokenBalance = new BigDecimal(tokenData.getString("quantity")).movePointLeft(scale).toPlainString();
                    currentBalanceArrayList.add(new AssetQuantity(currentTokenBalance, token));
                }

                // TRC21
                JSONObject jsonTRC21 = new JSONObject(addressTRC21DataJSON);
                JSONArray jsonTRC21Array = jsonTRC21.getJSONArray("items");
                for(int i = 0; i < jsonTRC21Array.length(); i++) {
                    JSONObject tokenData = jsonTRC21Array.getJSONObject(i);
                    if("NaN".equals(tokenData.getString("quantity"))) { continue; }


                    JSONObject tokenObj = tokenData.getJSONObject("tokenObj");

                    String key = tokenData.getString("token");
                    String name = tokenObj.getString("symbol");
                    String display_name = tokenObj.getString("name");
                    int scale = tokenObj.getInt("decimals");
                    //String id = tokenObj.getString("id"); // What is this???
                    String id = key;

                    Token token = TokenManager.getTokenManagerFromKey("TomoChainZTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    String currentTokenBalance = new BigDecimal(tokenData.getString("quantity")).movePointLeft(scale).toPlainString();
                    currentBalanceArrayList.add(new AssetQuantity(currentTokenBalance, token));
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://scan.tomochain.com";
        }
        else {
            baseURL = "https://scan.testnet.tomochain.com";
        }

        // Normal Transactions - These are all TOMO
        String addressDataJSON = RESTUtil.get(baseURL + "/api/txs/listByAccount/" + cryptoAddress.address + "?limit=100");

        // Internal Transactions - These are all TOMO
        String addressDataInternalJSON = RESTUtil.get(baseURL + "/api/txs/internal/" + cryptoAddress.address + "?limit=100");

        // Rewards - These are all TOMO
        String addressDataRewardJSON = RESTUtil.get(baseURL + "/api/rewards/" + cryptoAddress.address + "?limit=100");

        // Votes - Only for mainnet
        String addressDataVotesJSON;
        if(cryptoAddress.network.isMainnet()) {
            addressDataVotesJSON = RESTUtil.get("https://master.tomochain.com/api/transactions/voter/" + cryptoAddress.address);
        }
        else {
            addressDataVotesJSON = "{}";
        }

        if(addressDataJSON == null || addressDataInternalJSON == null || addressDataRewardJSON == null || addressDataVotesJSON == null) {
            return null;
        }

        try {
            // Normal
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonArray = json.getJSONArray("items");
            for(int j = 0; j < jsonArray.length(); j++) {
                JSONObject o = jsonArray.getJSONObject(j);

                String from = o.getString("from");
                String to = o.getString("to");

                String action;
                BigDecimal fee = BigDecimal.ZERO;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    action = "Send";

                    BigDecimal gasPrice = new BigDecimal(o.getString("gasPrice"));
                    BigDecimal gasUsed = new BigDecimal(o.getString("gasUsed"));
                    fee = gasPrice.multiply(gasUsed);
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    action = "Receive";
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                BigDecimal balance_diff = new BigDecimal(o.getString("value"));
                balance_diff = balance_diff.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff.toPlainString();

                String fee_s = fee.toPlainString();

                String block_time = o.getString("timestamp");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // Don't count vote/unvote/withdraw here
                if("0x0000000000000000000000000000000000000088".equals(to) || "0x0000000000000000000000000000000000000088".equals(from)) {
                    Log.e("Crypto Buddy", "X");
                    continue;
                }

                if(!o.getBoolean("status")) {
                    continue;
                }

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Internal
            JSONObject jsonInternal = new JSONObject(addressDataInternalJSON);
            JSONArray jsonInternalArray = jsonInternal.getJSONArray("items");
            for(int j = 0; j < jsonInternalArray.length(); j++) {
                JSONObject oI = jsonInternalArray.getJSONObject(j);

                // There is no flag for errors.

                String from = oI.getString("from");
                String to = oI.getString("to");

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // Don't count vote/unvote/withdraw here
                if("0x0000000000000000000000000000000000000088".equals(to) || "0x0000000000000000000000000000000000000088".equals(from)) {
                    continue;
                }

                String action;
                BigDecimal fee = BigDecimal.ZERO;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    action = "Send";
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    action = "Receive";
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                BigDecimal balance_diff = new BigDecimal(oI.getString("value"));
                balance_diff = balance_diff.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff.toPlainString();

                String fee_s = fee.toPlainString();

                String block_time = oI.getString("timestamp");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Internal Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }

            // Rewards
            JSONObject jsonReward = new JSONObject(addressDataRewardJSON);
            JSONArray jsonRewardArray = jsonReward.getJSONArray("items");
            for(int j = 0; j < jsonRewardArray.length(); j++) {
                JSONObject oR = jsonRewardArray.getJSONObject(j);

                // There is no flag for errors.

                String action = "Receive";

                String balance_diff_s = oR.getString("reward");

                String block_time = oR.getString("rewardTime");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Reward"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Votes
            if(cryptoAddress.network.isMainnet()) {
                JSONObject jsonVotes = new JSONObject(addressDataVotesJSON);
                JSONArray jsonVotesArray = jsonVotes.getJSONArray("items");
                for(int j = 0; j < jsonVotesArray.length(); j++) {
                    JSONObject oV = jsonVotesArray.getJSONObject(j);

                    // There is no flag for errors.

                    String event = oV.getString("event");
                    String action;

                    if("Withdraw".equals(event)) { // Unvote???
                        action = "Receive";
                    }
                    else if("Vote".equals(event) || "Propose".equals(event)) { // Unvote???
                        action = "Send";
                    }
                    else {
                        // Skip everything else to avoid double counting.
                        continue;
                    }

                    BigDecimal balance_diff_d = new BigDecimal(oV.getString("capacity"));
                    balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String balance_diff_s = balance_diff_d.toPlainString();

                    String block_time = oV.getString("createdAt");

                    // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date block_time_date = format.parse(block_time);

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), event));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            // TRC-20 and TRC-21 Transactions - Various Tokens
            String addressDataTokenJSON20 = RESTUtil.get(baseURL + "/api/token-txs/trc20?limit=50&holder=" + cryptoAddress.address);
            String addressDataTokenJSON21 = RESTUtil.get(baseURL + "/api/token-txs/trc21?limit=50&holder=" + cryptoAddress.address);

            if(addressDataTokenJSON20 == null || addressDataTokenJSON21 == null) {
                return null;
            }

            try {
                // TRC-20 Tokens
                JSONObject jsonToken20 = new JSONObject(addressDataTokenJSON20);
                JSONArray jsonTokenArray20 = jsonToken20.getJSONArray("items");

                for(int j = 0; j < jsonTokenArray20.length(); j++) {
                    JSONObject oT = jsonTokenArray20.getJSONObject(j);

                    // There is no flag for errors.

                    String from = oT.getString("from");
                    String to = oT.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    String action;
                    BigDecimal fee = BigDecimal.ZERO;

                    if(cryptoAddress.address.equalsIgnoreCase(from)) {
                        action = "Send";
                    }
                    else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                        action = "Receive";
                    }
                    else {
                        // We shouldn't get here...
                        continue;
                    }

                    BigDecimal balance_diff = new BigDecimal(oT.getString("value"));

                    // Shift by token decimal
                    BigInteger tokenDecimal = new BigInteger(oT.getString("decimals"));
                    balance_diff = balance_diff.movePointLeft(tokenDecimal.intValue());
                    String balance_diff_s = balance_diff.toPlainString();

                    String fee_s = fee.toPlainString();

                    String block_time = oT.getString("blockTime");

                    // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date block_time_date = format.parse(block_time);

                    String key = oT.getString("address");
                    String name = oT.getString("symbol");
                    //String display_name = tokenObj.getString("name");
                    int scale = oT.getInt("decimals");
                    //String id = tokenObj.getString("id"); // What is this???
                    String id = key;

                    Token token = TokenManager.getTokenManagerFromKey("TomoChainTokenManager").getOrLookupToken(baseURL, key, name, "?", scale, id);

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date), "Token Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, token), null, new Timestamp(block_time_date),"Token Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }

                // TRC-21 Tokens
                JSONObject jsonToken21 = new JSONObject(addressDataTokenJSON21);
                JSONArray jsonTokenArray21 = jsonToken21.getJSONArray("items");

                for(int j = 0; j < jsonTokenArray21.length(); j++) {
                    JSONObject oT = jsonTokenArray21.getJSONObject(j);

                    // There is no flag for errors.

                    String from = oT.getString("from");
                    String to = oT.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    String action;
                    BigDecimal fee = BigDecimal.ZERO;

                    if(cryptoAddress.address.equalsIgnoreCase(from)) {
                        action = "Send";
                    }
                    else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                        action = "Receive";
                    }
                    else {
                        // We shouldn't get here...
                        continue;
                    }

                    BigDecimal balance_diff = new BigDecimal(oT.getString("value"));

                    // Shift by token decimal
                    BigInteger tokenDecimal = new BigInteger(oT.getString("decimals"));
                    balance_diff = balance_diff.movePointLeft(tokenDecimal.intValue());
                    String balance_diff_s = balance_diff.toPlainString();

                    String fee_s = fee.toPlainString();

                    String block_time = oT.getString("blockTime");

                    // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date block_time_date = format.parse(block_time);

                    String key = oT.getString("address");
                    String name = oT.getString("symbol");
                    //String display_name = tokenObj.getString("name");
                    int scale = oT.getInt("decimals");
                    //String id = tokenObj.getString("id"); // What is this???
                    String id = key;

                    Token token = TokenManager.getTokenManagerFromKey("TomoChainZTokenManager").getOrLookupToken(baseURL, key, name, "?", scale, id);

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date), "Token Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, token), null, new Timestamp(block_time_date),"Token Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return transactionArrayList;
    }
}
