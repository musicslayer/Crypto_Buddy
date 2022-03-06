package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.GenericAsset;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class Coinbase extends ExchangeAPI {
    public String getName() { return "Coinbase"; }
    public String getDisplayName() { return "Coinbase REST API V2"; }

    public AuthUtil.OAuthToken oAuthToken;

    public boolean isSupported(Exchange exchange) {
        return "Coinbase".equals(exchange.getKey());
    }

    private AuthUtil.OAuthInfo getOAuthInfo() {
        String authURLBase = "https://www.coinbase.com/oauth/authorize/";
        String tokenURLBase = "https://api.coinbase.com/oauth/token/";
        String client_id = BuildConfig.coinbase_client_id;
        String client_secret = BuildConfig.coinbase_client_secret;
        String redirect_uri = "https://musicslayer.github.io/";
        String response_type = "code";
        String grant_type = "authorization_code";
        String[] scopes = new String[] {"wallet:transactions:read", "wallet:accounts:read"};

        return new AuthUtil.OAuthInfo(authURLBase, tokenURLBase, client_id, client_secret, redirect_uri, response_type, grant_type, scopes);
    }

    public void authorize(Context context, AuthUtil.AuthorizationListener L) {
        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                Coinbase.this.oAuthToken = oAuthToken;
                L.onAuthorization();
            }
        };

        AuthUtil.authorizeOAuthBrowser(context, getOAuthInfo(), L_OAuth);
    }

    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {
        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                Coinbase.this.oAuthToken = oAuthToken;
                L.onAuthorization();
            }
        };

        AuthUtil.restoreListenersBrowser(context, getOAuthInfo(), L_OAuth);
    }

    public boolean isAuthorized() {
        return oAuthToken != null && oAuthToken.isAuthorized();
    }

    public String getAuthorizationInfo() {
        if(oAuthToken == null) {
            return "[Null OAuth Token]";
        }
        else {
            return oAuthToken.getSafeInfo();
        }
    }

    public String getWithToken(String url) {
        ArrayList<String> keyNameArrayList = new ArrayList<>();
        keyNameArrayList.add("Authorization");
        keyNameArrayList.add("CB-VERSION");

        ArrayList<String> keyArrayList = new ArrayList<>();
        keyArrayList.add("Bearer " + oAuthToken.getToken());
        keyArrayList.add("2022-03-04");

        return WebUtil.get(url, keyNameArrayList, keyArrayList);
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoExchange cryptoExchange) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String url = "https://api.coinbase.com/v2/accounts?limit=300";
        for(;;) {
            url = processBalance(url, currentBalanceArrayList);

            if(ERROR.equals(url)) {
                return null;
            }
            else if(DONE.equals(url)) {
                break;
            }
        }

        return currentBalanceArrayList;
    }

    public String processBalance(String url, ArrayList<AssetQuantity> currentBalanceArrayList) {
        String addressDataJSON = getWithToken(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String nextURL = DONE;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject jsonPage = json.getJSONObject("pagination");
            if(jsonPage.has("next_uri") && !jsonPage.getString("next_uri").equals("null")) {
                nextURL = jsonPage.getString("next_uri");
            }

            JSONArray accounts = json.getJSONArray("data");
            for(int i = 0; i < accounts.length(); i++) {
                JSONObject account = accounts.getJSONObject(i);

                // Don't bother with accounts that have not been updated (i.e. no transactions took place and the balance should be zero).
                String created_at = account.getString("created_at");
                String updated_at = account.getString("updated_at");
                if(created_at.equals(updated_at)) {
                    continue;
                }

                JSONObject balance = account.getJSONObject("balance");
                BigDecimal value = new BigDecimal(balance.getString("amount"));

                JSONObject currency = account.getJSONObject("currency");
                String type = currency.getString("type");
                if(!"fiat".equals(type) && !"crypto".equals(type)) {
                    // What is this? Just skip it for now.
                    continue;
                }

                // Just create a completely generic asset.
                String key = currency.getString("code");
                String name = key;
                String display_name = currency.getString("name");
                int scale = currency.getInt("exponent");
                Asset asset = GenericAsset.createGenericAsset(key, name, display_name, scale, null);

                currentBalanceArrayList.add(new AssetQuantity(value.toPlainString(), asset));
            }

            return nextURL;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public ArrayList<Transaction> getTransactions(CryptoExchange cryptoExchange) {
        // The strategy is to look at all the accounts like in "getCurrentBalance", and then for any account that has been updated, search for transactions.
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String url = "https://api.coinbase.com/v2/accounts?limit=300";
        for(;;) {
            url = processAllTransactions(url, transactionArrayList);

            if(ERROR.equals(url)) {
                return null;
            }
            else if(DONE.equals(url)) {
                break;
            }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processAllTransactions(String url, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = getWithToken(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String nextURL = DONE;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject jsonPage = json.getJSONObject("pagination");
            if(jsonPage.has("next_uri") && !jsonPage.getString("next_uri").equals("null")) {
                nextURL = jsonPage.getString("next_uri");
            }

            JSONArray accounts = json.getJSONArray("data");
            for(int i = 0; i < accounts.length(); i++) {
                JSONObject account = accounts.getJSONObject(i);

                // Don't bother with accounts that have not been updated (i.e. no transactions took place and the balance should be zero).
                String created_at = account.getString("created_at");
                String updated_at = account.getString("updated_at");
                if(created_at.equals(updated_at)) {
                    continue;
                }

                JSONObject currency = account.getJSONObject("currency");
                String type = currency.getString("type");
                if(!"fiat".equals(type) && !"crypto".equals(type)) {
                    // What is this? Just skip it for now.
                    continue;
                }

                // Just create a completely generic asset.
                String key = currency.getString("code");
                String name = key;
                String display_name = currency.getString("name");
                int scale = currency.getInt("exponent");
                Asset asset = GenericAsset.createGenericAsset(key, name, display_name, scale, null);

                String id = account.getString("id");

                // Within transactions, we must now process them, including potential pagination.
                String transactionUrl = "https://api.coinbase.com/v2/accounts/" + id + "/transactions?limit=300";
                for(;;) {
                    // Pass in asset because we don't have enough info to reconstruct it from transaction data.
                    transactionUrl = processTransaction(transactionUrl, transactionArrayList, asset);

                    if(ERROR.equals(transactionUrl)) {
                        return null;
                    }
                    else if(DONE.equals(transactionUrl)) {
                        break;
                    }
                }
            }

            return nextURL;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    private String processTransaction(String transactionUrl, ArrayList<Transaction> transactionArrayList, Asset asset) {
        String transactionDataJSON = getWithToken(transactionUrl);
        if(transactionDataJSON == null) {
            return ERROR;
        }

        try {
            String nextTransactionUrl = DONE;

            JSONObject json = new JSONObject(transactionDataJSON);
            JSONObject jsonPage = json.getJSONObject("pagination");
            if(jsonPage.has("next_uri") && !jsonPage.getString("next_uri").equals("null")) {
                nextTransactionUrl = jsonPage.getString("next_uri");
            }

            JSONArray transactions = json.getJSONArray("data");
            for(int i = 0; i < transactions.length(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);

                JSONObject details = transaction.getJSONObject("details");
                String infoTitle = details.getString("title");
                String infoSubtitle = details.getString("subtitle");
                String info = infoTitle + " (" + infoSubtitle + ")";

                String block_time = transaction.getString("created_at");
                Date block_time_date = DateTimeUtil.parseStandard(block_time);

                JSONObject amount = transaction.getJSONObject("amount");
                BigDecimal value = new BigDecimal(amount.getString("amount"));

                String action;
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    action = "Receive";
                }
                else if (value.compareTo(BigDecimal.ZERO) < 0) {
                    value = value.negate();
                    action = "Send";
                }
                else {
                    // If nothing was sent either way, just skip this.
                    continue;
                }

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(value.toPlainString(), asset), null, new Timestamp(block_time_date),info));
                if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return nextTransactionUrl;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}
