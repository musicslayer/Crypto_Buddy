package com.musicslayer.cryptobuddy.transaction;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class AssetQuantityData {
    final public HashMap<Asset, AssetAmount> deltaMap;

    public AssetQuantityData(ArrayList<AssetQuantity> assetQuantityArrayList) {
        this.deltaMap = new HashMap<>();

        if(assetQuantityArrayList != null) {
            for(AssetQuantity assetQuantity : assetQuantityArrayList) {
                HashMapUtil.putValueInMap(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
            }
        }
    }

    public AssetQuantityData(HashMap<Asset, AssetAmount> deltaMap) {
        this.deltaMap = deltaMap;
    }

    public String getAssetQuantityInfo(HashMap<Asset, AssetQuantity> priceMap, boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);

        ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
        Asset.sortAscendingByType(keySet);

        for(Asset asset : keySet) {
            AssetAmount assetAmount = deltaMap.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);

            s.appendRich("\n    ").appendAssetQuantity(assetQuantity);

            if(priceMap != null) {
                AssetQuantity price = HashMapUtil.getValueFromMap(priceMap, asset);
                if(price != null) {
                    AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), price);
                    AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                    s.appendRich(" = ").appendAssetQuantity(convertedAssetQuantity);
                }
                else {
                    s.appendRich(" = ?");
                }
            }
        }

        AssetQuantity total = getTotal(priceMap);
        if(total != null) {
            s.appendRich("\n\n    Total = ").appendAssetQuantity(total);
        }

        return s.toString();
    }

    public AssetQuantity getTotal(HashMap<Asset, AssetQuantity> priceMap) {
        // Get the sum total for all the amounts' fiat values.
        // Returns null if there are no values at all.
        // Note that all prices should be of the same fiat.
        if(deltaMap.isEmpty() || priceMap == null || priceMap.isEmpty()) {
            return null;
        }

        AssetAmount grandTotal = new AssetAmount("0");
        Fiat fiat = null;

        for(Asset asset : new ArrayList<>(deltaMap.keySet())) {
            AssetQuantity price = HashMapUtil.getValueFromMap(priceMap, asset);
            if(price != null) {
                fiat = (Fiat)price.asset;

                AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), price);

                AssetAmount amount = HashMapUtil.getValueFromMap(deltaMap, asset);
                AssetQuantity assetQuantity = new AssetQuantity(amount, asset);
                AssetQuantity convertedAssetQuantity = assetQuantity.convert(assetPrice);

                grandTotal = grandTotal.add(convertedAssetQuantity.assetAmount);
            }
        }

        return new AssetQuantity(grandTotal, fiat);
    }
}