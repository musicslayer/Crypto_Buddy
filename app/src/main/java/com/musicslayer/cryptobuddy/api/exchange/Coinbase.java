package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.GenericAsset;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

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

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoExchange cryptoExchange) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        if(!isAuthorized()) {
            return null;
        }

        String token = oAuthToken.getToken();
        String url = "https://api.coinbase.com/v2/accounts?limit=300";
        for(;;) {
            url = processBalance(url, token, currentBalanceArrayList);

            if(ERROR.equals(url)) {
                return null;
            }
            else if(DONE.equals(url)) {
                break;
            }
        }

        return currentBalanceArrayList;
    }

    public String processBalance(String url, String token, ArrayList<AssetQuantity> currentBalanceArrayList) {
        String addressDataJSON = WebUtil.getWithToken(url, token);
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

                // Don't bother with accounts that have a zero balance.
                JSONObject balance = account.getJSONObject("balance");
                BigDecimal amount = new BigDecimal(balance.getString("amount"));
                if(amount.compareTo(BigDecimal.ZERO) == 0) {
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

                currentBalanceArrayList.add(new AssetQuantity(amount.toPlainString(), asset));
            }

            return nextURL;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public ArrayList<Transaction> getTransactions(CryptoExchange cryptoExchange) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processTransaction(String url, String token, ArrayList<Transaction> transactionArrayList) {
        return DONE;
    }
}
