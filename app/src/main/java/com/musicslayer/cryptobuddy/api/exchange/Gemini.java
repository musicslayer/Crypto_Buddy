package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

public class Gemini extends ExchangeAPI {
    public String getName() { return "Gemini"; }
    public String getDisplayName() { return "Gemini REST API"; }

    public AuthUtil.OAuthToken oAuthToken;

    public boolean isSupported(Exchange exchange) {
        return "Gemini".equals(exchange.getName());
    }

    public void authorizeWebView(Context context) {
        AuthUtil.authorizeOAuthWebView(context);
    }

    public void authorizeBrowser(Context context) {
        AuthUtil.authorizeOAuthBrowser(context);
    }

    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {
        String authURLBase = "https://exchange.gemini.com/auth";
        String tokenURLBase = "https://exchange.gemini.com/auth/token";
        String client_id = BuildConfig.gemini_client_id;
        String client_secret = BuildConfig.gemini_client_secret;
        String redirect_uri = "https://musicslayer.github.io/";
        String response_type = "code";
        String grant_type = "authorization_code";
        String[] scopes = new String[] {"account:read", "addresses:read", "balances:read", "history:read"};

        AuthUtil.OAuthInfo oAuthInfo = new AuthUtil.OAuthInfo(authURLBase, tokenURLBase, client_id, client_secret, redirect_uri, response_type, grant_type, scopes);

        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                Gemini.this.oAuthToken = oAuthToken;
                L.onAuthorization();
            }
        };

        AuthUtil.restoreListenersWebView(context, oAuthInfo, L_OAuth);
        AuthUtil.restoreListenersBrowser(context, oAuthInfo, L_OAuth);
    }

    public boolean isAuthorized() {
        return oAuthToken != null && oAuthToken.isAuthorized();
    }

    public ArrayList<AssetQuantity> getCurrentBalance(Exchange exchange) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        return currentBalanceArrayList;
    }

    public String processBalance(String url, String token, ArrayList<AssetQuantity> currentBalanceArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public ArrayList<Transaction> getTransactions(Exchange exchange) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processTransaction(String url, String token, ArrayList<Transaction> transactionArrayList) {
        return DONE;
    }
}