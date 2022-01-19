package com.musicslayer.cryptobuddy.asset.exchange;

// TODO This may not belong in the asset package. Also, should we separate out CEX and DEX?
// TODO Add Registered Trademark symbols to subclasses...?

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

abstract public class Exchange implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getKey());
    }

    public static final Parcelable.Creator<Exchange> CREATOR = new Parcelable.Creator<Exchange>() {
        @Override
        public Exchange createFromParcel(Parcel in) {
            String key = in.readString();
            return Exchange.getExchangeFromKey(key);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Exchange[] newArray(int size) {
            return new Exchange[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static ArrayList<Exchange> exchanges;
    public static HashMap<String, Exchange> exchange_map;
    public static ArrayList<String> exchange_names;
    public static ArrayList<String> exchange_display_names;

    public static void initialize(Context context) {
        exchange_names = FileUtil.readFileIntoLines(context, R.raw.asset_exchange);

        exchanges = new ArrayList<>();
        exchange_map = new HashMap<>();
        exchange_display_names = new ArrayList<>();

        for(String exchangeName : exchange_names) {
            Exchange exchange = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.exchange." + exchangeName);
            exchanges.add(exchange);
            exchange_map.put(exchangeName, exchange);
            exchange_display_names.add(exchange.getDisplayName());
        }
    }

    abstract public String getKey(); // Matches class name for coins, dynamically determined for tokens.
    abstract public String getName(); // Usually same as key, but in some cases (i.e. TRON) it could be different.
    abstract public String getDisplayName();

    public static Exchange getExchangeFromKey(String key) {
        Exchange exchange = exchange_map.get(key);
        if(exchange == null) {
            exchange = UnknownExchange.createUnknownExchange(key);
        }

        return exchange;
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object other) {
        // TODO Should we just look at getKey? Should all Unknown assets be equal to each other?
        return (other instanceof Exchange) && getClass().equals(other.getClass());
    }

    public boolean isSameAs(Exchange exchange) {
        // Returns true if this Exchange is effectively the same as the input Exchange.
        // For now, just use "equals".
        return this.equals(exchange);
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    private int compare(Exchange other) {
        return this.getDisplayName().toLowerCase().compareTo(other.getDisplayName().toLowerCase());
    }

    public static int compare(Exchange a, Exchange b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real asset.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public static void sortAscendingByType(ArrayList<Exchange> exchangeArrayList) {
        Collections.sort(exchangeArrayList, (a, b) -> compare(a, b));
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
                .put("key", Serialization.string_serialize(getKey()))
                .toStringOrNull();
    }

    public static Exchange deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        return Exchange.getExchangeFromKey(key);
    }
}