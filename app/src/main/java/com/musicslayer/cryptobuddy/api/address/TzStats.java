package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.XTZ_Testnet_Florencenet;
import com.musicslayer.cryptobuddy.asset.network.XTZ_Testnet_Granadanet;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
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

// Negative token balances???
// tz1RKWsgCD3X1uXsDDb9dxQZFmbFwSJNo4id
// https://tzkt.io/tz1RKWsgCD3X1uXsDDb9dxQZFmbFwSJNo4id/tokens
// https://api.better-call.dev/v1/account/mainnet/tz1RKWsgCD3X1uXsDDb9dxQZFmbFwSJNo4id/token_balances?size=50&offset=

public class TzStats extends AddressAPI {
    public String getName() { return "TzStats"; }
    public String getDisplayName() { return "TzStats & Better Call Dev REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "XTZ".equals(cryptoAddress.getPrimaryCoin().getKey());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.tzstats.com";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Florencenet) {
            baseURL = "https://api.florence.tzstats.com";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Granadanet) {
            baseURL = "https://api.granada.tzstats.com";
        }
        else {
            return null;
        }

        String networkString;
        if(cryptoAddress.network.isMainnet()) {
            networkString = "mainnet";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Florencenet) {
            networkString = "florencenet";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Granadanet) {
            networkString = "granadanet";
        }
        else {
            return null;
        }

