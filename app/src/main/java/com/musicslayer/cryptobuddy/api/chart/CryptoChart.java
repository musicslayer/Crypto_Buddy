package com.musicslayer.cryptobuddy.api.chart;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;

public class CryptoChart implements DataBridge.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(DataBridge.reference(crypto, Crypto.class));
    }

    public static final Creator<CryptoChart> CREATOR = new Creator<CryptoChart>() {
        @Override
        public CryptoChart createFromParcel(Parcel in) {
            Crypto crypto = DataBridge.dereference(in.readString(), Crypto.class);
            return new CryptoChart(crypto);
        }

        @Override
        public CryptoChart[] newArray(int size) {
            return new CryptoChart[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Note that we could have cryptos or fiats in here.
    public Crypto crypto;

    public CryptoChart(Crypto crypto) {
        this.crypto = crypto;
    }

    @NonNull
    @Override
    public String toString() {
        return "Chart = " + crypto.getSettingName();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoChart) &&
            ((crypto == null && ((CryptoChart)other).crypto == null) || (crypto != null && ((CryptoChart) other).crypto != null && crypto.equals(((CryptoChart) other).crypto)));
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .reference("crypto", crypto, Crypto.class)
                .endObject();
    }

    public static CryptoChart deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        CryptoChart cryptoChart;

        if("1".equals(version)) {
            Crypto crypto = o.dereference("crypto", Crypto.class);
            o.endObject();

            cryptoChart = new CryptoChart(crypto);
        }
        else {
            throw new IllegalStateException();
        }

        return cryptoChart;
    }
}
