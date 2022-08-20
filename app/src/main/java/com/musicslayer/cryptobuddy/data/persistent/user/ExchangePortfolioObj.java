package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;
import java.util.ArrayList;

public class ExchangePortfolioObj implements DataBridge.SerializableToJSON {
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
            throw new IllegalStateException("version = " + version);
        }

        return exchangePortfolioObj;
    }
}
