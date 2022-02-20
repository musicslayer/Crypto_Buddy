package com.musicslayer.cryptobuddy.api;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;

abstract public class API implements Serialization.SerializableToJSON, Serialization.Versionable {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();
    abstract public String getAPIType();

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        // We have to do this based on type, rather than just the properties.
        return new DataBridge.JSONObjectDataBridge()
            .serialize("apiType", getAPIType(), String.class)
            .serialize("key", getKey(), String.class)
            .toStringOrNull();
    }

    public static API deserializeFromJSON(String s, String version) throws org.json.JSONException {
        // We have to do this based on type, rather than just the properties.
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        String apiType = o.deserialize("apiType", String.class);
        String key = o.deserialize("key", String.class);
        return API.getAPI(apiType, key);
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
