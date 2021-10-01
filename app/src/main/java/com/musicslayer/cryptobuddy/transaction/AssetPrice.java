package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

public class AssetPrice {
    public AssetQuantity topAssetQuantity;
    public AssetQuantity bottomAssetQuantity;

    public AssetPrice(AssetQuantity topAssetQuantity, AssetQuantity bottomAssetQuantity) {
        this.topAssetQuantity = topAssetQuantity;
        this.bottomAssetQuantity = bottomAssetQuantity;
    }

    public AssetPrice reverseAssetPrice() {
        return new AssetPrice(bottomAssetQuantity, topAssetQuantity);
    }

    @NonNull
    @Override
    public String toString() {
        if(topAssetQuantity == null || bottomAssetQuantity == null) {
            return "-";
        }

        // Normalize price
        AssetQuantity normalizedTopAssetQuantity = new AssetQuantity("1", topAssetQuantity.asset);
        AssetQuantity normalizedBottomAssetQuantity = normalizedTopAssetQuantity.convert(this);

        return normalizedTopAssetQuantity.toString() + " / " + normalizedBottomAssetQuantity.toString();
    }

    private int compare(AssetPrice other) {
        int valueTop = AssetQuantity.compare(this.topAssetQuantity, other.topAssetQuantity);
        if(valueTop != 0) {
            return valueTop;
        }
        else {
            return AssetQuantity.compare(this.bottomAssetQuantity, other.bottomAssetQuantity);
        }
    }

    public static int compare(AssetPrice a, AssetPrice b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real AssetPrice.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }
}
