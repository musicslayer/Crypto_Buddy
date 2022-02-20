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
import com.musicslayer.cryptobuddy.data.bridge.Referentiation;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.AssetDisplaySetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

abstract public class Asset implements Serialization.SerializableToJSON, Serialization.Versionable, Referentiation.ReferenceableToJSON, Referentiation.Versionable, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Within the app, we can just store the key so we can lookup the asset.
        // We don't need to write full info.
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
            return Asset.lookupAsset(assetKind, assetType, key);
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

    public static String serializationVersion() {
        return "4";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        // For both serialize and reference, write all information.
        // Use original properties directly, not the potentially modified ones from getter functions.
        return new DataBridge.JSONObjectDataBridge()
                .serialize("assetKind", getAssetKind(), String.class)
                .serialize("key", getOriginalKey(), String.class)
                .serialize("name", getOriginalName(), String.class)
                .serialize("displayName", getOriginalDisplayName(), String.class)
                .serialize("scale", getOriginalScale(), Integer.class)
                .serialize("assetType", getOriginalAssetType(), String.class)
                .serializeHashMap("additionalInfo", getOriginalAdditionalInfo(), String.class, String.class)
                .toStringOrNull();
    }

    public static Asset deserializeFromJSON(String s, String version) throws org.json.JSONException {
        Asset asset;

        if("4".equals(version)) {
            // Reconstruct the asset from the deserialized info.
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String assetKind = o.deserialize("assetKind", String.class);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String displayName = o.deserialize("displayName", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String assetType = o.deserialize("assetType", String.class);
            HashMap<String, String> additionalInfo = o.deserializeHashMap("additionalInfo", String.class, String.class);

            asset = createAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
        }
        else if("3".equals(version)) {
            // When we deserialize, we lookup by key, but we use the extra info in case we cannot find an existing asset.
            // In older versions, serialization was performing the role of referentiation.
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String assetKind = o.deserialize("assetKind", String.class);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String displayName = o.deserialize("displayName", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String assetType = o.deserialize("assetType", String.class);
            HashMap<String, String> additionalInfo = o.deserializeHashMap("additionalInfo", String.class, String.class);

            asset = lookupAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
        }
        else if("2".equals(version)) {
            // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
            // In older versions, serialization was performing the role of referentiation.
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String assetKind = o.deserialize("assetKind", String.class);
            String assetType = o.deserialize("assetType", String.class);
            String key = o.deserialize("key", String.class);

            asset = lookupAsset(assetKind, assetType, key);
        }
        else if("1".equals(version)) {
            // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
            // In older versions, serialization was performing the role of referentiation.
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String assetType = o.deserialize("assetType", String.class);
            String key = o.deserialize("key", String.class);

            String assetKind;
            if("!FIAT!".equals(assetType) || "!COIN!".equals(assetType)) {
                assetKind = assetType;
                assetType = "BASE";
            }
            else {
                // Everything else was a token.
                assetKind = "!TOKEN!";
            }

            asset = lookupAsset(assetKind, assetType, key);
        }
        else {
            throw new IllegalStateException();
        }

        return asset;
    }

    public static String referentiationVersion() {
        return "1";
    }

    public static String referentiationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String referenceToJSON() throws org.json.JSONException {
        // For both serialize and reference, write all information.
        // Use original properties directly, not the potentially modified ones from getter functions.
        return new DataBridge.JSONObjectDataBridge()
                .serialize("assetKind", getAssetKind(), String.class)
                .serialize("key", getOriginalKey(), String.class)
                .serialize("name", getOriginalName(), String.class)
                .serialize("displayName", getOriginalDisplayName(), String.class)
                .serialize("scale", getOriginalScale(), Integer.class)
                .serialize("assetType", getOriginalAssetType(), String.class)
                .serializeHashMap("additionalInfo", getOriginalAdditionalInfo(), String.class, String.class)
                .toStringOrNull();
    }

    public static Asset dereferenceFromJSON(String s, String version) throws org.json.JSONException {
        Asset asset;

        if("1".equals(version)) {
            // When we dereference, we lookup by key, but we use the extra info in case we cannot find an existing asset.
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String assetKind = o.deserialize("assetKind", String.class);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String displayName = o.deserialize("displayName", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String assetType = o.deserialize("assetType", String.class);
            HashMap<String, String> additionalInfo = o.deserializeHashMap("additionalInfo", String.class, String.class);

            asset = lookupAsset(assetKind, key, name, displayName, scale, assetType, additionalInfo);
        }
        else {
            throw new IllegalStateException();
        }

        return asset;
    }

    public static Asset createAsset(String assetKind, String key, String name, String displayName, int scale, String assetType, HashMap<String, String> additionalInfo) {
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
        else {
            return null;
        }
    }

    public static Asset lookupAsset(String assetKind, String assetType, String key) {
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
