package com.musicslayer.cryptobuddy.asset;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.settings.setting.AssetDisplaySetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

abstract public class Asset implements DataBridge.SerializableToJSON, DataBridge.ReferenceableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getAssetKind());
        out.writeString(getKey());
        out.writeString(getName());
        out.writeString(getDisplayName());
        out.writeInt(getScale());
        out.writeString(getAssetType());
        out.writeString(DataBridge.serializeHashMap(getOriginalAdditionalInfo(), String.class, String.class));
    }

    public static final Parcelable.Creator<Asset> CREATOR = new Parcelable.Creator<Asset>() {
        @Override
        public Asset createFromParcel(Parcel in) {
            String assetKind = in.readString();
            String key = in.readString();
            String name = in.readString();
            String displayName = in.readString();
            int scale = in.readInt();
            String assetType = in.readString();
            HashMap<String, String> additionalInfo = DataBridge.deserializeHashMap(in.readString(), String.class, String.class);

            return Asset.lookupAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
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

    // Used for serialization.
    abstract public String getOriginalKey();
    abstract public String getOriginalName();
    abstract public String getOriginalDisplayName();
    abstract public int getOriginalScale();
    abstract public String getOriginalAssetType();
    abstract public HashMap<String, String> getOriginalAdditionalInfo();

    abstract public String getKey(); // Matches class name for coins, dynamically determined for tokens.
    abstract public String getName(); // Usually same as key, but in some cases it could be different.
    abstract public String getDisplayName(); // Usually same as key, but in some cases it could be different.
    abstract public String getComboName();
    abstract public int getScale(); // Number of decimal places
    abstract public String getAssetType();
    abstract public HashMap<String, String> getAdditionalInfo(); // All other additional info. Varies based on asset kind.

    abstract public String getAssetKind();

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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "5", String.class)
                .serialize("assetKind", getAssetKind(), String.class)
                .serialize("key", getOriginalKey(), String.class)
                .serialize("name", getOriginalName(), String.class)
                .serialize("displayName", getOriginalDisplayName(), String.class)
                .serialize("scale", getOriginalScale(), Integer.class)
                .serialize("assetType", getOriginalAssetType(), String.class)
                .serializeHashMap("additionalInfo", getOriginalAdditionalInfo(), String.class, String.class)
                .endObject();
    }

    public static Asset deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Asset asset;

        if("5".equals(version)) {
            String assetKind = o.deserialize("assetKind", String.class);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String displayName = o.deserialize("displayName", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String assetType = o.deserialize("assetType", String.class);
            HashMap<String, String> additionalInfo = o.deserializeHashMap("additionalInfo", String.class, String.class);
            o.endObject();

            asset = createAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return asset;
    }

    @Override
    public void referenceToJSON(DataBridge.Writer o) throws IOException {
        // For both serialize and reference, write all information.
        // Use original properties directly, not the potentially modified ones from getter functions.
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .serialize("assetKind", getAssetKind(), String.class)
                .serialize("key", getOriginalKey(), String.class)
                .serialize("name", getOriginalName(), String.class)
                .serialize("displayName", getOriginalDisplayName(), String.class)
                .serialize("scale", getOriginalScale(), Integer.class)
                .serialize("assetType", getOriginalAssetType(), String.class)
                .serializeHashMap("additionalInfo", getOriginalAdditionalInfo(), String.class, String.class)
                .endObject();
    }

    public static Asset dereferenceFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Asset asset;

        if("1".equals(version)) {
            // When we dereference, we lookup by key, but we use the extra info in case we cannot find an existing asset.
            String assetKind = o.deserialize("assetKind", String.class);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String displayName = o.deserialize("displayName", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String assetType = o.deserialize("assetType", String.class);
            HashMap<String, String> additionalInfo = o.deserializeHashMap("additionalInfo", String.class, String.class);
            o.endObject();

            asset = lookupAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return asset;
    }

    public static Asset createAsset(String assetKind, String key, String name, String displayName, int scale, String assetType, HashMap<String, String> additionalInfo) {
        // Only the three "core" asset kinds can be dealt with here.
        if("!FIAT!".equals(assetKind)) {
            return new Fiat(key, name, displayName, scale, assetType, additionalInfo);
        }
        else if("!COIN!".equals(assetKind)) {
            return new Coin(key, name, displayName, scale, assetType, additionalInfo);
        }
        else if("!TOKEN!".equals(assetKind)) {
            return new Token(key, name, displayName, scale, assetType, additionalInfo);
        }
        else {
            return null;
        }
    }

    public static Asset lookupAsset(String assetKind, String key, String name, String displayName, int scale, String assetType, HashMap<String, String> additionalInfo) {
        if("!FIAT!".equals(assetKind)) {
            return FiatManager.getFiatManagerFromFiatType(assetType).getExistingFiat(key, name, displayName, scale, additionalInfo);
        }
        else if("!COIN!".equals(assetKind)) {
            return CoinManager.getCoinManagerFromCoinType(assetType).getExistingCoin(key, name, displayName, scale, additionalInfo);
        }
        else if("!TOKEN!".equals(assetKind)) {
            return TokenManager.getTokenManagerFromTokenType(assetType).getExistingToken(key, name, displayName, scale, additionalInfo);
        }
        else if("!EXCHANGE!".equals(assetKind)) {
            // Just create these from scratch, since we cannot look them up.
            return CoinbaseAsset.createCoinbaseAsset(key, name, displayName, scale, assetType, additionalInfo);
        }
        else {
            return null;
        }
    }
}
