package com.musicslayer.cryptobuddy.api;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.chart.ChartAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;

abstract public class API implements DataBridge.SerializableToJSON {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();
    abstract public String getAPIType();

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
            throw new IllegalStateException("version = " + version);
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
        else if("!CHARTAPI!".equals(apiType)) {
            return ChartAPI.getChartAPIFromKey(key);
        }
        else {
            return null;
        }
    }
}
