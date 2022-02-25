package com.musicslayer.cryptobuddy.data.persistent.user;

import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;
import java.util.ArrayList;

public class ChartPortfolioObj implements DataBridge.SerializableToJSON {
    public String name;
    public ArrayList<CryptoChart> cryptoChartArrayList = new ArrayList<>();

    public ChartPortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ChartPortfolioObj) && name.equals(((ChartPortfolioObj)other).name);
    }

    public void addData(CryptoChart cryptoChart) {
        cryptoChartArrayList.add(cryptoChart);
    }

    public void removeData(CryptoChart cryptoChart) {
        cryptoChartArrayList.remove(cryptoChart);
    }

    public boolean isSaved(CryptoChart cryptoChart) {
        for(CryptoChart cc : cryptoChartArrayList) {
            if(cc.equals(cryptoChart)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .serialize("name", name, String.class)
                .serializeArrayList("cryptoChartArrayList", cryptoChartArrayList, CryptoChart.class)
                .endObject();
    }

    public static ChartPortfolioObj deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        ChartPortfolioObj chartPortfolioObj;

        if("1".equals(version)) {
            String name = o.deserialize("name", String.class);
            ArrayList<CryptoChart> cryptoChartArrayList = o.deserializeArrayList("cryptoChartArrayList", CryptoChart.class);
            o.endObject();

            chartPortfolioObj = new ChartPortfolioObj(name);
            chartPortfolioObj.cryptoChartArrayList = cryptoChartArrayList;
        }
        else {
            throw new IllegalStateException();
        }

        return chartPortfolioObj;
    }
}
