package com.musicslayer.cryptobuddy.api.exchange;

import android.app.Activity;
import android.content.Context;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;

// TODO Binance OAuth requires approval, and Binance US OAuth is not available.

public class Binance extends ExchangeAPI {
    public String getName() { return "Binance"; }
    public String getDisplayName() { return "Binance REST API"; }

    public AuthUtil.OAuthToken oAuthToken;

    public boolean isSupported(Exchange exchange) {
        return "Binance".equals(exchange.getName());
    }

    public void authorize(Context context) {
        AuthUtil.authorizeOAuth(context);
    }

    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {
        String client_id = BuildConfig.binance_client_id;
        String client_secret = BuildConfig.binance_client_secret;
        String authURLBase = "https://accounts.binance.com/en/oauth/authorize/";
        String tokenURLBase = "https://accounts.binance.com/oauth/token/";
        String authURL = "https://accounts.binance.com/en/oauth/authorize?client_id=" + client_id + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=user:address,user:balance,asset:ocbs";
        long expiryTime = 7200000L; // 2 hours

        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                Binance.this.oAuthToken = oAuthToken;
                L.onAuthorization();
            }
        };

        AuthUtil.restoreListeners(context, authURLBase, authURL, tokenURLBase, client_id, client_secret, expiryTime, L_OAuth);
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
