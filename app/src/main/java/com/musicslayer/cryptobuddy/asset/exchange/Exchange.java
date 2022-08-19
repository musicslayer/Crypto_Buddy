package com.musicslayer.cryptobuddy.asset.exchange;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

abstract public class Exchange implements DataBridge.SerializableToJSON, Parcelable {
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

    public static void initialize() {
        exchange_names = FileUtil.readFileIntoLines(R.raw.asset_exchange);

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

    abstract public String getKey(); // Matches class name.
    abstract public String getName(); // Usually same as key, but in some cases it could be different.
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
        return (other instanceof Exchange) && getKey().equals(((Exchange)other).getKey());
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("key", getKey(), String.class)
                .endObject();
    }

    public static Exchange deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String key = o.deserialize("key", String.class);
        o.endObject();

        return Exchange.getExchangeFromKey(key);
    }
}
