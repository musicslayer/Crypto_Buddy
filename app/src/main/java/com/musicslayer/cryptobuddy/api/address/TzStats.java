package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.coin.XTZ;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.XTZ_Testnet_Florencenet;
import com.musicslayer.cryptobuddy.asset.network.XTZ_Testnet_Granadanet;
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

public class TzStats extends AddressAPI {
    public String getName() { return "TzStats"; }
    public String getDisplayName() { return "TzStats & Better Call Dev REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "XTZ".equals(cryptoAddress.getCrypto().getName());
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

        String addressDataJSON = RESTUtil.get(baseURL + "/explorer/account/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            // Account may not be active, so say 0 Tezos.
            // We really need a better way to check this case.
            currentBalanceArrayList.add(new AssetQuantity("0", new XTZ()));
            return currentBalanceArrayList;
        }

        try {
            // XTZ
            JSONObject json = new JSONObject(addressDataJSON);
            String currentBalance = new BigDecimal(json.getString("spendable_balance")).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new XTZ()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressDataTokenJSON = RESTUtil.get("https://api.better-call.dev/v1/account/" + networkString + "/" + cryptoAddress.address + "/token_balances?size=50");
            String addressDataTokenJSON2 = RESTUtil.get("https://api.better-call.dev/v1/account/" + networkString + "/" + cryptoAddress.address + "/token_balances?size=50&offset=50");
            if(addressDataTokenJSON == null || addressDataTokenJSON2 == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONArray("balances");
                for(int i = 0; i < jsonTokenArray.length(); i++) {
                    JSONObject tokenData = jsonTokenArray.getJSONObject(i);

                    if(!tokenData.has("symbol") || !tokenData.has("name") || !tokenData.has("decimals") || !tokenData.has("contract")) {
                        // These are "invalid" tokens.
                        continue;
                    }

                    String name = tokenData.getString("symbol");
                    String display_name = tokenData.getString("name");
                    int scale = tokenData.getInt("decimals");
                    String id = tokenData.getString("contract");

                    Token token = TokenManager.getTokenManagerFromKey("XTZTokenManager").getOrCreateToken(name, name, display_name, scale, id);

                    BigDecimal b = new BigDecimal(tokenData.getString("balance"));
                    b = b.movePointLeft(token.getScale());
                    String currentTokenBalance = b.toPlainString();
                    currentBalanceArrayList.add(new AssetQuantity(currentTokenBalance, token));
                }

                JSONObject jsonToken2 = new JSONObject(addressDataTokenJSON2);
                JSONArray jsonTokenArray2 = jsonToken2.getJSONArray("balances");
                for(int i = 0; i < jsonTokenArray2.length(); i++) {
                    JSONObject tokenData = jsonTokenArray2.getJSONObject(i);

                    if(!tokenData.has("symbol") || !tokenData.has("name") || !tokenData.has("decimals") || !tokenData.has("contract")) {
                        // These are "invalid" tokens.
                        continue;
                    }

                    String name = tokenData.getString("symbol");
                    String display_name = tokenData.getString("name");
                    int scale = tokenData.getInt("decimals");
                    String id = tokenData.getString("contract");

                    Token token = TokenManager.getTokenManagerFromKey("XTZTokenManager").getOrCreateToken(name, name, display_name, scale, id);

                    BigDecimal b = new BigDecimal(tokenData.getString("balance"));
                    b = b.movePointLeft(token.getScale());
                    String currentTokenBalance = b.toPlainString();
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

        String addressDataReceiveJSON = RESTUtil.get(baseURL + "/tables/op?receiver=" + cryptoAddress.address + "&limit=3000&columns=is_success,reward,deposit,volume,time,type,fee,burned");
        String addressDataSendJSON = RESTUtil.get(baseURL + "/tables/op?sender=" + cryptoAddress.address + "&limit=3000&columns=is_success,reward,deposit,volume,time,type,fee,burned");
        String addressDataCreateJSON = RESTUtil.get(baseURL + "/tables/op?creator=" + cryptoAddress.address + "&limit=3000&columns=is_success,reward,deposit,volume,time,type,fee,burned");
        if(addressDataReceiveJSON == null || addressDataSendJSON == null || addressDataCreateJSON == null) {
            return null;
        }

        try {
            // Receive
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonReceiveData = new JSONArray(addressDataReceiveJSON);

            for(int i = 0; i < jsonReceiveData.length(); i++) {
                JSONArray jsonTransaction = jsonReceiveData.getJSONArray(i);

                boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                BigDecimal reward = new BigDecimal(jsonTransaction.getString(1));
                if(reward.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Reward");
                }

                BigDecimal deposit = new BigDecimal(jsonTransaction.getString(2));
                if(deposit.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Deposit");
                }

                Date block_time_date = new Date(new BigInteger(jsonTransaction.getString(4)).longValue());
                BigDecimal value = new BigDecimal(jsonTransaction.getString(3));

                if(isSuccess && value.compareTo(BigDecimal.ZERO) > 0) {
                    String balance_diff_s = value.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }

            // Send
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonSendData = new JSONArray(addressDataSendJSON);

            for(int i = 0; i < jsonSendData.length(); i++) {
                JSONArray jsonTransaction = jsonSendData.getJSONArray(i);

                boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                BigDecimal reward = new BigDecimal(jsonTransaction.getString(1));
                if(reward.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Reward");
                }
                BigDecimal deposit = new BigDecimal(jsonTransaction.getString(2));
                if(deposit.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Deposit");
                }

                Date block_time_date = new Date(new BigInteger(jsonTransaction.getString(4)).longValue());
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
                    Log.e("Crypto Buddy R", "Type = " + type);
                    continue;
                }

                if(isSuccess && value.compareTo(BigDecimal.ZERO) > 0) {
                    String balance_diff_s = value.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),transactionName));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    String fee_s = fee.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),feeName));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                if(burned.compareTo(BigDecimal.ZERO) > 0) {
                    String burned_s = burned.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(burned_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),burnName));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }

            // Create
            // is_success, reward, deposit, volume, time, type, fee, burned
            JSONArray jsonCreateData = new JSONArray(addressDataCreateJSON);

            for(int i = 0; i < jsonCreateData.length(); i++) {
                JSONArray jsonTransaction = jsonCreateData.getJSONArray(i);

                // Fee and Burned are always taken into account, whether or not we succeed.
                //boolean isSuccess = "1".equals(jsonTransaction.getString(0));

                BigDecimal reward = new BigDecimal(jsonTransaction.getString(1));
                if(reward.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Reward");
                }
                BigDecimal deposit = new BigDecimal(jsonTransaction.getString(2));
                if(deposit.compareTo(BigDecimal.ZERO) > 0) {
                    Log.e("Crypto Buddy R", "Deposit");
                }

                Date block_time_date = new Date(new BigInteger(jsonTransaction.getString(4)).longValue());

                BigDecimal fee = new BigDecimal(jsonTransaction.getString(6));
                BigDecimal burned = new BigDecimal(jsonTransaction.getString(7));

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    String fee_s = fee.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Storage Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                if(burned.compareTo(BigDecimal.ZERO) > 0) {
                    String burned_s = burned.toPlainString();
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(burned_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Burned For Storage"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressDataTokenJSON = RESTUtil.get("https://api.better-call.dev/v1/tokens/" + networkString + "/transfers/" + cryptoAddress.address + "?size=10");
            if(addressDataTokenJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonTokenData = new JSONObject(addressDataTokenJSON);
                JSONArray jsonTokenArray = jsonTokenData.getJSONArray("transfers");
                for(int i = 0; i < jsonTokenArray.length(); i++) {
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

                    Token token = TokenManager.getTokenManagerFromKey("XTZTokenManager").getOrCreateToken(name, name, display_name, scale, id);

                    String action;

                    String from = jsonTransaction.getString("from");
                    String to = jsonTransaction.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    if(cryptoAddress.address.equalsIgnoreCase(from)) {
                        action = "Send";
                    }
                    else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                        action = "Receive";
                    }
                    else {
                        // Assume there is nothing else to process.
                        continue;
                    }

                    String block_time = jsonTransaction.getString("timestamp");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date block_time_date = format.parse(block_time);

                    BigDecimal value = new BigDecimal(jsonTransaction.getString("amount"));
                    value = value.movePointLeft(token.getScale());

                    if(value.compareTo(BigDecimal.ZERO) > 0) {
                        String balance_diff_s = value.toPlainString();
                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date),"Transaction"));
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
