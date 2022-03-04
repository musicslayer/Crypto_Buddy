package com.musicslayer.cryptobuddy.api.chart;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// TODO Do these Chart classes need to be Parcelable?

// A candle represents an OHLC quartet at a specific point in time.
// (Open, High, Low, Close)
public class Candle implements DataBridge.SerializableToJSON{
    public String timeframe;
    public BigDecimal time;
    public BigDecimal openPrice;
    public BigDecimal highPrice;
    public BigDecimal lowPrice;
    public BigDecimal closePrice;

    public Candle(String timeframe, BigDecimal time, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice) {
        this.timeframe = timeframe;
        this.time = time;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" +
                timeframe + ", " +
                time.toPlainString() + ", " +
                openPrice.toPlainString() + ", " +
                highPrice.toPlainString() + ", " +
                lowPrice.toPlainString() + ", " +
                closePrice.toPlainString()
                + "]";
    }

    public static Candle combine(ArrayList<Candle> candlesArrayList) {
        // Take an array of candles and return one resultant candle representing the overall price data.
        if(candlesArrayList == null || candlesArrayList.isEmpty()) {
            return null;
        }

        ArrayList<Candle> sortedCandlesArrayList = new ArrayList<>(candlesArrayList);
        Collections.sort(sortedCandlesArrayList, Comparator.comparing(a -> a.time));

        // Assume all the timeframes are the same.
        String timeframe_combine = sortedCandlesArrayList.get(0).timeframe;

        // Time should be the first one.
        BigDecimal time_combine = sortedCandlesArrayList.get(0).time;

        // Take first candle's openPrice.
        BigDecimal openPrice_combine = sortedCandlesArrayList.get(0).openPrice;

        // Search all candles for the highest highPrice and lowest lowPrice.
        BigDecimal currentHighPrice = sortedCandlesArrayList.get(0).highPrice;
        BigDecimal currentLowPrice = sortedCandlesArrayList.get(0).lowPrice;

        for(Candle candle : sortedCandlesArrayList) {
            currentHighPrice = currentHighPrice.max(candle.highPrice);
            currentLowPrice = currentLowPrice.min(candle.lowPrice);
        }

        BigDecimal highPrice_combine = currentHighPrice;
        BigDecimal lowPrice_combine = currentLowPrice;

        // Take last candle's closePrice.
        BigDecimal closePrice_combine = sortedCandlesArrayList.get(sortedCandlesArrayList.size() - 1).closePrice;

        return new Candle(timeframe_combine, time_combine, openPrice_combine, highPrice_combine, lowPrice_combine, closePrice_combine);
    }

    public static ArrayList<Candle> filterByTimeframe(ArrayList<Candle> candlesArrayList, String timeframe) {
        if(candlesArrayList == null) { return null; }

        ArrayList<Candle> filteredCandlesArrayList = new ArrayList<>();
        for(Candle candle : candlesArrayList) {
            if(timeframe.equals(candle.timeframe)) {
                filteredCandlesArrayList.add(candle);
            }
        }
        return filteredCandlesArrayList;
    }

    public static BigDecimal getMinTime(ArrayList<Candle> candlesArrayList) {
        BigDecimal minTime;
        if(candlesArrayList == null || candlesArrayList.isEmpty()) {
            minTime = BigDecimal.ZERO;
        }
        else {
            minTime = candlesArrayList.get(0).time;
            for(Candle candle : candlesArrayList) {
                minTime = minTime.min(candle.time);
            }
        }

        return minTime;
    }

    public static BigDecimal getMaxTime(ArrayList<Candle> candlesArrayList) {
        BigDecimal maxTime;
        if(candlesArrayList == null || candlesArrayList.isEmpty()) {
            maxTime = BigDecimal.ZERO;
        }
        else {
            maxTime = candlesArrayList.get(0).time;
            for(Candle candle : candlesArrayList) {
                maxTime = maxTime.max(candle.time);
            }
        }

        return maxTime;
    }

    public static BigDecimal getMinPrice(ArrayList<Candle> candlesArrayList) {
        BigDecimal minPrice;
        if(candlesArrayList == null || candlesArrayList.isEmpty()) {
            minPrice = BigDecimal.ZERO;
        }
        else {
            // Assume the data is valid and only check the low price.
            minPrice = candlesArrayList.get(0).lowPrice;
            for(Candle candle : candlesArrayList) {
                minPrice = minPrice.min(candle.lowPrice);
            }
        }

        return minPrice;
    }

    public static BigDecimal getMaxPrice(ArrayList<Candle> candlesArrayList) {
        BigDecimal maxPrice;
        if(candlesArrayList == null || candlesArrayList.isEmpty()) {
            maxPrice = BigDecimal.ZERO;
        }
        else {
            // Assume the data is valid and only check the high price.
            maxPrice = candlesArrayList.get(0).highPrice;
            for(Candle candle : candlesArrayList) {
                maxPrice = maxPrice.max(candle.highPrice);
            }
        }

        return maxPrice;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("timeframe", timeframe, String.class)
                .serialize("time", time, BigDecimal.class)
                .serialize("openPrice", openPrice, BigDecimal.class)
                .serialize("highPrice", highPrice, BigDecimal.class)
                .serialize("lowPrice", lowPrice, BigDecimal.class)
                .serialize("closePrice", closePrice, BigDecimal.class)
                .endObject();
    }

    public static Candle deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String timeframe = o.deserialize("timeframe", String.class);
        BigDecimal time = o.deserialize("time", BigDecimal.class);
        BigDecimal openPrice = o.deserialize("openPrice", BigDecimal.class);
        BigDecimal highPrice = o.deserialize("highPrice", BigDecimal.class);
        BigDecimal lowPrice = o.deserialize("lowPrice", BigDecimal.class);
        BigDecimal closePrice = o.deserialize("closePrice", BigDecimal.class);
        o.endObject();

        return new Candle(timeframe, time, openPrice, highPrice, lowPrice, closePrice);
    }
}
