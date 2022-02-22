package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class ExchangePortfolioObj implements LegacySerialization.SerializableToJSON, LegacySerialization.Versionable, DataBridge.SerializableToJSON {
    public String name;
    public ArrayList<CryptoExchange> cryptoExchangeArrayList = new ArrayList<>();

    public ExchangePortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ExchangePortfolioObj) && name.equals(((ExchangePortfolioObj)other).name);
    }

    public void addData(CryptoExchange cryptoExchange) {
        cryptoExchangeArrayList.add(cryptoExchange);
    }

    public void removeData(CryptoExchange cryptoExchange) {
        cryptoExchangeArrayList.remove(cryptoExchange);
    }

    public boolean isSaved(CryptoExchange cryptoExchange) {
        for(CryptoExchange ce : cryptoExchangeArrayList) {
            if(ce.isSameAs(cryptoExchange)) {
                return true;
            }
        }
        return false;
    }

    public static String legacy_serializationVersion() {
        return "1";
    }

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("name", name, String.class)
            .serializeArrayList("cryptoExchangeArrayList", cryptoExchangeArrayList, CryptoExchange.class)
            .toStringOrNull();
    }

    public static ExchangePortfolioObj legacy_deserializeFromJSON(String s, String version) throws JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        String name = o.deserialize("name", String.class);
        ArrayList<CryptoExchange> cryptoExchangeArrayList = o.deserializeArrayList("cryptoExchangeArrayList", CryptoExchange.class);

        ExchangePortfolioObj exchangePortfolioObj = new ExchangePortfolioObj(name);
        exchangePortfolioObj.cryptoExchangeArrayList = cryptoExchangeArrayList;

        return exchangePortfolioObj;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("name", name, String.class)
                .serializeArrayList("cryptoExchangeArrayList", cryptoExchangeArrayList, CryptoExchange.class)
                .endObject();
    }

    public static ExchangePortfolioObj deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        ExchangePortfolioObj exchangePortfolioObj;

        if("2".equals(version)) {
            String name = o.deserialize("name", String.class);
            ArrayList<CryptoExchange> cryptoExchangeArrayList = o.deserializeArrayList("cryptoExchangeArrayList", CryptoExchange.class);
            o.endObject();

            exchangePortfolioObj = new ExchangePortfolioObj(name);
            exchangePortfolioObj.cryptoExchangeArrayList = cryptoExchangeArrayList;
        }
        else {
            throw new IllegalStateException();
        }

        return exchangePortfolioObj;
    }
}
