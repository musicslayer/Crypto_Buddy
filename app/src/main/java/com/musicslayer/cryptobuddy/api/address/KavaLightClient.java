package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.KAVA;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KavaLightClient extends AddressAPI {
    public String getName() { return "KavaLightClient"; }
    public String getDisplayName() { return "Kava Light Client RPC API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "KAVA".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.kava.io";
        }
        else {
            baseURL = "https://api.data-testnet.kava.io";
        }

        String addressDataJSON = REST.get(baseURL + "/bank/balances/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            boolean hasNativeCoin = false;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray results = json.getJSONArray("result");
            for(int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String denom = result.getString("denom").toUpperCase();

                BigDecimal b = new BigDecimal(result.getString("amount"));
                b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                String currentBalance = b.toPlainString();

                Crypto crypto;
                if("UKAVA".equals(denom)) {
                    crypto = new KAVA();
                    hasNativeCoin = true;
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("KavaTokenManager").getToken(denom, "?", "?", 0, "?");
                }

                currentBalanceArrayList.add(new AssetQuantity(currentBalance, crypto));
            }

            if(!hasNativeCoin) {
                // Always show a zero balance of the native coin.
                currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getCrypto()));
            }
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

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.kava.io";
        }
        else {
            baseURL = "https://api.data-testnet.kava.io";
        }

        String addressDataJSON_Send = REST.get(baseURL + "/txs?transfer.sender=" + cryptoAddress.address + "&limit=100");
        String addressDataJSON_Receive = REST.get(baseURL + "/txs?transfer.recipient=" + cryptoAddress.address + "&limit=100");
        String addressDataJSON_Delegate = REST.get(baseURL + "/txs?message.action=delegate&message.sender=" + cryptoAddress.address + "&limit=100");

        if(addressDataJSON_Send == null || addressDataJSON_Receive == null || addressDataJSON_Delegate == null) {
            return null;
        }

        try {
            // Make sure each transaction only pays the fee once.
            ArrayList<String> feeList = new ArrayList<>();

            // Send
            JSONObject json1 = new JSONObject(addressDataJSON_Send);
            JSONArray jsonTxs1 = json1.getJSONArray("txs");

            for(int i = 0; i < jsonTxs1.length(); i++) {
                JSONObject jsonTransaction = jsonTxs1.getJSONObject(i);

                String txhash = jsonTransaction.getString("txhash");

                String block_time = jsonTransaction.getString("timestamp");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                JSONObject tx = jsonTransaction.getJSONObject("tx");
                //BigDecimal fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));

                BigDecimal fee;
                try {
                    fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                }
                catch(Exception ignored) {
                    fee = BigDecimal.ZERO;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                JSONArray logs = jsonTransaction.getJSONArray("logs");

                for(int iL = 0; iL < logs.length(); iL++) {
                    JSONObject log = logs.getJSONObject(iL);
                    JSONArray events = log.getJSONArray("events");

                    Log.e("Crypto T", "SEND");

                    for(int ii = 0; ii < events.length(); ii++) {
                        JSONObject event = events.getJSONObject(ii);
                        String type = event.getString("type");

                        Log.e("Crypto T", "Type = " + type);

                        if("transfer".equals(type)) {
                            // For a transfer, keys come in sets of 3 - recipient, sender, amount
                            // Note that we could have multiple transfers, and we could have either "sends" or "receives" here.
                            JSONArray attributes = event.getJSONArray("attributes");

                            for(int iii = 0; iii < attributes.length(); iii+=3) {
                                JSONObject attributeR = attributes.getJSONObject(iii);
                                JSONObject attributeS = attributes.getJSONObject(iii + 1);
                                JSONObject attributeA = attributes.getJSONObject(iii + 2);

                                String rAddress = attributeR.getString("value");
                                String sAddress = attributeS.getString("value");

                                // Only process sends
                                if(!cryptoAddress.address.equals(sAddress)) {
                                    continue;
                                }

                                String[] amountSA = attributeA.getString("value").split(",");
                                for(String amountS : amountSA) {
                                    // Separate into amount and name
                                    Pattern pattern = Pattern.compile("[a-zA-Z]");
                                    Matcher matcher = pattern.matcher(amountS);

                                    matcher.find();
                                    int idx = matcher.start();

                                    Crypto crypto;

                                    String name = amountS.substring(idx).toUpperCase();
                                    if("UKAVA".equals(name)) {
                                        crypto = cryptoAddress.getCrypto();
                                    }
                                    else {
                                        if(!shouldIncludeTokens(cryptoAddress)) {
                                            continue;
                                        }

                                        crypto = TokenManager.getTokenManagerFromKey("KavaTokenManager").getToken(name, "?", "?", 0, "?");
                                    }

                                    BigDecimal value = new BigDecimal(amountS.substring(0, idx));

                                    if(crypto == null) {
                                        Log.e("Crypto Buddy", "N");
                                    }

                                    value = value.movePointLeft(crypto.getScale());
                                    String balance_diff_s = value.toString();
                                    transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                                }
                            }
                        }
                    }
                }

                String fee_s = fee.toPlainString();

                if(fee.compareTo(BigDecimal.ZERO) > 0 && !feeList.contains(txhash)) {
                    feeList.add(txhash);
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }

            // Receive
            JSONObject json2 = new JSONObject(addressDataJSON_Receive);
            JSONArray jsonTxs2 = json2.getJSONArray("txs");

            for(int i = 0; i < jsonTxs2.length(); i++) {
                JSONObject jsonTransaction = jsonTxs2.getJSONObject(i);

                String txhash = jsonTransaction.getString("txhash");

                String block_time = jsonTransaction.getString("timestamp");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                JSONObject tx = jsonTransaction.getJSONObject("tx");

                BigDecimal fee;
                try {
                    fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                }
                catch(Exception ignored) {
                    fee = BigDecimal.ZERO;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                JSONArray logs = jsonTransaction.getJSONArray("logs");

                boolean fee_enabled = false;

                for(int iL = 0; iL < logs.length(); iL++) {
                    JSONObject log = logs.getJSONObject(iL);
                    JSONArray events = log.getJSONArray("events");

                    Log.e("Crypto T", "Receive");

                    for(int ii = 0; ii < events.length(); ii++) {
                        JSONObject event = events.getJSONObject(ii);
                        String type = event.getString("type");

                        Log.e("Crypto T", "Type = " + type);

                        if("transfer".equals(type)) {
                            // For a transfer, keys come in sets of 3 - recipient, sender, amount
                            // Note that we could have multiple transfers, and we could have either "sends" or "recieves" here.
                            JSONArray attributes = event.getJSONArray("attributes");

                            for(int iii = 0; iii < attributes.length(); iii+=3) {
                                JSONObject attributeR = attributes.getJSONObject(iii);
                                JSONObject attributeS = attributes.getJSONObject(iii + 1);
                                JSONObject attributeA = attributes.getJSONObject(iii + 2);

                                String rAddress = attributeR.getString("value");

                                // Only process sends
                                if(!cryptoAddress.address.equals(rAddress)) {
                                    continue;
                                }

                                String[] amountSA = attributeA.getString("value").split(",");
                                for(String amountS : amountSA) {
                                    // Separate into amount and name
                                    Pattern pattern = Pattern.compile("[a-zA-Z]");
                                    Matcher matcher = pattern.matcher(amountS);

                                    matcher.find();
                                    int idx = matcher.start();

                                    Crypto crypto;

                                    String name = amountS.substring(idx).toUpperCase();
                                    if("UKAVA".equals(name)) {
                                        crypto = cryptoAddress.getCrypto();
                                    }
                                    else {
                                        if(!shouldIncludeTokens(cryptoAddress)) {
                                            continue;
                                        }

                                        crypto = TokenManager.getTokenManagerFromKey("KavaTokenManager").getToken(name, "?", "?", 0, "?");
                                    }

                                    BigDecimal value = new BigDecimal(amountS.substring(0, idx));

                                    if(crypto == null) {
                                        Log.e("Crypto Buddy", "N");
                                    }

                                    value = value.movePointLeft(crypto.getScale());
                                    String balance_diff_s = value.toString();
                                    transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                                }
                            }
                        }
                        else if("cdp_draw".equals(type) || "withdraw_rewards".equals(type) || "claim_reward".equals(type) || "claim_atomic_swap".equals(type) || "hard_withdrawal".equals(type) || "cdp_withdrawal".equals(type)) {
                            // Don't do any additional processing, but we know that we have to pay the fee here.
                            fee_enabled = true;
                        }
                        else if("unbond".equals(type)) {
                            // Unbonding fee is charged immediately, but the actual unbonding may or may not be finished yet.
                            fee_enabled = true;

                            Date now = new Date();

                            JSONArray undelegate_attributes = event.getJSONArray("attributes");

                            for(int iii = 0; iii < undelegate_attributes.length(); iii+=3) {
                                //JSONObject attributeValidator = undelegate_attributes.getJSONObject(iii);
                                JSONObject attributeAmount = undelegate_attributes.getJSONObject(iii + 1);
                                JSONObject attributeTime = undelegate_attributes.getJSONObject(iii + 2);

                                String undelegate_block_time = attributeTime.getString("value");
                                DateFormat undelegate_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                                undelegate_format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date undelegate_block_time_date = undelegate_format.parse(undelegate_block_time);

                                if(now.after(undelegate_block_time_date)) {
                                    BigDecimal undelegate_d = new BigDecimal(attributeAmount.getString("value"));
                                    undelegate_d = undelegate_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                                    String undelegate_s = undelegate_d.toPlainString();

                                    transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(undelegate_s, cryptoAddress.getCrypto()), null, new Timestamp(undelegate_block_time_date),"Undelegate"));
                                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                                }
                            }
                        }
                    }
                }

                String fee_s = fee.toPlainString();

                if(fee_enabled && fee.compareTo(BigDecimal.ZERO) > 0 && !feeList.contains(txhash)) {
                    feeList.add(txhash);
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }

            // Delegate
            JSONObject json3 = new JSONObject(addressDataJSON_Delegate);
            JSONArray jsonTxs3 = json3.getJSONArray("txs");

            for(int i = 0; i < jsonTxs3.length(); i++) {
                JSONObject jsonTransaction = jsonTxs3.getJSONObject(i);

                String txhash = jsonTransaction.getString("txhash");

                String block_time = jsonTransaction.getString("timestamp");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                JSONObject tx = jsonTransaction.getJSONObject("tx");
                //BigDecimal fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));

                BigDecimal fee;
                try {
                    fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                }
                catch(Exception ignored) {
                    fee = BigDecimal.ZERO;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                JSONArray logs = jsonTransaction.getJSONArray("logs");

                for(int iL = 0; iL < logs.length(); iL++) {
                    JSONObject log = logs.getJSONObject(iL);
                    JSONArray events = log.getJSONArray("events");

                    Log.e("Crypto T", "Delegate");

                    for(int ii = 0; ii < events.length(); ii++) {
                        JSONObject event = events.getJSONObject(ii);
                        String type = event.getString("type");

                        Log.e("Crypto T", "Type = " + type);

                        if("delegate".equals(type)) {
                            JSONArray attributes = event.getJSONArray("attributes");

                            for(int iii = 0; iii < attributes.length(); iii+=2) {
                                //JSONObject attributeValidator = attributes.getJSONObject(iii);
                                JSONObject attributeAmount = attributes.getJSONObject(iii + 1);

                                Crypto crypto = cryptoAddress.getCrypto();

                                BigDecimal value = new BigDecimal(attributeAmount.getString("value"));
                                value = value.movePointLeft(crypto.getScale());

                                String balance_diff_s = value.toString();
                                transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Delegate"));
                                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                            }
                        }
                    }
                }

                String fee_s = fee.toPlainString();

                if(fee.compareTo(BigDecimal.ZERO) > 0 && !feeList.contains(txhash)) {
                    feeList.add(txhash);
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Delegate Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
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
