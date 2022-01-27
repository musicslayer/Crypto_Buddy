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
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

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

        String addressDataJSON = WebUtil.get(baseURL + "/v2/accounts/" + cryptoAddress.address);
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

                    String tokenData = WebUtil.get(baseURL + "/v2/assets/" + id);
                    JSONObject tokenJSON = new JSONObject(tokenData).getJSONObject("params");

                    String name = tokenJSON.getString("unit-name");
                    String display_name = tokenJSON.getString("name");
                    int scale = tokenJSON.getInt("decimals");

                    Token token = TokenManager.getTokenManagerFromKey("AlgoTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);

                    BigDecimal value = new BigDecimal(asset.getString("amount"));
                    value = value.movePointLeft(cryptoAddress.getCrypto().getScale());

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

        String next = "";
        for(;;) {
            String url = baseURL + "/idx2/v2/accounts/" + cryptoAddress.address + "/transactions" + "?limit=1000&next=" + next;
            next = process(url, cryptoAddress, transactionArrayList);

            if(ERROR.equals(next)) {
                return null;
            }
            else if(DONE.equals(next)) {
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

        try {
            String next = DONE;
            // Add account creation transaction (0.1 ALGO).
            //transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity("0.1", cryptoAddress.crypto), null, new Timestamp(null), "", "Account Creation Fee"));

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("transactions");
            for(int i = 0; i < jsonData.length(); i++) {
                // If there is anything to process, store next token to get the next page.
                if(json.has("next-token")) {
                    next = json.getString("next-token");
                }

                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                BigInteger block_time = new BigInteger(jsonTransaction.getString("round-time"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = jsonTransaction.getString("sender");

                // "to" may or may not exist.
                String to = "";
                if(jsonTransaction.has("payment-transaction") && jsonTransaction.getJSONObject("payment-transaction").has("receiver")) {
                    to = jsonTransaction.getJSONObject("payment-transaction").getString("receiver");
                }

                if(jsonTransaction.has("asset-transfer-transaction") && jsonTransaction.getJSONObject("asset-transfer-transaction").has("receiver")) {
                    to = jsonTransaction.getJSONObject("asset-transfer-transaction").getString("receiver");
                }

                String action;
                BigDecimal fee;
                boolean isFrom = false;
                boolean isTo = false;

                // If the address is both form and to, we largely treat it as from but still set the isTo flag.
                // For example, an address that is both still should pay the fee.
                if(cryptoAddress.matchesAddress(from)) {
                    isFrom = true;
                }
                if(cryptoAddress.matchesAddress(to)) {
                    isTo = true;
                }

                if(cryptoAddress.matchesAddress(from)) {
                    action = "Send";
                    fee = new BigDecimal(jsonTransaction.getString("fee"));
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                }
                else {
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                Crypto crypto;
                BigDecimal value;
                if(jsonTransaction.has("payment-transaction")) {
                    // ALGO
                    value = new BigDecimal(jsonTransaction.getJSONObject("payment-transaction").getString("amount"));
                    value = value.movePointLeft(cryptoAddress.getCrypto().getScale());

                    crypto = cryptoAddress.getCrypto();

                    // Also add staking rewards here. Any kind of ALGO transaction triggers reward collection.
                    BigDecimal reward = BigDecimal.ZERO;
                    if(isFrom) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("sender-rewards")));
                    }
                    if(isTo) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("receiver-rewards")));
                    }

                    reward = reward.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String reward_diff_s = reward.toString();

                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(reward_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Staking Reward"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
                else if(jsonTransaction.has("asset-transfer-transaction")) {
                    // Add staking rewards here even if we don't want tokens. Any kind of ALGO transaction triggers reward collection.
                    BigDecimal reward = BigDecimal.ZERO;
                    if(isFrom) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("sender-rewards")));
                    }
                    if(isTo) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("receiver-rewards")));
                    }

                    reward = reward.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String reward_diff_s = reward.toString();

                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(reward_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Staking Reward"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }

                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    // TOKEN
                    String id = jsonTransaction.getJSONObject("asset-transfer-transaction").getString("asset-id");
                    String tokenData = WebUtil.get(baseURL + "/v2/assets/" + id);
                    JSONObject tokenJSON = new JSONObject(tokenData).getJSONObject("params");

                    String name = tokenJSON.getString("unit-name");
                    String display_name = tokenJSON.getString("name");
                    int scale = tokenJSON.getInt("decimals");

                    crypto = TokenManager.getTokenManagerFromKey("AlgoTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);

                    value = new BigDecimal(jsonTransaction.getJSONObject("asset-transfer-transaction").getString("amount"));
                    value = value.movePointLeft(cryptoAddress.getCrypto().getScale());
                }
                else { // application-transaction ?
                    // No transaction, but there could still be a fee and reward.
                    value = BigDecimal.ZERO;
                    crypto = cryptoAddress.getCrypto();

                    BigDecimal reward = BigDecimal.ZERO;
                    if(isFrom) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("sender-rewards")));
                    }
                    if(isTo) {
                        reward = reward.add(new BigDecimal(jsonTransaction.getString("receiver-rewards")));
                    }

                    reward = reward.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String reward_diff_s = reward.toString();

                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(reward_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Staking Reward"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) {
                    continue;
                }

                if(value.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(value.toString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return next;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
