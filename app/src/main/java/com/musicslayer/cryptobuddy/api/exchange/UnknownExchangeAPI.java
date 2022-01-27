package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;

import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;

import java.util.ArrayList;

public class UnknownExchangeAPI extends ExchangeAPI {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_EXCHANGE_API?";
        }
        else {
            return "?UNKNOWN_EXCHANGE_API (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Exchange API?";
        }
        else {
            return "?Unknown Exchange API (" + key + ")?";
        }
    }

    public boolean isSupported(Exchange exchange) { return false; }
    public void authorize(Context context, AuthUtil.AuthorizationListener L) {}
    public void restoreListeners(Context context, AuthUtil.AuthorizationListener L) {}
    public boolean isAuthorized() { return false; }
    public ArrayList<AssetQuantity> getCurrentBalance(Exchange exchange) { return null; }
    public ArrayList<Transaction> getTransactions(Exchange exchange) { return null; }

    public static UnknownExchangeAPI createUnknownExchangeAPI(String key) {
        return new UnknownExchangeAPI(key);
    }

    private UnknownExchangeAPI(String key) {
        this.key = key;
    }
}
