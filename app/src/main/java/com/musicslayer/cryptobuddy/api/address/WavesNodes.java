package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.WAVES;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.WAVES_Stagenet;
import com.musicslayer.cryptobuddy.asset.network.WAVES_Testnet;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;

public class WavesNodes extends AddressAPI {
    public String getName() { return "WavesNodes"; }
    public String getDisplayName() { return "WavesNodes HTTP API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "WAVES".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://nodes.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Testnet) {
            baseURL = "https://nodes-testnet.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Stagenet) {
            baseURL = "https://nodes-stagenet.wavesnodes.com";
        }
        else {
            return null;
        }

        String addressDataJSON = RESTUtil.get(baseURL + "/addresses/balance/" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // WAVES
            JSONObject json = new JSONObject(addressDataJSON);
            BigDecimal b = new BigDecimal(json.getString("balance"));
            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
            currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), new WAVES()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressDataTokenJSON = RESTUtil.get(baseURL + "/assets/balance/" + cryptoAddress.address);
            if(addressDataTokenJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                JSONArray balances = jsonToken.getJSONArray("balances");
                for(int i = 0; i < balances.length(); i++) {
                    JSONObject tokenInfo = balances.getJSONObject(i);

                    Token token;
                    TokenManager tokenManager = TokenManager.getTokenManagerFromKey("WavesTokenManager");
                    try {
                        // issueTransaction may be null
                        JSONObject issueTransaction = tokenInfo.getJSONObject("issueTransaction");

                        String name = issueTransaction.getString("name");
                        String display_name = name;
                        int scale = issueTransaction.getInt("decimals");
                        String id = issueTransaction.getString("id");
                        String key = id;

                        token = tokenManager.getOrCreateToken(key, name, display_name, scale, id);
                    }
                    catch(Exception ignored) {
                        String id = tokenInfo.getString("assetId");
                        token = tokenManager.getOrLookupToken(baseURL,id, "?", "?", 0, id);
                    }

                    BigDecimal bb = new BigDecimal(tokenInfo.getString("balance"));
                    bb = bb.movePointLeft(token.getScale());
                    currentBalanceArrayList.add(new AssetQuantity(bb.toPlainString(), token));
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
            baseURL = "https://nodes.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Testnet) {
            baseURL = "https://nodes-testnet.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Stagenet) {
            baseURL = "https://nodes-stagenet.wavesnodes.com";
        }
        else {
            return null;
        }

        // 1000 is the limit that we can request, so we need pagination.
        String lastID = "";
        for(int page = 0; ; page++) {
            String url = baseURL + "/transactions/address/" + cryptoAddress.address + "/limit/1000?after=" + lastID;
            lastID = processTransfers(url, page, cryptoAddress, transactionArrayList);

            if(lastID == null) {
                return null;
            }
            else if(DONE.equals(lastID)) {
                break;
            }
        }

        return transactionArrayList;
    }

