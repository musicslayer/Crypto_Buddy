package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.ALGO;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.ALGO_Betanet;
import com.musicslayer.cryptobuddy.asset.network.ALGO_Testnet;
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
import java.util.ArrayList;
import java.util.Date;

// TODO We need to update to V2
// https://algoexplorerapi.io/idx2/v2/accounts/W2TVRKTHYB7HDVUGI6E6AVAXDGFT3RJT3XZGB2TVOT53TAISPBO2F5R3HE/transactions?limit=10000
// https://algoexplorerapi.io/idx2/v2/accounts/W2TVRKTHYB7HDVUGI6E6AVAXDGFT3RJT3XZGB2TVOT53TAISPBO2F5R3HE/transactions?limit=99&next=2IfwAAAAAAASAAAA
// This is paged, but also allows us to get a large number directly.

public class AlgoExplorer extends AddressAPI {
    public String getName() { return "AlgoExplorer"; }
    public String getDisplayName() { return "AlgoExplorer REST API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ALGO".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://algoexplorerapi.io";
        }
        else if(cryptoAddress.network instanceof ALGO_Testnet) {
            baseURL = "https://testnet.algoexplorerapi.io";
        }
        else if(cryptoAddress.network instanceof ALGO_Betanet) {
            baseURL = "https://betanet.algoexplorerapi.io";
        }
        else {
            return null;
        }

        String addressDataJSON = RESTUtil.get(baseURL + "/v1/account/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            BigDecimal b = new BigDecimal(json.getString("amount-without-pending-rewards"));
            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());

            // Subtract the account minimum.
            //b = b.subtract(BigDecimal.valueOf(0.1));

            currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), new ALGO()));

            if(shouldIncludeTokens(cryptoAddress)) {
                // Tokens
                JSONArray assets = json.getJSONArray("assets");
                for(int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String id = asset.getString("asset-id");

                    String tokenData = RESTUtil.get("https://algoexplorerapi.io/v1/asset/" + id);
                    JSONObject tokenJSON = new JSONObject(tokenData);

                    String name = tokenJSON.getString("unitname");
                    String display_name = tokenJSON.getString("assetname");
                    int scale = tokenJSON.getInt("decimals");

                    Token token = TokenManager.getTokenManagerFromKey("AlgoTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                    BigDecimal value = new BigDecimal(asset.getString("amount")); // Don't shift
                    currentBalanceArrayList.add(new AssetQuantity(value.toPlainString(), token));
                }
            }
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
            baseURL = "https://algoexplorerapi.io";
        }
        else if(cryptoAddress.network instanceof ALGO_Testnet) {
            baseURL = "https://testnet.algoexplorerapi.io";
        }
        else if(cryptoAddress.network instanceof ALGO_Betanet) {
            baseURL = "https://betanet.algoexplorerapi.io";
        }
        else {
            return null;
        }

/*
        for(int page = 0; ; page++) {
            String url = baseURL + "/v1/account/" + cryptoAddress.address + "/transactions" + "?page=" + page;
            int status = process(url, page, cryptoAddress, transactionArrayList);

            if(status == -1) {
                return null;
            }
            else if(status == 0) {
                break;
            }
        }

 */

        // For now, just do it once. After V2 we can fix this.
        String url = baseURL + "/v1/account/" + cryptoAddress.address + "/transactions";
        String status = process(url, 0, cryptoAddress, transactionArrayList);
        if(status == null) {
            return null;
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String process(String url, int page, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // Add account creation transaction (0.1 ALGO).
            //transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity("0.1", cryptoAddress.crypto), null, new Timestamp(null), "", "Account Creation Fee"));

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("transactions");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                BigInteger block_time = new BigInteger(jsonTransaction.getString("timestamp"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = jsonTransaction.getString("from");

                String action;
                BigDecimal fee;
                boolean isFrom;
                if(cryptoAddress.address.equals(from)) {
                    isFrom = true;
                    action = "Send";
                    fee = new BigDecimal(jsonTransaction.getString("fee"));
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                }
                else {
                    isFrom = false;
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                // If I send something to myself, just reject it!
                if(jsonTransaction.has("payment") && jsonTransaction.getJSONObject("payment").has("to") && jsonTransaction.getJSONObject("payment").getString("to").equals(from)) {
                    continue;
                }

                Crypto crypto;
                BigDecimal value;
                if(jsonTransaction.has("payment")) {
                    // ALGO
                    value = new BigDecimal(jsonTransaction.getJSONObject("payment").getString("amount"));
                    value = value.movePointLeft(cryptoAddress.getCrypto().getScale());

                    crypto = cryptoAddress.getCrypto();

                    // Also add staking rewards here. Any kind of ALGO transaction triggers reward collection.
                    BigDecimal reward;
                    if(isFrom) {
                        reward = new BigDecimal(jsonTransaction.getString("fromrewards"));
                    }
                    else {
                        reward = new BigDecimal(jsonTransaction.getJSONObject("payment").getString("torewards"));
                    }

                    reward = reward.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String reward_diff_s = reward.toString();

                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(reward_diff_s, crypto), null, new Timestamp(block_time_date),"Staking Reward"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
                else if(jsonTransaction.has("curxfer")) {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    // TOKEN
                    String id = jsonTransaction.getJSONObject("curxfer").getString("id");
                    String tokenData = RESTUtil.get("https://algoexplorerapi.io/v1/asset/" + id);
                    JSONObject tokenJSON = new JSONObject(tokenData);

                    String name = tokenJSON.getString("unitname");
                    String display_name = tokenJSON.getString("assetname");
                    int scale = tokenJSON.getInt("decimals");

                    crypto = TokenManager.getTokenManagerFromKey("AlgoTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                    value = new BigDecimal(jsonTransaction.getJSONObject("curxfer").getString("amt")); // Don't shift
                }
                else { // curfrz ?
                    // No transaction, but there could still be a fee and reward.
                    value = BigDecimal.ZERO;
                    crypto = cryptoAddress.getCrypto();

                    BigDecimal reward = BigDecimal.ZERO;
                    if(isFrom) {
                        reward = new BigDecimal(jsonTransaction.getString("fromrewards"));
                    }
                    else {
                        if(jsonTransaction.has("payment") && jsonTransaction.getJSONObject("payment").has("torewards")) {
                            reward = new BigDecimal(jsonTransaction.getJSONObject("payment").getString("torewards"));
                        }
                    }

                    reward = reward.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String reward_diff_s = reward.toString();

                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(reward_diff_s, crypto), null, new Timestamp(block_time_date),"Staking Reward"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }

                if(value.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(value.toString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            /*
            // See if we have more pages. They are indexed from 0 to pagesTotal - 1.
            int pagesTotal = json.getInt("pagesTotal");
            if(page < pagesTotal - 1) {
                return 1;
            }
            else {
                return 0;
            }

             */

            return DONE;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }
    }
}
