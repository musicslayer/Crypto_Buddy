package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

public class ExchangePortfolioObj implements Serialization.SerializableToJSON, Serialization.Versionable {
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

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("name", name, String.class)
            .serializeArrayList("cryptoExchangeArrayList", cryptoExchangeArrayList, CryptoExchange.class)
            .toStringOrNull();
    }

    public static ExchangePortfolioObj deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        String name = o.deserialize("name", String.class);
        ArrayList<CryptoExchange> cryptoExchangeArrayList = o.deserializeArrayList("cryptoExchangeArrayList", CryptoExchange.class);

        ExchangePortfolioObj exchangePortfolioObj = new ExchangePortfolioObj(name);
        exchangePortfolioObj.cryptoExchangeArrayList = cryptoExchangeArrayList;

        return exchangePortfolioObj;
    }
}
