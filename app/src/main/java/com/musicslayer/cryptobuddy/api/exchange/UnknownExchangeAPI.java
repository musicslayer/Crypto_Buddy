package com.musicslayer.cryptobuddy.api.exchange;

import android.app.Activity;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;

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
    public void authorize(Activity activity, ExchangeAPI.AuthorizationListener L) {}
    public ArrayList<AssetQuantity> getCurrentBalance(String token) { return null; }
    public ArrayList<Transaction> getTransactions(String token) { return null; }

    public static UnknownExchangeAPI createUnknownExchangeAPI(String key) {
        return new UnknownExchangeAPI(key);
    }

    private UnknownExchangeAPI(String key) {
        this.key = key;
    }
}
