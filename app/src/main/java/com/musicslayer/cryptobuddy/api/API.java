package com.musicslayer.cryptobuddy.api;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.serialize.Serialization;

// TODO Instead of fallback model, user should be able to select an api. Also, once an API is chosen, "Download Data" will download all data from the same api.

abstract public class API implements Serialization.SerializableToJSON {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        // We have to do this based on type, rather than just the properties.
        return new Serialization.JSONObjectWithNull()
            .put("apiType", Serialization.string_serialize(getAPIType()))
            .put("key", Serialization.string_serialize(getKey()))
            .toStringOrNull();
    }

    public static API deserializeFromJSON1(String s) throws org.json.JSONException {
        // We have to do this based on type, rather than just the properties.
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String apiType = Serialization.string_deserialize(o.getString("apiType"));
        String key = Serialization.string_deserialize(o.getString("key"));
        return API.getAPI(apiType, key);
    }

    public String getAPIType() {
        if(this instanceof AddressAPI) {
            return "!ADDRESSAPI!";
        }
        else if(this instanceof PriceAPI) {
            return "!PRICEAPI!";
        }
        else if(this instanceof ExchangeAPI) {
            return "!EXCHANGEAPI!";
        }
        else {
            return null;
        }
    }

    public static API getAPI(String apiType, String key) {
        if("!ADDRESSAPI!".equals(apiType)) {
            return AddressAPI.getAddressAPIFromKey(key);
        }
        else if("!PRICEAPI!".equals(apiType)) {
            return PriceAPI.getPriceAPIFromKey(key);
        }
        else if("!EXCHANGEAPI!".equals(apiType)) {
            return ExchangeAPI.getExchangeAPIFromKey(key);
        }
        else {
            return null;
        }
    }
}
