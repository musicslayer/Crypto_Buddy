package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.util.Serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AssetQuantity implements Serialization.SerializableToJSON {
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

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("assetAmount", new Serialization.JSONObjectWithNull(Serialization.serialize(assetAmount)))
            .put("asset", new Serialization.JSONObjectWithNull(Serialization.serialize(asset)))
            .toStringOrNull();
    }

    public static AssetQuantity deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        AssetAmount assetAmount = Serialization.deserialize(o.getJSONObject("assetAmount").toStringOrNull(), AssetAmount.class);
        Asset asset = Serialization.deserialize(o.getJSONObject("asset").toStringOrNull(), Asset.class);
        return new AssetQuantity(assetAmount, asset);
    }
}
