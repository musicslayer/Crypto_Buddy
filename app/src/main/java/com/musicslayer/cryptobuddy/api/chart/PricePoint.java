package com.musicslayer.cryptobuddy.api.chart;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

// A price point represents a price at a specific point in time.
public class PricePoint implements DataBridge.SerializableToJSON {
    public String timeframe;
    public Timestamp timestamp;
    public BigDecimal price;
    public BigDecimal marketCap;
    public BigDecimal volume;

    public PricePoint(String timeframe, Timestamp timestamp, BigDecimal price, BigDecimal marketCap, BigDecimal volume) {
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.price = price;
        this.marketCap = marketCap;
        this.volume = volume;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" +
                timeframe + ", " +
                timestamp.toString() + ", " +
                price.toPlainString() + ", " +
                marketCap.toPlainString() + ", " +
                volume.toPlainString()
                + "]";
    }

    public static ArrayList<PricePoint> filterByTimeframe(ArrayList<PricePoint> pricePointsArrayList, String timeframe) {
        if(pricePointsArrayList == null) { return null; }

        ArrayList<PricePoint> filteredPricePointsArrayList = new ArrayList<>();
        for(PricePoint pricePoint : pricePointsArrayList) {
            if(timeframe.equals(pricePoint.timeframe)) {
                filteredPricePointsArrayList.add(pricePoint);
            }
        }
        return filteredPricePointsArrayList;
    }

    public static BigDecimal getMinTime(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal minTime;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            minTime = BigDecimal.ZERO;
        }
        else {
            minTime = new BigDecimal(pricePointsArrayList.get(0).timestamp.date.getTime());
            for(PricePoint pricePoint : pricePointsArrayList) {
                minTime = minTime.min(new BigDecimal(pricePoint.timestamp.date.getTime()));
            }
        }

        return minTime;
    }

    public static BigDecimal getMaxTime(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal maxTime;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            maxTime = BigDecimal.ZERO;
        }
        else {
            maxTime = new BigDecimal(pricePointsArrayList.get(0).timestamp.date.getTime());
            for(PricePoint pricePoint : pricePointsArrayList) {
                maxTime = maxTime.max(new BigDecimal(pricePoint.timestamp.date.getTime()));
            }
        }

        return maxTime;
    }

    public static BigDecimal getMinPrice(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal minPrice;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            minPrice = BigDecimal.ZERO;
        }
        else {
            minPrice = pricePointsArrayList.get(0).price;
            for(PricePoint pricePoint : pricePointsArrayList) {
                minPrice = minPrice.min(pricePoint.price);
            }
        }

        return minPrice;
    }

    public static BigDecimal getMaxPrice(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal maxPrice;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            maxPrice = BigDecimal.ZERO;
        }
        else {
            maxPrice = pricePointsArrayList.get(0).price;
            for(PricePoint pricePoint : pricePointsArrayList) {
                maxPrice = maxPrice.max(pricePoint.price);
            }
        }

        return maxPrice;
    }

    public static BigDecimal getMinMarketCap(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal minMarketCap;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            minMarketCap = BigDecimal.ZERO;
        }
        else {
            minMarketCap = pricePointsArrayList.get(0).marketCap;
            for(PricePoint pricePoint : pricePointsArrayList) {
                minMarketCap = minMarketCap.min(pricePoint.marketCap);
            }
        }

        return minMarketCap;
    }

    public static BigDecimal getMaxMarketCap(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal maxMarketCap;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            maxMarketCap = BigDecimal.ZERO;
        }
        else {
            maxMarketCap = pricePointsArrayList.get(0).marketCap;
            for(PricePoint pricePoint : pricePointsArrayList) {
                maxMarketCap = maxMarketCap.max(pricePoint.marketCap);
            }
        }

        return maxMarketCap;
    }

    public static BigDecimal getMinVolume(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal minVolume;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            minVolume = BigDecimal.ZERO;
        }
        else {
            minVolume = pricePointsArrayList.get(0).volume;
            for(PricePoint pricePoint : pricePointsArrayList) {
                minVolume = minVolume.min(pricePoint.volume);
            }
        }

        return minVolume;
    }

    public static BigDecimal getMaxVolume(ArrayList<PricePoint> pricePointsArrayList) {
        BigDecimal maxVolume;
        if(pricePointsArrayList == null || pricePointsArrayList.isEmpty()) {
            maxVolume = BigDecimal.ZERO;
        }
        else {
            maxVolume = pricePointsArrayList.get(0).volume;
            for(PricePoint pricePoint : pricePointsArrayList) {
                maxVolume = maxVolume.max(pricePoint.volume);
            }
        }

        return maxVolume;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("timeframe", timeframe, String.class)
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("price", price, BigDecimal.class)
                .serialize("marketCap", marketCap, BigDecimal.class)
                .serialize("volume", volume, BigDecimal.class)
                .endObject();
    }

    public static PricePoint deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String timeframe = o.deserialize("timeframe", String.class);
        Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
        BigDecimal price = o.deserialize("price", BigDecimal.class);
        BigDecimal marketCap = o.deserialize("marketCap", BigDecimal.class);
        BigDecimal volume = o.deserialize("volume", BigDecimal.class);
        o.endObject();

        return new PricePoint(timeframe, timestamp, price, marketCap, volume);
    }
}
