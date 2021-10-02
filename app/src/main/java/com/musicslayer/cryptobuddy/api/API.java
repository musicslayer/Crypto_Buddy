package com.musicslayer.cryptobuddy.api;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONObject;

abstract public class API implements Serialization.SerializableToJSON {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() {
        // We have to do this based on type, rather than just the properties.
        return "{\"apiType\":\"" + getAPIType() + "\",\"key\":\"" + getKey() + "\"}";
    }

    public static API deserializeFromJSON1(String s) throws org.json.JSONException {
        // We have to do this based on type, rather than just the properties.
        JSONObject o = new JSONObject(s);
        String apiType = o.getString("apiType");
        String key = o.getString("key");
        return API.getAPI(apiType, key);
    }

    public String getAPIType() {
        if(this instanceof AddressAPI) {
            return "!ADDRESSAPI!";
        }
        else if(this instanceof PriceAPI) {
            return "!PRICEAPI!";
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
        else {
            return null;
        }
    }
}
