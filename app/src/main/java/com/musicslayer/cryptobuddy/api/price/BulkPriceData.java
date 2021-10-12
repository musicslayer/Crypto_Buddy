package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

import java.util.ArrayList;
import java.util.HashMap;

public class BulkPriceData implements Serialization.SerializableToJSON {
    public ArrayList<Crypto> cryptoArrayList;
    public PriceAPI priceAPI_price;
    public PriceAPI priceAPI_marketCap;
    public HashMap<Crypto, AssetQuantity> priceHashMap;
    public HashMap<Crypto, AssetQuantity> marketCapHashMap;

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("cryptoArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(cryptoArrayList)))
            .put("priceAPI_price", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_price)))
            .put("priceAPI_marketCap", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_marketCap)))
            .put("priceHashMap", new Serialization.JSONObjectWithNull(Serialization.serializeHashMap(priceHashMap)))
            .put("marketCapHashMap", new Serialization.JSONObjectWithNull(Serialization.serializeHashMap(marketCapHashMap)))
            .toStringOrNull();
    }

    public static BulkPriceData deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        ArrayList<Crypto> cryptoArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("cryptoArrayList"), Crypto.class);
        PriceAPI priceAPI_price = Serialization.deserialize(o.getJSONObjectString("priceAPI_price"), PriceAPI.class);
        PriceAPI priceAPI_marketCap = Serialization.deserialize(o.getJSONObjectString("priceAPI_marketCap"), PriceAPI.class);
        HashMap<Crypto, AssetQuantity> priceHashMap = Serialization.deserializeHashMap(o.getJSONObjectString("priceHashMap"), Crypto.class, AssetQuantity.class);
        HashMap<Crypto, AssetQuantity> marketCapHashMap = Serialization.deserializeHashMap(o.getJSONObjectString("marketCapHashMap"), Crypto.class, AssetQuantity.class);
        return new BulkPriceData(cryptoArrayList, priceAPI_price, priceAPI_marketCap, priceHashMap, marketCapHashMap);
    }

    public BulkPriceData(ArrayList<Crypto> cryptoArrayList, PriceAPI priceAPI_price, PriceAPI priceAPI_marketCap, HashMap<Crypto, AssetQuantity> priceHashMap, HashMap<Crypto, AssetQuantity> marketCapHashMap) {
        this.cryptoArrayList = cryptoArrayList;
        this.priceAPI_price = priceAPI_price;
        this.priceAPI_marketCap = priceAPI_marketCap;
        this.priceHashMap = priceHashMap;
        this.marketCapHashMap = marketCapHashMap;
    }

    public static BulkPriceData getBulkPriceData(ArrayList<Crypto> cryptoArrayList) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Crypto, AssetQuantity> priceHashMap_f = null;
        HashMap<Crypto, AssetQuantity> marketCapHashMap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoArrayList)) {
                continue;
            }

            priceHashMap_f = priceAPI.getBulkPrice(cryptoArrayList);
            if(priceHashMap_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoArrayList)) {
                continue;
            }

            marketCapHashMap_f = priceAPI.getBulkMarketCap(cryptoArrayList);
            if(marketCapHashMap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new BulkPriceData(cryptoArrayList, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f);
    }

    public boolean isPriceComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && priceHashMap != null;
    }

    public boolean isComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && !(priceAPI_marketCap instanceof UnknownPriceAPI) && priceHashMap != null && marketCapHashMap != null;
    }
}
