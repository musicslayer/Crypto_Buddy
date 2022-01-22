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

public class CryptoCom extends ExchangeAPI {
    public String getName() { return "CryptoCom"; }
    public String getDisplayName() { return "Crypto.com REST API V2"; }

    public AuthUtil.OAuthToken oAuthToken;

    public boolean isSupported(Exchange exchange) {
        return "Coinbase".equals(exchange.getName());
    }

    public void authorize(Context context) {
        AuthUtil.authorizeOAuth(context);
    }

    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {
        //String authURLBase = "https://auth.crypto.com/auth/";
        String authURLBase = "https://auth.crypto.com/auth/authorization/new/";
        String tokenURLBase = "https://oauth2.crypto.com/api/v1/callback/auth/crypto/";
        String client_id = BuildConfig.coinbase_client_id;
        String client_secret = BuildConfig.coinbase_client_secret;
        String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
        String response_type = "code";
        String grant_type = "authorization_code";
        String[] scopes = new String[] {"wallet:transactions:read", "wallet:accounts:read"};

        AuthUtil.OAuthInfo oAuthInfo = new AuthUtil.OAuthInfo(authURLBase, tokenURLBase, client_id, client_secret, redirect_uri, response_type, grant_type, scopes);

        AuthUtil.OAuthAuthorizationListener L_OAuth = new AuthUtil.OAuthAuthorizationListener() {
            @Override
            public void onAuthorization(AuthUtil.OAuthToken oAuthToken) {
                CryptoCom.this.oAuthToken = oAuthToken;
                L.onAuthorization();
            }
        };

        AuthUtil.restoreListeners(context, oAuthInfo, L_OAuth);
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