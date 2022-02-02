package com.musicslayer.cryptobuddy.api.price;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;

public class CryptoPrice implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Writing a List directly requires a higher API.
        out.writeString(Serialization.serializeArrayList(cryptoArrayList));
        out.writeParcelable(fiat, flags);
    }

    public static final Creator<CryptoPrice> CREATOR = new Creator<CryptoPrice>() {
        @Override
        public CryptoPrice createFromParcel(Parcel in) {
            ArrayList<Crypto> cryptoArrayList = Serialization.deserializeArrayList(in.readString(), Crypto.class);
            Fiat fiat = in.readParcelable(Fiat.class.getClassLoader());
            return new CryptoPrice(cryptoArrayList, fiat);
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

    public ArrayList<Crypto> cryptoArrayList;
    public Fiat fiat;

    public CryptoPrice(ArrayList<Crypto> cryptoArrayList, Fiat fiat) {
        this.cryptoArrayList = cryptoArrayList;
        this.fiat = fiat;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof CryptoPrice) &&
            ((cryptoArrayList == null && ((CryptoPrice)other).cryptoArrayList == null) || (cryptoArrayList != null && ((CryptoPrice) other).cryptoArrayList != null && cryptoArrayList.equals(((CryptoPrice) other).cryptoArrayList))) &&
            ((fiat == null && ((CryptoPrice)other).fiat == null) || (fiat != null && ((CryptoPrice) other).fiat != null && fiat.equals(((CryptoPrice) other).fiat)));
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("cryptoArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(cryptoArrayList)))
            .put("fiat", new Serialization.JSONObjectWithNull(Serialization.serialize(fiat)))
            .toStringOrNull();
    }

    public static CryptoPrice deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        ArrayList<Crypto> cryptoArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("cryptoArrayList"), Crypto.class);
        Fiat fiat = Serialization.deserialize(o.getJSONObjectString("fiat"), Fiat.class);
        return new CryptoPrice(cryptoArrayList, fiat);
    }
}
