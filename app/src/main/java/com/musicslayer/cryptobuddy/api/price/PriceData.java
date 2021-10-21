package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.serialize.Serialization;

public class PriceData implements Serialization.SerializableToJSON {
    public Crypto crypto;
    public PriceAPI priceAPI_price;
    public PriceAPI priceAPI_marketCap;
    public AssetQuantity price;
    public AssetQuantity marketCap;

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("crypto", new Serialization.JSONObjectWithNull(Serialization.serialize(crypto)))
            .put("priceAPI_price", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_price)))
            .put("priceAPI_marketCap", new Serialization.JSONObjectWithNull(Serialization.serialize(priceAPI_marketCap)))
            .put("price", new Serialization.JSONObjectWithNull(Serialization.serialize(price)))
            .put("marketCap", new Serialization.JSONObjectWithNull(Serialization.serialize(marketCap)))
            .toStringOrNull();
    }

    public static PriceData deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        Crypto crypto = Serialization.deserialize(o.getJSONObjectString("crypto"), Crypto.class);
        PriceAPI priceAPI_price = Serialization.deserialize(o.getJSONObjectString("priceAPI_price"), PriceAPI.class);
        PriceAPI priceAPI_marketCap = Serialization.deserialize(o.getJSONObjectString("priceAPI_marketCap"), PriceAPI.class);
        AssetQuantity price = Serialization.deserialize(o.getJSONObjectString("price"), AssetQuantity.class);
        AssetQuantity marketCap = Serialization.deserialize(o.getJSONObjectString("marketCap"), AssetQuantity.class);
        return new PriceData(crypto, priceAPI_price, priceAPI_marketCap, price, marketCap);
    }

    public PriceData(Crypto crypto, PriceAPI priceAPI_price, PriceAPI priceAPI_marketCap, AssetQuantity price, AssetQuantity marketCap) {
        this.crypto = crypto;
        this.priceAPI_price = priceAPI_price;
        this.priceAPI_marketCap = priceAPI_marketCap;
        this.price = price;
        this.marketCap = marketCap;
    }

    public static PriceData getAllData(Crypto crypto) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        AssetQuantity price_f = null;
        AssetQuantity marketCap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            price_f = priceAPI.getPrice(crypto);
            if(price_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            marketCap_f = priceAPI.getMarketCap(crypto);
            if(marketCap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(crypto, priceAPI_price_f, priceAPI_marketCap_f, price_f, marketCap_f);
    }

    public static PriceData getPriceData(Crypto crypto) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        AssetQuantity price_f = null;
        AssetQuantity marketCap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            price_f = priceAPI.getPrice(crypto);
            if(price_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        return new PriceData(crypto, priceAPI_price_f, priceAPI_marketCap_f, price_f, marketCap_f);
    }

    public static PriceData getMarketCapData(Crypto crypto) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        AssetQuantity price_f = null;
        AssetQuantity marketCap_f = null;

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            marketCap_f = priceAPI.getMarketCap(crypto);
            if(marketCap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(crypto, priceAPI_price_f, priceAPI_marketCap_f, price_f, marketCap_f);
    }

    public static PriceData getNoData(Crypto crypto) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        AssetQuantity price_f = null;
        AssetQuantity marketCap_f = null;

        return new PriceData(crypto, priceAPI_price_f, priceAPI_marketCap_f, price_f, marketCap_f);
    }

    public boolean isComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && !(priceAPI_marketCap instanceof UnknownPriceAPI) && price != null && marketCap != null;
    }

    public boolean isPriceComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && price != null;
    }

    public boolean isMarketCapComplete() {
        return !(priceAPI_marketCap instanceof UnknownPriceAPI) && marketCap != null;
    }
}
