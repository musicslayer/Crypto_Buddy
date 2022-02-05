package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

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

    public static PriceData merge(PriceData oldPriceData, PriceData newPriceData) {
        PriceAPI priceAPI_price_f = oldPriceData.priceAPI_price;
        PriceAPI priceAPI_marketCap_f = oldPriceData.priceAPI_marketCap;
        HashMap<Asset, AssetQuantity> priceHashMap_f = oldPriceData.priceHashMap;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = oldPriceData.marketCapHashMap;
        Timestamp timestamp_price_f = oldPriceData.timestamp_price;
        Timestamp timestamp_marketCap_f = oldPriceData.timestamp_marketCap;

        if(newPriceData.isPriceComplete()) {
            priceAPI_price_f = newPriceData.priceAPI_price;
            priceHashMap_f = newPriceData.priceHashMap;
            timestamp_price_f = newPriceData.timestamp_price;
        }

        if(newPriceData.isMarketCapComplete()) {
            priceAPI_marketCap_f = newPriceData.priceAPI_marketCap;
            marketCapHashMap_f = newPriceData.marketCapHashMap;
            timestamp_marketCap_f = newPriceData.timestamp_marketCap;
        }

        // Both PriceData objects should have the same cryptoPrice, but just in case we favor the newer one for consistency.
        return new PriceData(newPriceData.cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, timestamp_price_f, timestamp_marketCap_f);
    }

    public boolean isComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && !(priceAPI_marketCap instanceof UnknownPriceAPI) && priceHashMap != null && marketCapHashMap != null;
    }

    public boolean isPriceComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && priceHashMap != null;
    }

    public boolean isMarketCapComplete() {
        return !(priceAPI_marketCap instanceof UnknownPriceAPI) && marketCapHashMap != null;
    }
}
