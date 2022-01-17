package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;

public class ExchangePortfolioObj implements Serialization.SerializableToJSON {
    public String name;
    public ArrayList<Exchange> exchangeArrayList = new ArrayList<>();

    public ExchangePortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ExchangePortfolioObj) && name.equals(((ExchangePortfolioObj)other).name);
    }

    public void addData(Exchange exchange) {
        exchangeArrayList.add(exchange);
    }

    public void removeData(Exchange exchange) {
        exchangeArrayList.remove(exchange);
    }

    public boolean isSaved(Exchange exchange) {
        for(Exchange e : exchangeArrayList) {
            if(e.isSameAs(exchange)) {
                return true;
            }
        }
        return false;
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("name", Serialization.string_serialize(name))
            .put("exchangeArrayList", new Serialization.JSONArrayWithNull(Serialization.serializeArrayList(exchangeArrayList)))
            .toStringOrNull();
    }

    public static ExchangePortfolioObj deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String name = Serialization.string_deserialize(o.getString("name"));
        ArrayList<Exchange> exchangeArrayList = Serialization.deserializeArrayList(o.getJSONArrayString("exchangeArrayList"), Exchange.class);

        ExchangePortfolioObj exchangePortfolioObj = new ExchangePortfolioObj(name);
        exchangePortfolioObj.exchangeArrayList = exchangeArrayList;

        return exchangePortfolioObj;
    }
}
