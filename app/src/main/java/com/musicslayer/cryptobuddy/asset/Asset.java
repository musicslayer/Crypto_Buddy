package com.musicslayer.cryptobuddy.asset;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.persistence.Settings;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

abstract public class Asset implements Serializable {
    abstract public String getKey(); // Matches class name for coins, dynamically determined for tokens.
    abstract public String getName(); // Usually same as key, but in some cases (i.e. TRON) it could be different.
    abstract public String getDisplayName();
    abstract public int getScale(); // Number of decimal places

    @Override
    public boolean equals(Object other) {
        return (other instanceof Asset) && getClass().equals(other.getClass());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    public String getSettingName() {
        if("full".equals(Settings.setting_asset)) {
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
        Collections.sort(assetArrayList, new Comparator<Asset>() {
            @Override
            public int compare(Asset a, Asset b) {
                return Asset.compare(a, b);
            }
        });
    }

    public String serialize() {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        return "{\"assetType\":\"" + getAssetType() + "\",\"key\":\"" + getKey() + "\"}";
    }

    public static Asset deserialize(String s) {
        // We have to do this based on whether it's a FIAT, COIN, or a TOKEN, rather than just the properties.
        try {
            JSONObject o = new JSONObject(s);
            String assetType = o.getString("assetType");
            String key = o.getString("key");
            return Asset.getAsset(assetType, key);
        }
        catch(Exception e) {
            return null;
        }
    }

    public String getAssetType() {
        if(this instanceof Fiat) {
            return "!FIAT!";
        }
        else if(this instanceof Coin) {
            return "!COIN!";
        }
        else {
            return ((Token)this).getTokenType();
        }
    }

    public static Asset getAsset(String assetType, String key) {
        if("!FIAT!".equals(assetType)) {
            return Fiat.getFiatFromKey(key);
        }
        else if("!COIN!".equals(assetType)) {
            return Coin.getCoinFromKey(key);
        }
        else {
            return TokenManager.getTokenManagerFromTokenType(assetType).getToken(key, null, null, 0, null);
        }
    }
}
