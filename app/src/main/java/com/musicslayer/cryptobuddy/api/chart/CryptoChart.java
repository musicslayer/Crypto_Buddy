package com.musicslayer.cryptobuddy.api.chart;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;

public class CryptoChart implements DataBridge.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(DataBridge.reference(crypto, Crypto.class));
        out.writeString(DataBridge.reference(fiat, Fiat.class));
    }

    public static final Creator<CryptoChart> CREATOR = new Creator<CryptoChart>() {
        @Override
        public CryptoChart createFromParcel(Parcel in) {
            Crypto crypto = DataBridge.dereference(in.readString(), Crypto.class);
            Fiat fiat = DataBridge.dereference(in.readString(), Fiat.class);
            return new CryptoChart(crypto, fiat);
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

    public Crypto crypto;
    public Fiat fiat;

    public CryptoChart(Crypto crypto, Fiat fiat) {
        this.crypto = crypto;
        this.fiat = fiat;
    }

    @NonNull
    @Override
    public String toString() {
        return "Crypto: " + crypto.getSettingName() + "\nFiat: " + fiat.getSettingName();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoChart) &&
            ((crypto == null && ((CryptoChart)other).crypto == null) || (crypto != null && ((CryptoChart) other).crypto != null && crypto.equals(((CryptoChart) other).crypto))) &&
            ((fiat == null && ((CryptoChart)other).fiat == null) || (fiat != null && ((CryptoChart) other).fiat != null && fiat.equals(((CryptoChart) other).fiat)));
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .reference("crypto", crypto, Crypto.class)
                .reference("fiat", fiat, Fiat.class)
                .endObject();
    }

    public static CryptoChart deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        CryptoChart cryptoChart;

        if("1".equals(version)) {
            Crypto crypto = o.dereference("crypto", Crypto.class);
            Fiat fiat = o.dereference("fiat", Fiat.class);
            o.endObject();

            cryptoChart = new CryptoChart(crypto, fiat);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return cryptoChart;
    }
}
