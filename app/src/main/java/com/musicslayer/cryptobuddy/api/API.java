package com.musicslayer.cryptobuddy.api;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;

import org.json.JSONException;

import java.io.IOException;

abstract public class API implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();
    abstract public String getAPIType();

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws JSONException {
        // We have to do this based on type, rather than just the properties.
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("apiType", getAPIType(), String.class)
            .serialize("key", getKey(), String.class)
            .toStringOrNull();
    }

    public static API legacy_deserializeFromJSON(String s, String version) throws JSONException {
        // We have to do this based on type, rather than just the properties.
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        String apiType = o.deserialize("apiType", String.class);
        String key = o.deserialize("key", String.class);
        return API.getAPI(apiType, key);
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("apiType", getAPIType(), String.class)
                .serialize("key", getKey(), String.class)
                .endObject();
    }

    public static API deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        API api;

        if("2".equals(version)) {
            String apiType = o.deserialize("apiType", String.class);
            String key = o.deserialize("key", String.class);
            o.endObject();

            api = API.getAPI(apiType, key);
        }
        else {
            throw new IllegalStateException();
        }

        return api;
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
