package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class PriceData implements Serialization.SerializableToJSON {
    final public CryptoPrice cryptoPrice;
    final public PriceAPI priceAPI_price;
    final public PriceAPI priceAPI_marketCap;
    final public HashMap<Asset, AssetQuantity> priceHashMap;
    final public HashMap<Asset, AssetQuantity> marketCapHashMap;
    final public Timestamp timestamp_price;
    final public Timestamp timestamp_marketCap;

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("cryptoPrice", new Serialization.JSONObjectWithNull(Serialization.serialize(cryptoPrice)))
            .put("priceAPI_price", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_price)))
            .put("priceAPI_marketCap", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_marketCap)))
            .put("priceHashMap", new Serialization.JSONObjectWithNull(Serialization.serializeHashMap(priceHashMap)))
            .put("marketCapHashMap", new Serialization.JSONObjectWithNull(Serialization.serializeHashMap(marketCapHashMap)))
            .put("timestamp_price", new Serialization.JSONObjectWithNull(Serialization.serialize(timestamp_price)))
            .put("timestamp_marketCap", new Serialization.JSONObjectWithNull(Serialization.serialize(timestamp_marketCap)))
            .toStringOrNull();
    }

    public static PriceData deserializeFromJSON(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        CryptoPrice cryptoPrice = Serialization.deserialize(o.getJSONObjectString("cryptoPrice"), CryptoPrice.class);
        PriceAPI priceAPI_price = Serialization.deserialize(o.getJSONObjectString("priceAPI_price"), PriceAPI.class);
        PriceAPI priceAPI_marketCap = Serialization.deserialize(o.getJSONObjectString("priceAPI_marketCap"), PriceAPI.class);
        HashMap<Asset, AssetQuantity> priceHashMap = Serialization.deserializeHashMap(o.getJSONObjectString("priceHashMap"), Asset.class, AssetQuantity.class);
        HashMap<Asset, AssetQuantity> marketCapHashMap = Serialization.deserializeHashMap(o.getJSONObjectString("marketCapHashMap"), Asset.class, AssetQuantity.class);
        Timestamp timestamp_price = Serialization.deserialize(o.getJSONObjectString("timestamp_price"), Timestamp.class);
        Timestamp timestamp_marketCap = Serialization.deserialize(o.getJSONObjectString("timestamp_marketCap"), Timestamp.class);
        return new PriceData(cryptoPrice, priceAPI_price, priceAPI_marketCap, priceHashMap, marketCapHashMap, timestamp_price, timestamp_marketCap);
    }

    public PriceData(CryptoPrice cryptoPrice, PriceAPI priceAPI_price, PriceAPI priceAPI_marketCap, HashMap<Asset, AssetQuantity> priceHashMap, HashMap<Asset, AssetQuantity> marketCapHashMap, Timestamp timestamp_price, Timestamp timestamp_marketCap) {
        this.cryptoPrice = cryptoPrice;
        this.priceAPI_price = priceAPI_price;
        this.priceAPI_marketCap = priceAPI_marketCap;
        this.priceHashMap = priceHashMap;
        this.marketCapHashMap = marketCapHashMap;
        this.timestamp_price = timestamp_price;
        this.timestamp_marketCap = timestamp_marketCap;
    }

    public static PriceData getAllData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            priceHashMap_f = priceAPI.getPrice(cryptoPrice);
            if(priceHashMap_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            marketCapHashMap_f = priceAPI.getMarketCap(cryptoPrice);
            if(marketCapHashMap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getPriceData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            priceHashMap_f = priceAPI.getPrice(cryptoPrice);
            if(priceHashMap_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getMarketCapData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            marketCapHashMap_f = priceAPI.getMarketCap(cryptoPrice);
            if(marketCapHashMap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getNoData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public String getPriceInfoString(Asset asset, boolean isRich) {
        // Get info for "CryptoPriceDialog".
        // Note that prices are all positive, so we do not need to call "appendAssetQuantity"
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(!isPriceComplete(asset)) {
            s.appendRich("(Price information not present.)");
        }
        else {
            AssetQuantity priceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, asset);

            AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), priceAssetQuantity);
            s.appendRich("Forward Price = ").appendRich(assetPrice.toString());
            if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                s.appendRich("\nBackward Price = ").appendRich(assetPrice.reverseAssetPrice().toString());
            }
            s.appendRich("\n\nPrice Data Source = ").appendRich(priceAPI_price.getDisplayName());
            s.appendRich("\nPrice Data Timestamp = ").appendRich(timestamp_price.toString());
        }

        if(!isMarketCapComplete(asset)) {
            s.appendRich("\n\n(Market cap information not present.)");
        }
        else {
            AssetQuantity marketCapAssetQuantity = HashMapUtil.getValueFromMap(marketCapHashMap, asset);

            s.appendRich("\n\nMarket Cap = ").appendRich(marketCapAssetQuantity.toString());
            s.appendRich("\n\nMarket Cap Data Source = ").appendRich(priceAPI_marketCap.getDisplayName());
            s.appendRich("\nMarket Cap Data Timestamp = ").appendRich(timestamp_marketCap.toString());
        }

        return s.toString();
    }

    public String getConverterInfoString(String amount, Asset assetPrimary, Asset assetSecondary, boolean isRich) {
        // Get info for "CryptoConverterDialog".
        // Note that prices are all positive, so we do not need to call "appendAssetQuantity"
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(!isPriceComplete(assetPrimary) || !isPriceComplete(assetSecondary)) {
            s.appendRich("(Conversion information not present.)");
        }
        else {
            AssetQuantity primaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, assetPrimary);
            AssetQuantity secondaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, assetSecondary);

            AssetQuantity primaryAssetQuantity = new AssetQuantity(amount, assetPrimary);
            AssetPrice primaryAssetPrice = new AssetPrice(new AssetQuantity("1", assetPrimary), primaryPriceAssetQuantity);
            AssetPrice secondaryAssetPrice = new AssetPrice(new AssetQuantity("1", assetSecondary), secondaryPriceAssetQuantity);
            AssetQuantity secondaryAssetQuantity = primaryAssetQuantity.convert(primaryAssetPrice).convert(secondaryAssetPrice.reverseAssetPrice());

            s.appendRich("Conversion:\n").appendRich(primaryAssetQuantity.toString()).appendRich(" = ").appendRich(secondaryAssetQuantity.toString());

            s.appendRich("\n\nForward Prices:\n").appendRich(primaryAssetPrice.toString()).appendRich("\n").appendRich(secondaryAssetPrice.toString());
            if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                s.appendRich("\n\nBackward Prices:\n").appendRich(primaryAssetPrice.reverseAssetPrice().toString()).appendRich("\n").appendRich(secondaryAssetPrice.reverseAssetPrice().toString());
            }
            s.appendRich("\n\nPrice Data Source = ").appendRich(priceAPI_price.getDisplayName());
            s.appendRich("\nPrice Data Timestamp = ").appendRich(timestamp_price.toString());
        }

        return s.toString();
    }

    public boolean isComplete() {
        return isPriceComplete() && isMarketCapComplete();
    }

    public boolean isPriceComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && priceHashMap != null;
    }

    public boolean isMarketCapComplete() {
        return !(priceAPI_marketCap instanceof UnknownPriceAPI) && marketCapHashMap != null;
    }

    // Since we get the info for multiple assets, these functions check that a particular asset has complete information.
    public boolean isComplete(Asset asset) {
        return isPriceComplete(asset) && isMarketCapComplete(asset);
    }

    public boolean isPriceComplete(Asset asset) {
        return isPriceComplete() && HashMapUtil.getValueFromMap(priceHashMap, asset) != null;
    }

    public boolean isMarketCapComplete(Asset asset) {
        return isMarketCapComplete() && HashMapUtil.getValueFromMap(marketCapHashMap, asset) != null;
    }

    // Check that all assets have complete data.
    public boolean isFull() {
        return isPriceFull() && isMarketFull();
    }

    public boolean isPriceFull() {
        return priceHashMap.keySet().containsAll(cryptoPrice.assetArrayList);
    }

    public boolean isMarketFull() {
        return marketCapHashMap.keySet().containsAll(cryptoPrice.assetArrayList);
    }
}
