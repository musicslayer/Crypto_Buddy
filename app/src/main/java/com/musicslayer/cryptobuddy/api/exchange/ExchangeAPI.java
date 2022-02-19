package com.musicslayer.cryptobuddy.api.exchange;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

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

abstract public class ExchangeAPI extends API implements Parcelable {
    final public static String DONE = "!DONE!";
    final public static String NOTDONE = "!NOTDONE!";
    final public static String ERROR = "!ERROR!";

    public static ArrayList<ExchangeAPI> exchange_apis;
    public static HashMap<String, ExchangeAPI> exchange_api_map;
    public static ArrayList<String> exchange_api_names;
    public static ArrayList<String> exchange_api_display_names;

    public static void initialize() {
        exchange_api_names = FileUtil.readFileIntoLines(R.raw.api_exchange);

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

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getKey());
    }

    public static final Parcelable.Creator<ExchangeAPI> CREATOR = new Parcelable.Creator<ExchangeAPI>() {
        @Override
        public ExchangeAPI createFromParcel(Parcel in) {
            return ExchangeAPI.getExchangeAPIFromKey(in.readString());
        }

        @Override
        public ExchangeAPI[] newArray(int size) {
            return new ExchangeAPI[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public boolean isSupported(Exchange exchange);
    abstract public void authorize(Context context, AuthUtil.AuthorizationListener L);
    abstract public void restoreListeners(Context context, AuthUtil.AuthorizationListener L);
    abstract public boolean isAuthorized();
    abstract public String getAuthorizationInfo();
    abstract public ArrayList<AssetQuantity> getCurrentBalance(CryptoExchange cryptoExchange);
    abstract public ArrayList<Transaction> getTransactions(CryptoExchange cryptoExchange);

    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ExchangeAPI) && getKey().equals(((ExchangeAPI)other).getKey());
    }

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
