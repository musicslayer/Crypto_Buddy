package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AssetQuantity implements DataBridge.SerializableToJSON {
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

    public boolean hasSlidingScale() {
        return asset instanceof Fiat || (asset.getScale() == 0 && (asset instanceof UnknownCoin || asset instanceof UnknownToken));
    }

    @NonNull
    @Override
    public String toString() {
        return assetAmount.toFormattedScaledString(asset.getScale(), hasSlidingScale()) + " " + asset.getSettingName();
    }

    public String toNumberString(int additionalScale) {
        // Only return the number part of the string. Also allow for more digits if needed.
        return assetAmount.toFormattedScaledString(asset.getScale() + additionalScale, hasSlidingScale());
    }

    public String toRawString() {
        // For raw data, use asset combo name so all information is present.
        return assetAmount.toFormattedScaledString(asset.getScale(), hasSlidingScale()) + " " + asset.getComboName();
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
        Collections.sort(assetQuantityArrayList, (a, b) -> compare(a, b));
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("assetAmount", assetAmount, AssetAmount.class)
                .reference("asset", asset, Asset.class)
                .endObject();
    }

    public static AssetQuantity deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        AssetQuantity assetQuantity;

        if("2".equals(version)) {
            AssetAmount assetAmount = o.deserialize("assetAmount", AssetAmount.class);
            Asset asset = o.dereference("asset", Asset.class);
            o.endObject();

            assetQuantity = new AssetQuantity(assetAmount, asset);
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return assetQuantity;
    }
}
