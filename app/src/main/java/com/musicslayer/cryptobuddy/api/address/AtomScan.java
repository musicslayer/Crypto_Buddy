package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.Exception;
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

// TODO For multisend, you get a pair of entries instead of the triplet.

// Alternate base URL: https://lcd-cosmos.cosmostation.io/

public class AtomScan extends AddressAPI {
    public String getName() { return "AtomScan"; }
    public String getDisplayName() { return "AtomScan & Cosmostation REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ATOM".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String addressDataJSON = REST.get("https://node.atomscan.com/cosmos/bank/v1beta1/balances/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            boolean hasNativeCoin = false;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray balances = json.getJSONArray("balances");
            for(int i = 0; i < balances.length(); i++) {
                JSONObject balance = balances.getJSONObject(i);

                Crypto crypto;

                String name = balance.getString("denom");
                if("uatom".equals(name)) {
                    crypto = cryptoAddress.getCrypto();
                    hasNativeCoin = true;
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    // Take the "ibc/" off the name.
                    name = name.substring(4);
                    crypto = TokenManager.getTokenManagerFromKey("ATOMTokenManager").getToken(name, "?", "?", cryptoAddress.getCrypto().getScale(), "?");
                }

                BigDecimal b = new BigDecimal(balance.getString("amount"));
                b = b.movePointLeft(crypto.getScale());

                currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), crypto));
            }

            if(!hasNativeCoin) {
                // Always show a zero balance of the native coin.
                currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getCrypto()));
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // Unsuccessful transaction changes are omitted, so we do not have to check ourselves.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String addressDataJSON = REST.get("https://api.cosmostation.io/v1/account/new_txs/" + cryptoAddress.address + "?limit=50");

        // Next page:
        // Use last ID from header.
        // String addressDataJSON = REST.get("https://api.cosmostation.io/v1/account/new_txs/" + cryptoAddress.address + "?limit=50&from=1234567");

        if(addressDataJSON == null) {
            return null;
        }

        try {
            ArrayList<String> txhashArrayList = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(addressDataJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTransaction = jsonArray.getJSONObject(i).getJSONObject("data");

                String txhash = jsonTransaction.getString("txhash");
                if(txhashArrayList.contains(txhash)) {
                    continue;
                }

                txhashArrayList.add(txhash);

                String block_time = jsonTransaction.getString("timestamp");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                JSONObject tx = jsonTransaction.getJSONObject("tx");

                BigDecimal fee;
                try {
                    fee = new BigDecimal(tx.getJSONObject("auth_info").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                }
                catch(java.lang.Exception ignored) {
                    fee = BigDecimal.ZERO;
                }
                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                boolean fee_enabled = false;

                JSONArray logs = jsonTransaction.getJSONArray("logs");

                if(logs.length() == 0) {
                    fee_enabled = true;
                }

                for(int iL = 0; iL < logs.length(); iL++) {
                    JSONObject log = logs.getJSONObject(iL);
                    JSONArray events = log.getJSONArray("events");

                    for(int ii = 0; ii < events.length(); ii++) {
                        JSONObject event = events.getJSONObject(ii);
                        String type = event.getString("type");

                        switch(type) {
                            case "transfer":
                                // For a transfer, keys come in sets of 3 - recipient, sender, amount
                                // Note that we could have multiple transfers, and we could have either "sends" or "receives" here.
                                JSONArray attributes = event.getJSONArray("attributes");

                                for(int iii = 0; iii < attributes.length(); iii+=3) {
                                    JSONObject attributeR = attributes.getJSONObject(iii);
                                    JSONObject attributeS = attributes.getJSONObject(iii + 1);
                                    JSONObject attributeA = attributes.getJSONObject(iii + 2);

                                    String rAddress = attributeR.getString("value");
                                    String sAddress = attributeS.getString("value");

                                    String action;

                                    // Pay fee if there are any sends
                                    if(cryptoAddress.address.equals(sAddress)) {
                                        fee_enabled = true;
                                        action = "Send";
                                    }
                                    else if(cryptoAddress.address.equals(rAddress)) {
                                        action = "Receive";
                                    }
                                    else {
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

                                        // In the future, there may be other COSMOS tokens.
                                        String name = amountS.substring(idx).toUpperCase();
                                        if("UATOM".equals(name)) {
                                            crypto = cryptoAddress.getCrypto();
                                        }
                                        else {
                                            if(!shouldIncludeTokens(cryptoAddress)) {
                                                continue;
                                            }

                                            // Take the "ibc/" off the name.
                                            name = name.substring(4);
                                            crypto = TokenManager.getTokenManagerFromKey("ATOMTokenManager").getToken(name, "?", "?", cryptoAddress.getCrypto().getScale(), "?");
                                        }

                                        BigDecimal value = new BigDecimal(amountS.substring(0, idx));

                                        value = value.movePointLeft(crypto.getScale());
                                        String balance_diff_s = value.toString();
                                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                                    }
                                }
                                break;

                            case "delegate":
                                fee_enabled = true;

                                JSONArray attributesD = event.getJSONArray("attributes");

                                for(int iii = 0; iii < attributesD.length(); iii+=2) {
                                    //JSONObject attributeValidator = attributesD.getJSONObject(iii);
                                    JSONObject attributeAmount = attributesD.getJSONObject(iii + 1);

                                    Crypto crypto = cryptoAddress.getCrypto();

                                    BigDecimal value = new BigDecimal(attributeAmount.getString("value"));
                                    value = value.movePointLeft(crypto.getScale());

                                    String balance_diff_s = value.toString();
                                    transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Delegate"));
                                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                                }
                                break;

                            case "unbond":
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
                                break;

                            case "cdp_draw":
                            case "withdraw_rewards":
                            case "claim_reward":
                            case "claim_atomic_swap":
                            case "hard_withdrawal":
                            case "cdp_withdrawal":
                            case "redelegate":
                            case "proposal_vote":
                                // Don't do any additional processing, but we know that we have to pay the fee here.
                                fee_enabled = true;
                                break;

                            case "message":
                            case "denomination_trace":
                            case "fungible_token_packet":
                            case "recv_packet":
                            case "write_acknowledgement":
                            case "create_client":
                            case "update_client":
                            case "channel_open_confirm":
                            case "channel_open_try":
                            case "connection_open_confirm":
                            case "connection_open_try":
                            case "swap_within_batch":
                            case "withdraw_within_batch":
                            case "deposit_within_batch":
                                // Do nothing.
                                break;

                            default:
                                Log.e("Crypto Buddy", "New Type = " + type);
                        }
                    }
                }
                if(fee_enabled & fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        return transactionArrayList;
    }
}
