package com.musicslayer.cryptobuddy.api.exchange;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

public class CryptoExchange implements Serialization.SerializableToJSON, Serialization.Versionable, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(exchange, flags);
        out.writeParcelable(exchangeAPI, flags);
    }

    public static final Creator<CryptoExchange> CREATOR = new Creator<CryptoExchange>() {
        @Override
        public CryptoExchange createFromParcel(Parcel in) {
            Exchange exchange = in.readParcelable(Exchange.class.getClassLoader());
            ExchangeAPI exchangeAPI = in.readParcelable(ExchangeAPI.class.getClassLoader());
            return new CryptoExchange(exchange, exchangeAPI);
        }

        @Override
        public CryptoExchange[] newArray(int size) {
            return new CryptoExchange[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Exchange exchange;
    public ExchangeAPI exchangeAPI;

    public CryptoExchange(Exchange exchange, ExchangeAPI exchangeAPI) {
        this.exchange = exchange;
        this.exchangeAPI = exchangeAPI;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoExchange) &&
            ((exchange == null && ((CryptoExchange)other).exchange == null) || (exchange != null && ((CryptoExchange) other).exchange != null && exchange.equals(((CryptoExchange) other).exchange))) &&
            ((exchangeAPI == null && ((CryptoExchange)other).exchangeAPI == null) || (exchangeAPI != null && ((CryptoExchange) other).exchangeAPI != null && exchangeAPI.equals(((CryptoExchange) other).exchangeAPI)));
    }

    public boolean isSameAs(CryptoExchange cryptoExchange) {
        // Returns true if this CryptoExchange is effectively the same as the input CryptoExchange.
        // For example, don't look at authorization info.
        // For right now, just use "equals".
        return equals(cryptoExchange);
    }

    public boolean isAuthorized() {
        return exchangeAPI != null && exchangeAPI.isAuthorized();
    }

    @NonNull
    @Override
    public String toString() {
        // Just return the exchange name.
        return exchange.toString();
    }

    public static ArrayList<CryptoExchange> getAllValidCryptoExchange(Exchange exchange) {
        ArrayList<CryptoExchange> cryptoExchangeArrayList = new ArrayList<>();

        for(ExchangeAPI exchangeAPI : ExchangeAPI.exchange_apis) {
            if(exchangeAPI.isSupported(exchange)) {
                cryptoExchangeArrayList.add(new CryptoExchange(exchange, exchangeAPI));
            }
        }

        return cryptoExchangeArrayList;
    }

    public String getInfo() {
        // Include authorization info.
        return "Exchange: " + exchange.toString() + "\nExchangeAPI: " + exchangeAPI.toString() + "\n" + exchangeAPI.getAuthorizationInfo();
    }

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType() {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new JSONWithNull.JSONObjectWithNull()
            .put("exchange", exchange, Exchange.class)
            .put("exchangeAPI", exchangeAPI, ExchangeAPI.class)
            .toStringOrNull();
    }

    public static CryptoExchange deserializeFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        Exchange exchange = o.get("exchange", Exchange.class);
        ExchangeAPI exchangeAPI = o.get("exchangeAPI", ExchangeAPI.class);
        return new CryptoExchange(exchange, exchangeAPI);
    }
}
