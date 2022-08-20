package com.musicslayer.cryptobuddy.api.price;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;
import java.util.ArrayList;

public class CryptoPrice implements DataBridge.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Writing a List directly requires a higher Android API.
        out.writeString(DataBridge.referenceArrayList(assetArrayList, Asset.class));
        out.writeString(DataBridge.reference(fiat, Fiat.class));
    }

    public static final Creator<CryptoPrice> CREATOR = new Creator<CryptoPrice>() {
        @Override
        public CryptoPrice createFromParcel(Parcel in) {
            ArrayList<Asset> assetArrayList = DataBridge.dereferenceArrayList(in.readString(), Asset.class);
            Fiat fiat = DataBridge.dereference(in.readString(), Fiat.class);
            return new CryptoPrice(assetArrayList, fiat);
        }

        @Override
        public CryptoPrice[] newArray(int size) {
            return new CryptoPrice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Note that we could have cryptos or fiats in here.
    public ArrayList<Asset> assetArrayList;
    public Fiat fiat;

    public CryptoPrice(ArrayList<Asset> assetArrayList, Fiat fiat) {
        this.assetArrayList = assetArrayList;
        this.fiat = fiat;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoPrice) &&
            ((assetArrayList == null && ((CryptoPrice)other).assetArrayList == null) || (assetArrayList != null && ((CryptoPrice) other).assetArrayList != null && assetArrayList.equals(((CryptoPrice) other).assetArrayList))) &&
            ((fiat == null && ((CryptoPrice)other).fiat == null) || (fiat != null && ((CryptoPrice) other).fiat != null && fiat.equals(((CryptoPrice) other).fiat)));
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .referenceArrayList("assetArrayList", assetArrayList, Asset.class)
                .reference("fiat", fiat, Fiat.class)
                .endObject();
    }

    public static CryptoPrice deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        CryptoPrice cryptoPrice;

        if("2".equals(version)) {
            ArrayList<Asset> assetArrayList = o.dereferenceArrayList("assetArrayList", Asset.class);
            Fiat fiat = o.dereference("fiat", Fiat.class);
            o.endObject();

            cryptoPrice = new CryptoPrice(assetArrayList, fiat);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return cryptoPrice;
    }
}
