package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
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

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new JSONWithNull.JSONObjectWithNull()
            .put("name", Serialization.string_serialize(name))
            .put("cryptoExchangeArrayList", new JSONWithNull.JSONArrayWithNull(Serialization.serializeArrayList(cryptoExchangeArrayList)))
            .toStringOrNull();
    }

    public static ExchangePortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        String name = Serialization.string_deserialize(o.getString("name"));
        ArrayList<CryptoExchange> cryptoExchangeArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("cryptoExchangeArrayList"), CryptoExchange.class);

        ExchangePortfolioObj exchangePortfolioObj = new ExchangePortfolioObj(name);
        exchangePortfolioObj.cryptoExchangeArrayList = cryptoExchangeArrayList;

        return exchangePortfolioObj;
    }
}
