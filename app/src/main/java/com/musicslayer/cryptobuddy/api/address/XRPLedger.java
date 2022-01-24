package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.XRP;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.XRP_Devnet;
import com.musicslayer.cryptobuddy.asset.network.XRP_Testnet;
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

public class XRPLedger extends AddressAPI {
    public String getName() { return "XRPLedger"; }
    public String getDisplayName() { return "XRPLedger HTTP API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "XRP".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://xrplcluster.com";
        }
        else if(cryptoAddress.network instanceof XRP_Testnet) {
            baseURL = "https://s.altnet.rippletest.net:51234";
        }
        else if(cryptoAddress.network instanceof XRP_Devnet) {
            baseURL = "https://s.devnet.rippletest.net:51234";
        }
        else {
            return null;
        }

        String body = "{" +
                "\"method\": \"account_info\"," +
                "\"params\": [" +
                "{" +
                "\"account\": \"" + cryptoAddress.address + "\"" +
                "}" +
                "]" +
                "}";
        String addressDataJSON = RESTUtil.post(baseURL, body);

        if(addressDataJSON == null) {
            return null;
        }

        try {
            // XRP
            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject result = json.getJSONObject("result");

            String currentBalance;
            if(result.has("account_data")) {
                BigDecimal b = new BigDecimal(result.getJSONObject("account_data").getString("Balance"));
                b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                currentBalance = b.toPlainString();
            }
            else {
                currentBalance = "0";
            }

            // Subtract the 20 used to create the account.
            //b = b.subtract(BigDecimal.valueOf(20));

            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new XRP()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String bodyTokensL = "{" +
                    "\"method\": \"gateway_balances\"," +
                    "\"params\": [" +
                    "{" +
                    "\"account\": \"" + cryptoAddress.address + "\"" +
                    "}" +
                    "]" +
                    "}";
            String addressDataTokensLJSON = RESTUtil.post(baseURL, bodyTokensL);

            String bodyTokens = "{" +
                    "\"method\": \"account_lines\"," +
                    "\"params\": [" +
                    "{" +
                    "\"account\": \"" + cryptoAddress.address + "\"" +
                    "}" +
                    "]" +
                    "}";
            String addressDataTokensJSON = RESTUtil.post(baseURL, bodyTokens);

            if(addressDataTokensLJSON == null || addressDataTokensJSON == null) {
                return null;
            }

            try {
                // Token Liabilities
                JSONObject jsonTokenL = new JSONObject(addressDataTokensLJSON);
                if(jsonTokenL.has("result")) {
                    JSONObject results = jsonTokenL.getJSONObject("result");
                    if(results.has("obligations")) {
                        JSONObject obligations = results.getJSONObject("obligations");
                        JSONArray names = obligations.names();
                        for(int i = 0; i < names.length(); i++) {
                            String name = names.getString(i);
                            BigDecimal value = new BigDecimal(obligations.getString(name)).negate();

                            String display_name = name;
                            int scale = 15; // Arbitrary fixed value for all XRP Tokens
                            String id = cryptoAddress.address + "_" + name; // This account is the issuer.
                            String key = id;

                            Token token = TokenManager.getTokenManagerFromKey("XRPTokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                            currentBalanceArrayList.add(new AssetQuantity(value.toPlainString(), token));
                        }
                    }
                }

                // Token Balances
                JSONObject jsonToken = new JSONObject(addressDataTokensJSON);
                JSONObject result = jsonToken.getJSONObject("result");
                if(result.has("lines")) {
                    JSONArray jsonTokenArray = result.getJSONArray("lines");
                    for(int i = 0; i < jsonTokenArray.length(); i++) {
                        JSONObject tokenInfo = jsonTokenArray.getJSONObject(i);

                        BigDecimal tokenBalance = new BigDecimal(tokenInfo.getString("balance"));
                        boolean isLiability = tokenBalance.compareTo(BigDecimal.ZERO) < 0;

                        if(isLiability) {
                            // These were dealt with above.
                            continue;
                        }

                        String name = tokenInfo.getString("currency");
                        String display_name = name;
                        int scale = 15; // Arbitrary fixed value for all XRP Tokens
                        String id = tokenInfo.getString("account") + "_" + name;
                        String key = id;

                        Token token = TokenManager.getTokenManagerFromKey("XRPTokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                        currentBalanceArrayList.add(new AssetQuantity(tokenBalance.toPlainString(), token));
                    }
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
            baseURL = "https://xrplcluster.com";
        }
        else if(cryptoAddress.network instanceof XRP_Testnet) {
            baseURL = "https://s.altnet.rippletest.net:51234";
        }
        else if(cryptoAddress.network instanceof XRP_Devnet) {
            baseURL = "https://s.devnet.rippletest.net:51234";
        }
        else {
            return null;
        }

        String marker = "";
        for(;;) {
            String markerString = marker.isEmpty() ? "" : "\"marker\": " + marker + ", ";

            String body = "{" +
                    "\"method\": \"account_tx\"," +
                    "\"params\": [" +
                    "{" +
                    "\"limit\": 1000, " + markerString + "\"account\": \"" + cryptoAddress.address + "\"" +
                    "}" +
                    "]" +
                    "}";
            marker = processTransfers(baseURL, body, cryptoAddress, transactionArrayList);

            if(ERROR.equals(marker)) {
                return null;
            }
            else if(DONE.equals(marker)) {
                break;
            }
        }

        return transactionArrayList;
    }

    public String processTransfers(String url, String body, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.post(url, body);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String marker = DONE;

            // Add account creation transaction (20 XRP).
            //transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity("20", cryptoAddress.crypto), null, new Timestamp(null), "", "Account Creation Fee"));

            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject result = json.getJSONObject("result");

            // If the marker value is present, then we are not done yet.
            if(result.has("marker")) {
                marker = result.getString("marker");
            }

            JSONArray jsonData = result.getJSONArray("transactions");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                JSONObject meta = jsonTransaction.getJSONObject("meta");
                JSONObject tx = jsonTransaction.getJSONObject("tx");

                BigInteger block_time = new BigInteger(tx.getString("date"));
                //The Ripple Epoch is 946684800 seconds after the Unix Epoch
                double block_time_d = (block_time.doubleValue() + 946684800) * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String type = tx.getString("TransactionType");

                BigDecimal fee;
                if("Payment".equals(type) && cryptoAddress.matchesAddress(tx.getString("Destination"))) {
                    fee = BigDecimal.ZERO;
                }
                else {
                    fee = new BigDecimal(tx.getString("Fee"));
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                if(!"tesSUCCESS".equals(meta.getString("TransactionResult"))) {
                    // The transaction failed. Don't process anything else after the fee.
                    continue;
                }

                String action;
                if("OfferCancel".equals(type) || "OfferCreate".equals(type) || "TrustSet".equals(type) || "AccountSet".equals(type)) {
                    // Nothing else to process.
                }
                else if("Payment".equals(type)) {
                    if(cryptoAddress.matchesAddress(tx.getString("Destination"))) {
                        action = "Receive";
                    }
                    else {
                        action = "Send";
                    }

                    Crypto crypto;
                    String amount;

                    String amountData = meta.getString("delivered_amount");
                    // Either it will just be a string for XRP, or a JSONObject for a token.
                    try {
                        JSONObject tokenData = new JSONObject(amountData);

                        String name = tokenData.getString("currency");
                        String display_name = name;
                        int scale = 15; // Arbitrary fixed value for XRP Tokens
                        String id = tokenData.getString("issuer") + "_" + name;
                        String key = id;

                        crypto = TokenManager.getTokenManagerFromKey("XRPTokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                        amount = tokenData.getString("value");

                        if(!shouldIncludeTokens(cryptoAddress)) {
                            continue;
                        }
                    }
                    catch(org.json.JSONException ignored) {
                        crypto = cryptoAddress.getCrypto();
                        amount = new BigDecimal(amountData).movePointLeft(crypto.getScale()).toPlainString();
                    }

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return marker;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