        String addressDataJSON = WebUtil.get(baseURL + "/explorer/account/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            // Account may not be active, so say 0 Tezos.
            // We really need a better way to check this case.
            currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getPrimaryCoin()));
            return currentBalanceArrayList;
        }

        try {
            // XTZ
            JSONObject json = new JSONObject(addressDataJSON);
            String currentBalance = new BigDecimal(json.getString("spendable_balance")).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(currentBalance, cryptoAddress.getPrimaryCoin()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        // Process all Tokens.
        for(int offset = 0; ; offset += 50) {
            String url = "https://api.better-call.dev/v1/account/" + networkString + "/" + cryptoAddress.address + "/token_balances?size=50&offset=" + offset;
            String status = processTokensBalance(url, cryptoAddress, currentBalanceArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        return currentBalanceArrayList;
    }

    public String processTokensBalance(String url, CryptoAddress cryptoAddress, ArrayList<AssetQuantity> currentBalanceArrayList) {
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataTokenJSON = WebUtil.get(url);
        if(addressDataTokenJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            // Tokens
            JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
            JSONArray jsonTokenArray = jsonToken.getJSONArray("balances");
            for(int i = 0; i < jsonTokenArray.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject tokenData = jsonTokenArray.getJSONObject(i);

                if(!tokenData.has("symbol") || !tokenData.has("name") || !tokenData.has("decimals") || !tokenData.has("contract")) {
                    // These are "invalid" tokens.
                    continue;
                }

                String name = tokenData.getString("symbol");
                String display_name = tokenData.getString("name");
                int scale = tokenData.getInt("decimals");
                String id = tokenData.getString("contract");

                Token token = TokenManager.getTokenManagerFromKey("XTZTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);

                BigDecimal b = new BigDecimal(tokenData.getString("balance"));
                b = b.movePointLeft(token.getScale());
                String currentTokenBalance = b.toPlainString();
                currentBalanceArrayList.add(new AssetQuantity(currentTokenBalance, token));
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.tzstats.com";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Florencenet) {
            baseURL = "https://api.florence.tzstats.com";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Granadanet) {
            baseURL = "https://api.granada.tzstats.com";
        }
        else {
            return null;
        }

        String networkString;
        if(cryptoAddress.network.isMainnet()) {
            networkString = "mainnet";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Florencenet) {
            networkString = "florencenet";
        }
        else if(cryptoAddress.network instanceof XTZ_Testnet_Granadanet) {
            networkString = "granadanet";
        }
        else {
            return null;
        }

        ArrayList<Transaction> transactionReceiveArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionSendArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionCreateArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokenArrayList = new ArrayList<>();

        // We can request up to 50000 from the table API, which is much more than our highest setting currently, so we don't need pagination.
        int LIMIT = getMaxTransactions();

        String urlReceive = baseURL + "/tables/op?receiver=" + cryptoAddress.address + "&limit=" + LIMIT + "&columns=is_success,reward,deposit,volume,time,type,fee,burned";
        String statusReceive = processReceive(urlReceive, cryptoAddress, transactionReceiveArrayList);
        if(ERROR.equals(statusReceive)) { return null; }

        String urlSend = baseURL + "/tables/op?sender=" + cryptoAddress.address + "&limit=" + LIMIT + "&columns=is_success,reward,deposit,volume,time,type,fee,burned";
        String statusSend = processSend(urlSend, cryptoAddress, transactionSendArrayList);
        if(ERROR.equals(statusSend)) { return null; }

        String urlCreate = baseURL + "/tables/op?creator=" + cryptoAddress.address + "&limit=" + LIMIT + "&columns=is_success,reward,deposit,volume,time,type,fee,burned";
        String statusCreate = processCreate(urlCreate, cryptoAddress, transactionCreateArrayList);
        if(ERROR.equals(statusCreate)) { return null; }

        // Process all tokens.
        String lastID = "";
        for(;;) {
            String url = "https://api.better-call.dev/v1/tokens/" + networkString + "/transfers/" + cryptoAddress.address + "?size=10&last_id=" + lastID;
            lastID = processTokens(url, cryptoAddress, transactionTokenArrayList);

            if(ERROR.equals(lastID)) {
                return null;
            }
            else if(DONE.equals(lastID)) {
                break;
            }
        }

        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = shouldIncludeTokens(cryptoAddress) ? 4 : 3;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionReceiveArrayList.subList(0, Math.min(splitMax, transactionReceiveArrayList.size())));
        transactionArrayList.addAll(transactionSendArrayList.subList(0, Math.min(splitMax, transactionSendArrayList.size())));
        transactionArrayList.addAll(transactionCreateArrayList.subList(0, Math.min(splitMax, transactionCreateArrayList.size())));
        transactionArrayList.addAll(transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())));

        transactionReceiveArrayList.subList(0, Math.min(splitMax, transactionReceiveArrayList.size())).clear();
        transactionSendArrayList.subList(0, Math.min(splitMax, transactionSendArrayList.size())).clear();
        transactionCreateArrayList.subList(0, Math.min(splitMax, transactionCreateArrayList.size())).clear();
        transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())).clear();

        while(transactionReceiveArrayList.size() + transactionSendArrayList.size() + transactionCreateArrayList.size() + transactionTokenArrayList.size() > 0) {
            if(transactionReceiveArrayList.size() > 0) {
                transactionArrayList.add(transactionReceiveArrayList.get(0));
                transactionReceiveArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionSendArrayList.size() > 0) {
                transactionArrayList.add(transactionSendArrayList.get(0));
                transactionSendArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionCreateArrayList.size() > 0) {
                transactionArrayList.add(transactionCreateArrayList.get(0));
                transactionCreateArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionTokenArrayList.size() > 0) {
                transactionArrayList.add(transactionTokenArrayList.get(0));
                transactionTokenArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }

    public String processReceive(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionReceiveArrayList) {
        String addressDataReceiveJSON = WebUtil.get(url);
        if(addressDataReceiveJSON == null) {
            return ERROR;
        }

        try {
            // Receive
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonReceiveData = new JSONArray(addressDataReceiveJSON);

            for(int i = 0; i < jsonReceiveData.length(); i++) {
                JSONArray jsonTransaction = jsonReceiveData.getJSONArray(i);

                boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                String block_time = jsonTransaction.getString(4);
                Date block_time_date = DateTimeUtil.parseMilliseconds(block_time);

                BigDecimal value = new BigDecimal(jsonTransaction.getString(3));

                if(isSuccess && value.compareTo(BigDecimal.ZERO) > 0) {
                    String balance_diff_s = value.toPlainString();
                    transactionReceiveArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s, cryptoAddress.getPrimaryCoin()), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionReceiveArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return DONE;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public String processSend(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionSendArrayList) {
        String addressDataSendJSON = WebUtil.get(url);
        if(addressDataSendJSON == null) {
            return ERROR;
        }

        try {
            // Send
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonSendData = new JSONArray(addressDataSendJSON);

            for(int i = 0; i < jsonSendData.length(); i++) {
                JSONArray jsonTransaction = jsonSendData.getJSONArray(i);

                boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                String block_time = jsonTransaction.getString(4);
                Date block_time_date = DateTimeUtil.parseMilliseconds(block_time);

                BigDecimal value = new BigDecimal(jsonTransaction.getString(3));

                BigDecimal fee = new BigDecimal(jsonTransaction.getString(6));
                BigDecimal burned = new BigDecimal(jsonTransaction.getString(7));

                String transactionName;
                String feeName;
                String burnName;

                String type = jsonTransaction.getString(5);
                if("transaction".equals(type)) {
                    transactionName = "Transaction";
                    feeName = "Transaction Fee";
                    burnName = "Burned For Transaction";
                }
                else if("reveal".equals(type)) {
                    transactionName = "Public Key Reveal";
                    feeName = "Public Key Reveal Fee";
                    burnName = "Burned For Public Key Reveal";
                }
                else if("delegation".equals(type)) {
                    transactionName = "Delegate";
                    feeName = "Delegate Fee";
                    burnName = "Burned For Delegate";
                    value = BigDecimal.ZERO;
                }
                else if("origination".equals(type)) {
                    transactionName = "Origination";
                    feeName = "Origination Fee";
                    burnName = "Burned For Origination";
                }
                else if("activate_account".equals(type)) {
                    // This is duplicated in "Receive", so skip it here.
                    continue;
                }
                else {
                    // Deal with any other kinds?
                    // Full List:
                    /*
                        activate_account
                        double_baking_evidence
                        double_endorsement_evidence
                        seed_nonce_revelation
                        transaction
                        origination
                        delegation
                        reveal
                        endorsement
                        proposals
                        ballot
                        bake (implicit, no hash, block header event op_n = -1)
                        unfreeze (implicit, no hash, block header event op_n = -1)
                        seed_slash (implicit, no hash, block header event op_n = -1)
                        airdrop (implicit, no hash, protocol upgrade event op_n = -2)
                        invoice (implicit, no hash, protocol upgrade event op_n = -2)
                        migration (implicit, no hash, protocol upgrade event op_n = -2)
                    */
                    //Log.e("Crypto Buddy R", "Type = " + type);
                    continue;
                }

                if(isSuccess && value.compareTo(BigDecimal.ZERO) > 0) {
                    String balance_diff_s = value.toPlainString();
                    transactionSendArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, cryptoAddress.getPrimaryCoin()), null, new Timestamp(block_time_date),transactionName));
                    if(transactionSendArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    String fee_s = fee.toPlainString();
                    transactionSendArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),feeName));
                    if(transactionSendArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                if(burned.compareTo(BigDecimal.ZERO) > 0) {
                    String burned_s = burned.toPlainString();
                    transactionSendArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(burned_s, cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),burnName));
                    if(transactionSendArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return DONE;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public String processCreate(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionCreateArrayList) {
        String addressDataCreateJSON = WebUtil.get(url);
        if(addressDataCreateJSON == null) {
            return ERROR;
        }

        try {
            // Create
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonCreateData = new JSONArray(addressDataCreateJSON);

            for(int i = 0; i < jsonCreateData.length(); i++) {
                JSONArray jsonTransaction = jsonCreateData.getJSONArray(i);

                // Fee and Burned are always taken into account, whether or not we succeed.
                //boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                String block_time = jsonTransaction.getString(4);
                Date block_time_date = DateTimeUtil.parseMilliseconds(block_time);

                BigDecimal fee = new BigDecimal(jsonTransaction.getString(6));
                BigDecimal burned = new BigDecimal(jsonTransaction.getString(7));

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    String fee_s = fee.toPlainString();
                    transactionCreateArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),"Storage Fee"));
                    if(transactionCreateArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                if(burned.compareTo(BigDecimal.ZERO) > 0) {
                    String burned_s = burned.toPlainString();
                    transactionCreateArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(burned_s, cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),"Burned For Storage"));
                    if(transactionCreateArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return DONE;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public String processTokens(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionTokenArrayList) {
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataTokenJSON = WebUtil.get(url);
        if(addressDataTokenJSON == null) {
            return ERROR;
        }

        try {
            String lastID = DONE;

            // Tokens
            JSONObject jsonTokenData = new JSONObject(addressDataTokenJSON);
            JSONArray jsonTokenArray = jsonTokenData.getJSONArray("transfers");
            for(int i = 0; i < jsonTokenArray.length(); i++) {
                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = jsonTokenData.getString("last_id");

                JSONObject jsonTransaction = jsonTokenArray.getJSONObject(i);

                boolean isSuccess = "applied".equals(jsonTransaction.getString("status"));
                if(!isSuccess) {
                    continue;
                }

                JSONObject tokenInfo = jsonTransaction.getJSONObject("token");
                if(!tokenInfo.has("symbol") || !tokenInfo.has("name") || !tokenInfo.has("decimals") || !tokenInfo.has("contract")) {
                    // These are "invalid" tokens.
                    continue;
                }

                String name = tokenInfo.getString("symbol");
                String display_name = tokenInfo.getString("name");
                int scale = tokenInfo.getInt("decimals");
                String id = tokenInfo.getString("contract");

                Token token = TokenManager.getTokenManagerFromKey("XTZTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);

                String action;

                String from = jsonTransaction.getString("from");
                String to = jsonTransaction.getString("to");

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                if(cryptoAddress.matchesAddress(from)) {
                    action = "Send";
                }
                else if(cryptoAddress.matchesAddress(to)) {
                    action = "Receive";
                }
                else {
                    // Assume there is nothing else to process.
                    continue;
                }

                String block_time = jsonTransaction.getString("timestamp");
                Date block_time_date = DateTimeUtil.parseStandard(block_time);

                BigDecimal value = new BigDecimal(jsonTransaction.getString("amount"));
                value = value.movePointLeft(token.getScale());

                if(value.compareTo(BigDecimal.ZERO) > 0) {
                    String balance_diff_s = value.toPlainString();
                    transactionTokenArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionTokenArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