    public String processTransfers(String url, int page, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return null;
        }

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://nodes.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Testnet) {
            baseURL = "https://nodes-testnet.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Stagenet) {
            baseURL = "https://nodes-stagenet.wavesnodes.com";
        }
        else {
            return null;
        }

        try {
            String lastID = DONE;

            JSONArray json = new JSONArray(addressDataJSON);
            JSONArray jsonData = json.getJSONArray(0);

            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                String sender = jsonTransaction.getString("sender");

                String action;

                BigInteger block_time = new BigInteger(jsonTransaction.getString("timestamp"));
                Date block_time_date = new Date(block_time.longValue());

                String feeAssetId = jsonTransaction.getString("feeAssetId");

                Crypto fee_crypto;
                if("null".equals(feeAssetId)) {
                    fee_crypto = cryptoAddress.getCrypto();
                }
                else {
                    fee_crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, feeAssetId, "?", "?", 0, feeAssetId);
                }

                BigDecimal fee;
                if(cryptoAddress.address.equals(sender)) {
                    action = "Send";
                    fee = new BigDecimal(jsonTransaction.getString("fee"));
                    fee = fee.movePointLeft(fee_crypto.getScale());
                }
                else {
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                String fee_s = fee.toPlainString();

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    if(!(fee_crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee_s, fee_crypto), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }

                if(!"succeeded".equals(jsonTransaction.getString("applicationStatus"))) {
                    continue;
                }

                // Payments
                if(jsonTransaction.has("payment")) {
                    JSONArray payment = jsonTransaction.getJSONArray("payment");
                    for(int j = 0; j < payment.length(); j++) {
                        JSONObject p = payment.getJSONObject(j);
                        String assetId = p.getString("assetId");

                        Crypto crypto;
                        if("null".equals(assetId)) {
                            crypto = cryptoAddress.getCrypto();
                        }
                        else {
                            crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, assetId, "?", "?", 0, assetId);
                        }

                        String amount = p.getString("amount");
                        BigDecimal value = new BigDecimal(amount);
                        value = value.movePointLeft(crypto.getScale());
                        String balance_diff_s = value.toString();

                        if(!(crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action("Send"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Payment"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                }

                // Regular transactions
                if(jsonTransaction.has("assetId")) {
                    String assetId = jsonTransaction.getString("assetId");

                    Crypto crypto;
                    if("null".equals(assetId)) {
                        crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, assetId, "?", "?", 0, assetId);
                    }

                    // Transfers.
                    if(jsonTransaction.has("transfers")) {
                        JSONArray transfers = jsonTransaction.getJSONArray("transfers");
                        for(int j = 0; j < transfers.length(); j++) {
                            JSONObject t = transfers.getJSONObject(j);

                            String amount = t.getString("amount");
                            BigDecimal value = new BigDecimal(amount);
                            value = value.movePointLeft(crypto.getScale());
                            String balance_diff_s = value.toString();

                            if(!(crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transfer"));
                                if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                            }
                        }
                    }

                    // Main transaction
                    if(jsonTransaction.has("amount")) {
                        String amount = jsonTransaction.getString("amount");
                        BigDecimal value = new BigDecimal(amount);
                        value = value.movePointLeft(crypto.getScale());
                        String balance_diff_s = value.toString();

                        if(!(crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                }

                // Token Reissues
                // The old token was traded away in a payment above. Here we get a new token.
                if(jsonTransaction.has("stateChanges")) {
                    JSONObject stateChanges = jsonTransaction.getJSONObject("stateChanges");
                    JSONArray reissues = stateChanges.getJSONArray("reissues");
                    for(int j = 0; j < reissues.length(); j++) {
                        JSONObject r = reissues.getJSONObject(j);
                        String assetId = r.getString("assetId");

                        Crypto crypto;
                        if("null".equals(assetId)) {
                            crypto = cryptoAddress.getCrypto();
                        }
                        else {
                            crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, assetId, "?", "?", 0, assetId);
                        }

                        String amount = r.getString("quantity");
                        BigDecimal value = new BigDecimal(amount);
                        value = value.movePointLeft(crypto.getScale());
                        String balance_diff_s = value.toString();

                        if(!(crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date),"Token Reissue"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                }

                // Orders - Do up to 99, and start with 1
                for(int orderIdx = 1; orderIdx < 100; orderIdx++) {
                    String orderName = "order" + orderIdx;
                    if(!jsonTransaction.has(orderName)) {
                        break;
                    }

                    JSONObject orderJSON = jsonTransaction.getJSONObject(orderName);
                    if(!cryptoAddress.address.equals(orderJSON.getString("sender"))) {
                        continue;
                    }

                    JSONObject assetPair = orderJSON.getJSONObject("assetPair");

                    String amount_assetId = assetPair.getString("amountAsset");
                    Crypto amount_crypto;
                    if("null".equals(amount_assetId)) {
                        amount_crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        amount_crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, amount_assetId, "?", "?", 0, amount_assetId);
                    }

                    String price_assetId = assetPair.getString("priceAsset");
                    Crypto price_crypto;
                    if("null".equals(price_assetId)) {
                        price_crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        price_crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, price_assetId, "?", "?", 0, price_assetId);
                    }

                    String amount_action;
                    String price_action;
                    String amount_info;
                    String price_info;
                    if("buy".equals(orderJSON.getString("orderType"))) {
                        amount_action = "Receive";
                        price_action = "Send";
                        amount_info = "Order (Buy)";
                        price_info = "Order (Sell)";
                    }
                    else {
                        amount_action = "Send";
                        price_action = "Receive";
                        amount_info = "Order (Sell)";
                        price_info = "Order (Buy)";
                    }

                    BigDecimal amount_balance = new BigDecimal(orderJSON.getString("amount"));
                    amount_balance = amount_balance.movePointLeft(amount_crypto.getScale());

                    BigDecimal price = new BigDecimal(orderJSON.getString("price"));
                    price = price.movePointLeft(14); // Fixed amount for price???

                    BigDecimal price_balance = amount_balance.multiply(price);

                    // For this, we need to manually truncate instead of the normal rounding.
                    price_balance = price_balance.setScale(price_crypto.getScale(), RoundingMode.DOWN);

                    if(!(amount_crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                        transactionArrayList.add(new Transaction(new Action(amount_action), new AssetQuantity(amount_balance.toPlainString(), amount_crypto), null, new Timestamp(block_time_date),amount_info));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }

                    if(!(price_crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                        transactionArrayList.add(new Transaction(new Action(price_action), new AssetQuantity(price_balance.toPlainString(), price_crypto), null, new Timestamp(block_time_date),price_info));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }

                    // We always pay the fee here.
                    BigDecimal matcher_fee = new BigDecimal(orderJSON.getString("matcherFee"));

                    String matcher_fee_assetId = orderJSON.getString("matcherFeeAssetId");
                    Crypto matcher_fee_crypto;
                    if("null".equals(matcher_fee_assetId)) {
                        matcher_fee_crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        matcher_fee_crypto = TokenManager.getTokenManagerFromKey("WavesTokenManager").getOrLookupToken(baseURL, matcher_fee_assetId, "?", "?", 0, matcher_fee_assetId);
                    }

                    matcher_fee = matcher_fee.movePointLeft(matcher_fee_crypto.getScale());
                    String matcher_fee_s = matcher_fee.toPlainString();

                    if(matcher_fee.compareTo(BigDecimal.ZERO) > 0) {
                        if(!(matcher_fee_crypto instanceof Token) || shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(matcher_fee_s, matcher_fee_crypto), null, new Timestamp(block_time_date),"Transaction Fee"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }
    }
}
