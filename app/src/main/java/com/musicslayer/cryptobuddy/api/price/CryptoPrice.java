package com.musicslayer.cryptobuddy.api.price;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

public class CryptoPrice implements Serialization.SerializableToJSON, Serialization.Versionable, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Writing a List directly requires a higher Android API.
        out.writeString(Serialization.serializeArrayList(assetArrayList, Asset.class));
        out.writeParcelable(fiat, flags);
    }

    public static final Creator<CryptoPrice> CREATOR = new Creator<CryptoPrice>() {
        @Override
        public CryptoPrice createFromParcel(Parcel in) {
            ArrayList<Asset> assetArrayList = Serialization.deserializeArrayList(in.readString(), Asset.class);
            Fiat fiat = in.readParcelable(Fiat.class.getClassLoader());
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

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serializeArrayList("assetArrayList", assetArrayList, Asset.class)
            .serialize("fiat", fiat, Fiat.class)
            .toStringOrNull();
    }

    public static CryptoPrice deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        ArrayList<Asset> assetArrayList = o.deserializeArrayList("assetArrayList", Asset.class);
        Fiat fiat = o.deserialize("fiat", Fiat.class);
        return new CryptoPrice(assetArrayList, fiat);
    }
}
