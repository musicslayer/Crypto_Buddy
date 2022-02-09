package com.musicslayer.cryptobuddy.asset;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.AssetDisplaySetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

abstract public class Asset implements Serialization.SerializableToJSON, Serialization.Versionable, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getAssetKind());
        out.writeString(getAssetType());
        out.writeString(getKey());
    }

    public static final Parcelable.Creator<Asset> CREATOR = new Parcelable.Creator<Asset>() {
        @Override
        public Asset createFromParcel(Parcel in) {
            String assetKind = in.readString();
            String assetType = in.readString();
            String key = in.readString();
            return Asset.getAsset(assetKind, assetType, key);
        }

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
    abstract public String getComboName();
    abstract public int getScale(); // Number of decimal places
    abstract public String getAssetKind();
    abstract public String getAssetType();
    abstract public HashMap<String, String> getAdditionalInfo(); // All other additional info. Varies based on asset kind.

    @Override
    public boolean equals(Object other) {
        return (other instanceof Asset) && getAssetType().equals(((Asset)other).getAssetType()) && getKey().equals(((Asset)other).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    public String getSettingName() {
        if("combo".equals(AssetDisplaySetting.value)) {
            return getComboName();
        }
        else if("full".equals(AssetDisplaySetting.value)) {
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

    public String serializationVersion() { return "2"; }

    public String serializeToJSON() throws org.json.JSONException {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        return new Serialization.JSONObjectWithNull()
            .put("assetKind", Serialization.string_serialize(getAssetKind()))
            .put("assetType", Serialization.string_serialize(getAssetType()))
            .put("key", Serialization.string_serialize(getKey()))
            .toStringOrNull();
    }

    public static Asset deserializeFromJSON1(String s) throws org.json.JSONException {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String assetType = Serialization.string_deserialize(o.getString("assetType"));
        String key = Serialization.string_deserialize(o.getString("key"));

        String assetKind;
        if("!FIAT!".equals(assetType) || "!COIN!".equals(assetType)) {
            assetKind = assetType;
            assetType = "BASE";
        }
        else {
            // Everything else was a token.
            assetKind = "!TOKEN!";
        }

        return getAsset(assetKind, assetType, key);
    }

    public static Asset deserializeFromJSON2(String s) throws org.json.JSONException {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String assetKind = Serialization.string_deserialize(o.getString("assetKind"));
        String assetType = Serialization.string_deserialize(o.getString("assetType"));
        String key = Serialization.string_deserialize(o.getString("key"));
        return getAsset(assetKind, assetType, key);
    }

    public static Asset getAsset(String assetKind, String assetType, String key) {
        if("!FIAT!".equals(assetKind)) {
            return FiatManager.getFiatManagerFromFiatType(assetType).getFiat(key, null, null, 0);
        }
        else if("!COIN!".equals(assetKind)) {
            return CoinManager.getCoinManagerFromCoinType(assetType).getCoin(key, null, null, 0, null);
        }
        else if("!TOKEN!".equals(assetKind)) {
            return TokenManager.getTokenManagerFromTokenType(assetType).getToken(null, key, null, null, 0, null);
        }
        else {
            return null;
        }
    }


}
