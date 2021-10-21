package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

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

// Alternate base URL: https://lcd-cosmos.cosmostation.io/

// Liqudity pool token swaps not fully taken into account:
// cosmos1aj2yhukrny6l2es489qyt0uvna4zsr7fmpmpsv

// "https://node.atomscan.com" has pagination for balances, but there are so few tokens that we aren't gonna bother with this right now.

// ATOM balances must take into account transactions on all chains, cosmoshub-4, cosmoshub-3, etc...

public class AtomScan extends AddressAPI {
    public String getName() { return "AtomScan"; }
    public String getDisplayName() { return "AtomScan & Cosmostation REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ATOM".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String addressDataJSON = RESTUtil.get("https://node.atomscan.com/cosmos/bank/v1beta1/balances/" + cryptoAddress.address);
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

                    // Take the *ibc/ off the name.
                    int slashIdx = name.indexOf("/");
                    name = name.substring(slashIdx + 1);
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
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // Unsuccessful transaction changes are omitted, so we do not have to check ourselves.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String lastID = "";
        for(;;) {
            String url = "https://api.cosmostation.io/v1/account/new_txs/" + cryptoAddress.address + "?limit=50&from=" + lastID;
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

            ArrayList<String> txhashArrayList = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(addressDataJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                JSONObject jsonHeader = jsonArray.getJSONObject(i).getJSONObject("header");
                lastID = jsonHeader.getString("id");

                String chain_id_string = " [" + jsonHeader.getString("chain_id") + "]";
                String info_string = "";

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

                BigDecimal fee = BigDecimal.ZERO;
                if(tx.has("value")) {
                    if(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").length() > 0) {
                        fee = new BigDecimal(tx.getJSONObject("value").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                    }
                }
                else if(tx.has("auth_info")) {
                    if(tx.getJSONObject("auth_info").getJSONObject("fee").getJSONArray("amount").length() > 0) {
                        fee = new BigDecimal(tx.getJSONObject("auth_info").getJSONObject("fee").getJSONArray("amount").getJSONObject(0).getString("amount"));
                    }
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                boolean fee_enabled = false;
                String fee_string = "";

                // We look at the logs for transfers, because only these have token information,
                // but we look at the tx messages for other things, because only they specify all the addresses involved.
                JSONArray messages;
                if(tx.has("value")) {
                    messages = tx.getJSONObject("value").getJSONArray("msg");
                }
                else if(tx.has("body")) {
                    messages = tx.getJSONObject("body").getJSONArray("messages");
                }
                else {
                    messages = new JSONArray("[]");
                }

                for(int ii = 0; ii < messages.length(); ii++) {
                    JSONObject message = messages.getJSONObject(ii);
                    String type;

                    if(message.has("value")) {
                        type = message.getString("type");
                        message = message.getJSONObject("value");
                    }
                    else {
                        type = message.getString("@type");
                    }

                    switch(type) {
                        case "cosmos-sdk/MsgSend":
                        case "/cosmos.bank.v1beta1.MsgSend":
                            // Do nothing - transactions are handled elsewhere.
                            // However, enable the fee here because if the transaction failed there may not be a log entry to do so.
                            if(!cryptoAddress.matchesAddress(message.getString("from_address"))) { break; }

                            fee_enabled = true;
                            fee_string = "Transaction Fee";
                            info_string = "Transaction";

                            break;

                        case "cosmos-sdk/MsgDelegate":
                        case "/cosmos.staking.v1beta1.MsgDelegate":
                            if(!cryptoAddress.matchesAddress(message.getString("delegator_address"))) { break; }

                            fee_enabled = true;
                            fee_string = "Delegate Fee";
                            info_string = "Delegate";

                            JSONObject amount = message.getJSONObject("amount");
                            String name = amount.getString("denom");
                            Crypto crypto;
                            if("uatom".equalsIgnoreCase(name)) {
                                crypto = cryptoAddress.getCrypto();
                            }
                            else {
                                if(!shouldIncludeTokens(cryptoAddress)) {
                                    break;
                                }

                                // Take the *ibc/ off the name.
                                int slashIdx = name.indexOf("/");
                                name = name.substring(slashIdx + 1);
                                crypto = TokenManager.getTokenManagerFromKey("ATOMTokenManager").getToken(name, "?", "?", cryptoAddress.getCrypto().getScale(), "?");
                            }

                            BigDecimal value = new BigDecimal(amount.getString("amount"));
                            value = value.movePointLeft(crypto.getScale());

                            String balance_diff_s = value.toString();
                            transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),info_string + chain_id_string));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }

                            break;

                        case "cosmos-sdk/MsgUndelegate":
                        case "/cosmos.staking.v1beta1.MsgUndelegate":
                            if(!cryptoAddress.matchesAddress(message.getString("delegator_address"))) { break; }

                            fee_enabled = true;
                            fee_string = "Undelegate Fee";
                            info_string = "Undelegate";

                            JSONObject amount2 = message.getJSONObject("amount");
                            String name2 = amount2.getString("denom");
                            Crypto crypto2;
                            if("uatom".equalsIgnoreCase(name2)) {
                                crypto2 = cryptoAddress.getCrypto();
                            }
                            else {
                                if(!shouldIncludeTokens(cryptoAddress)) {
                                    break;
                                }

                                // Take the *ibc/ off the name.
                                int slashIdx = name2.indexOf("/");
                                name2 = name2.substring(slashIdx + 1);
                                crypto2 = TokenManager.getTokenManagerFromKey("ATOMTokenManager").getToken(name2, "?", "?", cryptoAddress.getCrypto().getScale(), "?");
                            }

                            BigDecimal value2 = new BigDecimal(amount2.getString("amount"));
                            value2 = value2.movePointLeft(crypto2.getScale());

                            String balance_diff_s2 = value2.toString();
                            transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s2, crypto2), null, new Timestamp(block_time_date),info_string + chain_id_string));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }

                            break;

                        case "cosmos-sdk/MsgBeginRedelegate":
                        case "/cosmos.staking.v1beta1.MsgBeginRedelegate":
                            // Don't do any additional processing, but we still may have a fee to pay.
                            if(!cryptoAddress.matchesAddress(message.getString("delegator_address"))) { break; }

                            fee_enabled = true;
                            fee_string = "Redelegate Fee";
                            break;

                        case "cosmos-sdk/MsgVote":
                        case "/cosmos.gov.v1beta1.MsgVote":
                            // Don't do any additional processing, but we still may have a fee to pay.
                            if(!cryptoAddress.matchesAddress(message.getString("voter"))) { break; }

                            fee_enabled = true;
                            fee_string = "Vote Fee";
                            break;

                        case "cosmos-sdk/MsgWithdrawDelegationReward":
                        case "/cosmos.distribution.v1beta1.MsgWithdrawDelegatorReward":
                            // Don't do any additional processing, but we still may have a fee to pay.
                            if(!cryptoAddress.matchesAddress(message.getString("delegator_address"))) { break; }

                            fee_enabled = true;
                            fee_string = "Reward Fee";
                            info_string = "Reward";
                            break;

                        default:
                            //Log.e("Crypto Buddy", "New Type = " + type);
                    }
                }

                if(jsonTransaction.has("logs")) {
                    JSONArray logs = jsonTransaction.getJSONArray("logs");

                    for(int iL = 0; iL < logs.length(); iL++) {
                        JSONObject log = logs.getJSONObject(iL);

                        if(!log.has("events")) {
                            continue;
                        }

                        JSONArray events = log.getJSONArray("events");

                        for(int ii = 0; ii < events.length(); ii++) {
                            JSONObject event = events.getJSONObject(ii);
                            String type = event.getString("type");

                            switch(type) {
                                case "transfer":
                                    if("".equals(info_string)) {
                                        info_string = "Transaction";
                                    }

                                    // For a transfer, keys come in sets of 3 - recipient, sender, amount
                                    // Or sets of 2, recipient, amount
                                    // Or sets of 2, sender, amount (?)
                                    // Note that we could have multiple transfers, and we could have either "sends" or "receives" here.
                                    JSONArray attributes = event.getJSONArray("attributes");

                                    // If an address is not filled in, it is assumed to be this one.
                                    String rAddress = cryptoAddress.address;
                                    String sAddress = cryptoAddress.address;

                                    for(int iii = 0; iii < attributes.length(); iii++) {
                                        JSONObject attribute = attributes.getJSONObject(iii);
                                        if("recipient".equals(attribute.getString("key"))) {
                                            rAddress = attribute.getString("value");
                                            continue;
                                        }
                                        else if("sender".equals(attribute.getString("key"))) {
                                            sAddress = attribute.getString("value");
                                            continue;
                                        }

                                        // This attribute is the one with the amounts.
                                        // We reached the end of the set. Continue on and process the transactions.
                                        String action;

                                        // Pay fee if there are any sends.
                                        if(cryptoAddress.matchesAddress(sAddress)) {
                                            fee_enabled = true;
                                            fee_string = "Transaction Fee";
                                            action = "Send";
                                        }
                                        else if(cryptoAddress.matchesAddress(rAddress)) {
                                            action = "Receive";
                                        }
                                        else {
                                            continue;
                                        }

                                        String[] amountSA = attribute.getString("value").split(",");
                                        for(String amountS : amountSA) {
                                            // Separate into amount and name
                                            Pattern pattern = Pattern.compile("[a-zA-Z]");
                                            Matcher matcher = pattern.matcher(amountS);

                                            matcher.find();
                                            int idx = matcher.start();

                                            Crypto crypto;

                                            // In the future, there may be other COSMOS tokens.
                                            String name = amountS.substring(idx);
                                            if("uatom".equalsIgnoreCase(name)) {
                                                crypto = cryptoAddress.getCrypto();
                                            }
                                            else {
                                                if(!shouldIncludeTokens(cryptoAddress)) {
                                                    continue;
                                                }

                                                // Take the *ibc/ off the name.
                                                int slashIdx = name.indexOf("/");
                                                name = name.substring(slashIdx + 1);
                                                crypto = TokenManager.getTokenManagerFromKey("ATOMTokenManager").getToken(name, "?", "?", cryptoAddress.getCrypto().getScale(), "?");
                                            }

                                            BigDecimal value = new BigDecimal(amountS.substring(0, idx));

                                            value = value.movePointLeft(crypto.getScale());
                                            String balance_diff_s = value.toString();
                                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),info_string + chain_id_string));
                                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                                        }
                                    }
                                    break;

                                case "delegate":
                                    // Do nothing - these are handled elsewhere.
                                    break;

                                case "unbond":
                                    // Do nothing - these are handled elsewhere.
                                    break;

                                case "proposal_vote":
                                    // Do nothing - these are handled elsewhere.
                                    break;

                                case "redelegate":
                                    // Do nothing - these are handled elsewhere.
                                    break;

                                case "cdp_draw":
                                case "withdraw_rewards":
                                case "claim_reward":
                                case "claim_atomic_swap":
                                case "hard_withdrawal":
                                case "cdp_withdrawal":
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

                                // Other types:
                                // ibc_transfer
                                // send_packet
                                // acknowledge_packet

                                default:
                                    Log.e("Crypto Buddy", "New Type = " + type);
                            }
                        }
                    }
                }
                if(fee_enabled & fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), fee_string + chain_id_string));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            // lastID will be the last ID we processed, or DONE if we didn't process anything.
            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
