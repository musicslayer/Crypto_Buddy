package com.musicslayer.cryptobuddy.api.exchange;

import android.app.Activity;
import android.content.Context;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.decode.Alphanumeric;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

public class BinanceUS extends ExchangeAPI {
    public String getName() { return "BinanceUS"; }
    public String getDisplayName() { return "Binance US REST API"; }

    public AuthUtil.OAuthToken oAuthToken;

    public boolean isSupported(Exchange exchange) {
        return "BinanceUS".equals(exchange.getName());
    }

    public void authorizeWebView(Context context) {
        AuthUtil.authorizeOAuthWebView(context);
    }

    public void authorizeBrowser(Context context) {
        AuthUtil.authorizeOAuthBrowser(context);
    }

    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {
        String authURLBase = "https://accounts.binance.com/en/oauth/authorize/";
        String tokenURLBase = "https://accounts.binance.com/oauth/token/";
        String client_id = BuildConfig.binance_us_client_id;
        String client_secret = BuildConfig.binance_us_client_secret;
        String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
        String response_type = "code";
        String grant_type = "authorization_code";
        String[] scopes = new String[] {"user:address", "user:balance", "asset:ocbs"};

        AuthUtil.OAuthInfo oAuthInfo = new AuthUtil.OAuthInfo(authURLBase, tokenURLBase, client_id, client_secret, redirect_uri, response_type, grant_type, scopes);

        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                BinanceUS.this.oAuthToken = oAuthToken;
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
