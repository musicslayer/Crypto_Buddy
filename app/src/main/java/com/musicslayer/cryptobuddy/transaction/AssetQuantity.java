package com.musicslayer.cryptobuddy.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AssetQuantity implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(assetAmount, flags);
        out.writeParcelable(asset, flags);
    }

    public static final Parcelable.Creator<AssetQuantity> CREATOR = new Parcelable.Creator<AssetQuantity>() {
        @Override
        public AssetQuantity createFromParcel(Parcel in) {
            AssetAmount assetAmount = in.readParcelable(AssetAmount.class.getClassLoader());
            Asset asset = in.readParcelable(Asset.class.getClassLoader());
            return new AssetQuantity(assetAmount, asset);
        }

        @Override
        public AssetQuantity[] newArray(int size) {
            return new AssetQuantity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

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
        return assetAmount.toFormattedScaledString(asset.getScale(), hasSlidingScale()) + " " + asset.getSettingName();
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

    public static String getAssetInfo(ArrayList<AssetQuantity> assetQuantityArrayList, HashMap<Asset, AssetAmount> priceMap, Fiat priceFiat, boolean isRich) {
        HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();

        for(AssetQuantity assetQuantity : assetQuantityArrayList) {
            HashMapUtil.putValueInMap(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
        }

        return getAssetInfo(deltaMap, priceMap, priceFiat, isRich);
    }

    public static String getAssetInfo(HashMap<Asset, AssetAmount> deltaMap, HashMap<Asset, AssetAmount> priceMap, Fiat priceFiat, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);

        ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
        Asset.sortAscendingByType(keySet);

        for(Asset asset : keySet) {
            AssetAmount assetAmount = deltaMap.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);

            s.appendRich("\n    ").appendAssetQuantity(assetQuantity);

            if(priceMap != null) {
                AssetAmount price = HashMapUtil.getValueFromMap(priceMap, asset);
                if(price != null) {
                    AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), new AssetQuantity(price, priceFiat));
                    AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                    s.appendRich(" = ").appendAssetQuantity(convertedAssetQuantity);
                }
                else {
                    s.appendRich(" = ?");
                }
            }
        }

        AssetQuantity total = AssetQuantity.getTotal(deltaMap, priceMap, priceFiat);
        if(total != null) {
            s.appendRich("\n\n    Total = ").appendAssetQuantity(total);
        }

        return s.toString();
    }

    public static AssetQuantity getTotal(HashMap<Asset, AssetAmount> deltaMap, HashMap<Asset, AssetAmount> priceMap, Fiat priceFiat) {
        // Get the sum total (in USD for now) for all the amounts' fiat values.
        // Returns null if there are no values at all.
        if(deltaMap.isEmpty() || priceMap == null || priceMap.isEmpty()) {
            return null;
        }

        AssetAmount grandTotal = new AssetAmount("0");

        for(Asset asset : new ArrayList<>(deltaMap.keySet())) {
            AssetAmount price = HashMapUtil.getValueFromMap(priceMap, asset);
            if(price != null) {
                AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), new AssetQuantity(price, priceFiat));

                AssetAmount amount = HashMapUtil.getValueFromMap(deltaMap, asset);
                AssetQuantity assetQuantity = new AssetQuantity(amount, asset);
                AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                grandTotal = grandTotal.add(convertedAssetQuantity.assetAmount);
            }
        }

        return new AssetQuantity(grandTotal, priceFiat);
    }

    public static void sortAscendingByType(ArrayList<AssetQuantity> assetQuantityArrayList) {
        Collections.sort(assetQuantityArrayList, (a, b) -> compare(a, b));
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
        AssetAmount assetAmount = Serialization.deserialize(o.getJSONObjectString("assetAmount"), AssetAmount.class);
        Asset asset = Serialization.deserialize(o.getJSONObjectString("asset"), Asset.class);
        return new AssetQuantity(assetAmount, asset);
    }
}
