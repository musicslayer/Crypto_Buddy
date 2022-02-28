package com.musicslayer.cryptobuddy.api.chart;

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

    public PricePoint(String timeframe, Timestamp timestamp, BigDecimal price) {
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.price = price;
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("timeframe", timeframe, String.class)
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("price", price, BigDecimal.class)
                .endObject();
    }

    public static PricePoint deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String timeframe = o.deserialize("timeframe", String.class);
        Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
        BigDecimal price = o.deserialize("price", BigDecimal.class);
        o.endObject();

        return new PricePoint(timeframe, timestamp, price);
    }
}
