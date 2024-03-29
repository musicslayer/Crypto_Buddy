package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.chart.Candle;
import com.musicslayer.cryptobuddy.chart.PricePoint;

import java.util.ArrayList;

public class UnknownChartAPI extends ChartAPI {
    String key;

    public String getKey() { return key; }

    public String getName() {
        if(key == null) {
            return "?UNKNOWN_CHART_API?";
        }
        else {
            return "?UNKNOWN_CHART_API (" + key + ")?";
        }
    }

    public String getDisplayName() {
        if(key == null) {
            return "?Unknown Chart API?";
        }
        else {
            return "?Unknown Chart API (" + key + ")?";
        }
    }

    public boolean isSupported(CryptoChart cryptoChart) { return false; }
    public ArrayList<PricePoint> getPricePoints(CryptoChart cryptoChart) { return null; }
    public ArrayList<Candle> getCandles(CryptoChart cryptoChart) { return null; }

    public static UnknownChartAPI createUnknownChartAPI(String key) {
        return new UnknownChartAPI(key);
    }

    private UnknownChartAPI(String key) {
        this.key = key;
    }
}
