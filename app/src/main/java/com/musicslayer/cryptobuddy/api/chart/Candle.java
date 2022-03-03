package com.musicslayer.cryptobuddy.api.chart;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

// TODO Do these Chart classes need to be Parcelable?
// TODO Should we be storing Timestamp, or just the long value.

// A candle represents an OHLC quartet at a specific point in time.
// (Open, High, Low, Close)
public class Candle implements DataBridge.SerializableToJSON{
    public String timeframe;
    public Timestamp timestamp;
    public long timestampL;
    public BigDecimal openPrice;
    public BigDecimal highPrice;
    public BigDecimal lowPrice;
    public BigDecimal closePrice;

    public Candle(String timeframe, Timestamp timestamp, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice) {
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;

        timestampL = timestamp.date.getTime();
    }

    @NonNull
    @Override
    public String toString() {
        return "[" +
                timeframe + ", " +
                timestamp.toString() + ", " +
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
        Collections.sort(sortedCandlesArrayList, (a, b) -> Timestamp.compare(a.timestamp, b.timestamp));

        // Assume all the timeframes are the same.
        String timeframe_combine = sortedCandlesArrayList.get(0).timeframe;

        // Timestamp should be the first one.
        Timestamp timestamp_combine = sortedCandlesArrayList.get(0).timestamp;

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

        return new Candle(timeframe_combine, timestamp_combine, openPrice_combine, highPrice_combine, lowPrice_combine, closePrice_combine);
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
            minTime = new BigDecimal(candlesArrayList.get(0).timestamp.date.getTime());
            for(Candle candle : candlesArrayList) {
                minTime = minTime.min(new BigDecimal(candle.timestamp.date.getTime()));
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
            maxTime = new BigDecimal(candlesArrayList.get(0).timestamp.date.getTime());
            for(Candle candle : candlesArrayList) {
                maxTime = maxTime.max(new BigDecimal(candle.timestamp.date.getTime()));
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
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("openPrice", openPrice, BigDecimal.class)
                .serialize("highPrice", highPrice, BigDecimal.class)
                .serialize("lowPrice", lowPrice, BigDecimal.class)
                .serialize("closePrice", closePrice, BigDecimal.class)
                .endObject();
    }

    public static Candle deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String timeframe = o.deserialize("timeframe", String.class);
        Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
        BigDecimal openPrice = o.deserialize("openPrice", BigDecimal.class);
        BigDecimal highPrice = o.deserialize("highPrice", BigDecimal.class);
        BigDecimal lowPrice = o.deserialize("lowPrice", BigDecimal.class);
        BigDecimal closePrice = o.deserialize("closePrice", BigDecimal.class);
        o.endObject();

        return new Candle(timeframe, timestamp, openPrice, highPrice, lowPrice, closePrice);
    }
}
