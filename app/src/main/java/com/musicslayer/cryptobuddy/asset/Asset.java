package com.musicslayer.cryptobuddy.asset;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.AssetDisplaySetting;

import java.util.ArrayList;
import java.util.Collections;

abstract public class Asset implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getAssetType());
        out.writeString(getKey());
    }

    public static final Parcelable.Creator<Asset> CREATOR = new Parcelable.Creator<Asset>() {
        @Override
        public Asset createFromParcel(Parcel in) {
            String assetType = in.readString();
            String key = in.readString();
            return Asset.getAsset(assetType, key);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Asset[] newArray(int size) {
            return new Asset[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public String getKey(); // Matches class name for coins, dynamically determined for tokens.
    abstract public String getName(); // Usually same as key, but in some cases it could be different.
    abstract public String getDisplayName();
    abstract public int getScale(); // Number of decimal places
    abstract public String getAssetType();

    @Override
    public boolean equals(Object other) {
        return (other instanceof Asset) && getAssetType().equals(((Asset)other).getAssetType()) && getKey().equals(((Asset)other).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    public String getSettingName() {
        if("full".equals(AssetDisplaySetting.value)) {
            return getDisplayName();
        }
        else {
            return getName();
        }
    }

    private int compare(Asset other) {
        // Fiat comes before Coins, Coins come before Tokens, and then sort alphabetically.
        boolean isFiatA = this instanceof Fiat;
        boolean isFiatB = other instanceof Fiat;

        int f = Boolean.compare(!isFiatA, !isFiatB);
        if(f != 0) {
            return f;
        }

        boolean isCoinA = this instanceof Coin;
        boolean isCoinB = other instanceof Coin;

        int c = Boolean.compare(!isCoinA, !isCoinB);
        if(c != 0) {
            return c;
        }
        else {
            return this.getSettingName().toLowerCase().compareTo(other.getSettingName().toLowerCase());
        }
    }

    public static int compare(Asset a, Asset b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real asset.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public static void sortAscendingByType(ArrayList<Asset> assetArrayList) {
        Collections.sort(assetArrayList, (a, b) -> compare(a, b));
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        return new Serialization.JSONObjectWithNull()
            .put("assetType", Serialization.string_serialize(getAssetType()))
            .put("key", Serialization.string_serialize(getKey()))
            .toStringOrNull();
    }

    public static Asset deserializeFromJSON1(String s) throws org.json.JSONException {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String assetType = Serialization.string_deserialize(o.getString("assetType"));
        String key = Serialization.string_deserialize(o.getString("key"));
        return Asset.getAsset(assetType, key);
    }

    public static Asset getAsset(String assetType, String key) {
        if("!FIAT!".equals(assetType)) {
            return Fiat.getFiatFromKey(key);
        }
        else if("!COIN!".equals(assetType)) {
            return Coin.getCoinFromKey(key);
        }
        else {
            return TokenManager.getTokenManagerFromTokenType(assetType).getToken(null, key, null, null, 0, null);
        }
    }
}
