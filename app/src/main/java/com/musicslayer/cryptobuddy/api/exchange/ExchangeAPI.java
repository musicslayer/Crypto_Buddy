package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.settings.setting.MaxNumberTransactionsSetting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class ExchangeAPI extends API {
    final public static String DONE = "!DONE!";
    final public static String NOTDONE = "!NOTDONE!";
    final public static String ERROR = "!ERROR!";

    public static ArrayList<ExchangeAPI> exchange_apis;
    public static HashMap<String, ExchangeAPI> exchange_api_map;
    public static ArrayList<String> exchange_api_names;
    public static ArrayList<String> exchange_api_display_names;

    public static void initialize(Context context) {
        exchange_api_names = FileUtil.readFileIntoLines(context, R.raw.api_exchange);

        exchange_apis = new ArrayList<>();
        exchange_api_map = new HashMap<>();
        exchange_api_display_names = new ArrayList<>();

        for(String exchangeName : exchange_api_names) {
            ExchangeAPI exchangeAPI = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.api.exchange." + exchangeName);
            exchange_apis.add(exchangeAPI);
            exchange_api_map.put(exchangeName, exchangeAPI);
            exchange_api_display_names.add(exchangeAPI.getDisplayName());
        }
    }

    abstract public boolean isSupported(Exchange exchange);
    abstract public void authorizeWebView(Context context);
    abstract public void authorizeBrowser(Context context);
    abstract public void restoreListeners(Context context, AuthUtil.AuthorizationListener L);
    abstract public boolean isAuthorized();
    abstract public ArrayList<AssetQuantity> getCurrentBalance(Exchange exchange);
    abstract public ArrayList<Transaction> getTransactions(Exchange exchange);

    public static ExchangeAPI getExchangeAPIFromKey(String key) {
        ExchangeAPI exchangeAPI = exchange_api_map.get(key);
        if(exchangeAPI == null) {
            exchangeAPI = UnknownExchangeAPI.createUnknownExchangeAPI(key);
        }

        return exchangeAPI;
    }

    public static int getMaxTransactions() {
        return MaxNumberTransactionsSetting.value;
    }

    public String getAPIType() {
        return "!EXCHANGEAPI!";
    }
}
