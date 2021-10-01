package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AssetQuantity {
    public AssetAmount assetAmount;
    public Asset asset;

    // All assets we support have a fixed number of decimals, so allowing String input is a universal way to specify the amount.
    public AssetQuantity(String assetAmountString, Asset asset) {
        this.assetAmount = new AssetAmount(assetAmountString);
        this.asset = asset;
    }

    public AssetQuantity(AssetAmount assetAmount, Asset asset) {
        this.assetAmount = assetAmount;
        this.asset = asset;
    }

    public void setLoss() {
        this.assetAmount.isLoss = true;
    }

    public boolean hasSlidingScale() {
        return asset instanceof Fiat || (asset.getScale() == 0 && (asset instanceof UnknownCoin || asset instanceof UnknownToken));
    }

    @NonNull
    @Override
    public String toString() {
        return assetAmount.toScaledString(asset.getScale(), hasSlidingScale()) + " " + asset.getSettingName();
    }

    public String toNumericString() {
        // Returns toString, but with a negative sign for a loss.
        return assetAmount.toNumericScaledString(asset.getScale(), hasSlidingScale()) + " " + asset.getSettingName();
    }

    private int compare(AssetQuantity other) {
        // First compare asset, then quantity.
        int s = Asset.compare(asset, other.asset);
        if(s != 0) {
            return s;
        }
        else {
            return AssetAmount.compare(assetAmount, other.assetAmount);
        }
    }

    public static int compare(AssetQuantity a, AssetQuantity b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real action.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public AssetQuantity convert(AssetPrice p) {
        AssetAmount convertedAssetAmount = assetAmount.multiply(p.bottomAssetQuantity.assetAmount).divide(p.topAssetQuantity.assetAmount);
        return new AssetQuantity(convertedAssetAmount, p.bottomAssetQuantity.asset);
    }

    public static void sortAscendingByType(ArrayList<AssetQuantity> assetQuantityArrayList) {
        Collections.sort(assetQuantityArrayList, new Comparator<AssetQuantity>() {
            @Override
            public int compare(AssetQuantity a, AssetQuantity b) {
                return AssetQuantity.compare(a, b);
            }
        });
    }

    public String serialize() {
        return "{\"assetAmount\":" + assetAmount.serialize() + ",\"asset\":" + asset.serialize() + "}";
    }

    public static String serializeArray(ArrayList<AssetQuantity> arrayList) {
        StringBuilder s = new StringBuilder();
        s.append("[");

        for(int i = 0; i < arrayList.size(); i++) {
            s.append(arrayList.get(i).serialize());

            if(i < arrayList.size() - 1) {
                s.append(",");
            }
        }

        s.append("]");
        return s.toString();
    }

    public static AssetQuantity deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            AssetAmount assetAmount = AssetAmount.deserialize(o.getJSONObject("assetAmount").toString());
            Asset asset = Asset.deserialize(o.getJSONObject("asset").toString());
            return new AssetQuantity(assetAmount, asset);
        }
        catch(Exception e) {
            return null;
        }
    }

    public static ArrayList<AssetQuantity> deserializeArray(String s) {
        try {
            ArrayList<AssetQuantity> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                arrayList.add(AssetQuantity.deserialize(o.toString()));
            }

            return arrayList;
        }
        catch(Exception e) {
            return null;
        }
    }
}
