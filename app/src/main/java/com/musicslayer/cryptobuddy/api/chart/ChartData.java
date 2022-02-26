package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ChartData implements DataBridge.SerializableToJSON {
    final public CryptoChart cryptoChart;
    final public ChartAPI chartAPI_pricePoints;
    final public ChartAPI chartAPI_candles;
    final public ArrayList<PricePoint> pricePointsArrayList;
    final public ArrayList<Candle> candlesArrayList;
    final public Timestamp timestamp_pricePoints;
    final public Timestamp timestamp_candles;

    //final public AssetQuantityData pricePointsData;
    //final public AssetQuantityData candlesData;

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("cryptoChart", cryptoChart, CryptoChart.class)
                .serialize("chartAPI_pricePoints", chartAPI_pricePoints, ChartAPI.class)
                .serialize("chartAPI_candles", chartAPI_candles, ChartAPI.class)
                .serializeArrayList("pricePointsArrayList", pricePointsArrayList, PricePoint.class)
                .serializeArrayList("candlesArrayList", candlesArrayList, Candle.class)
                .serialize("timestamp_pricePoints", timestamp_pricePoints, Timestamp.class)
                .serialize("timestamp_candles", timestamp_candles, Timestamp.class)
                .endObject();
    }

    public static ChartData deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        CryptoChart cryptoChart = o.deserialize("cryptoChart", CryptoChart.class);
        ChartAPI chartAPI_pricePoints = o.deserialize("chartAPI_pricePoints", ChartAPI.class);
        ChartAPI chartAPI_candles = o.deserialize("chartAPI_candles", ChartAPI.class);
        ArrayList<PricePoint> pricePointsArrayList = o.deserializeArrayList("pricePointsArrayList", PricePoint.class);
        ArrayList<Candle> candlesArrayList = o.deserializeArrayList("candlesArrayList", Candle.class);
        Timestamp timestamp_pricePoints = o.deserialize("timestamp_pricePoints", Timestamp.class);
        Timestamp timestamp_candles = o.deserialize("timestamp_candles", Timestamp.class);
        o.endObject();

        return new ChartData(cryptoChart, chartAPI_pricePoints, chartAPI_candles, pricePointsArrayList, candlesArrayList, timestamp_pricePoints, timestamp_candles);
    }

    public ChartData(CryptoChart cryptoChart, ChartAPI chartAPI_pricePoints, ChartAPI chartAPI_candles, ArrayList<PricePoint> pricePointsArrayList, ArrayList<Candle> candlesArrayList, Timestamp timestamp_pricePoints, Timestamp timestamp_candles) {
        this.cryptoChart = cryptoChart;
        this.chartAPI_pricePoints = chartAPI_pricePoints;
        this.chartAPI_candles = chartAPI_candles;
        this.pricePointsArrayList = pricePointsArrayList;
        this.candlesArrayList = candlesArrayList;
        this.timestamp_pricePoints = timestamp_pricePoints;
        this.timestamp_candles = timestamp_candles;

        //pricePointsData = new AssetQuantityData(pricePointsArrayList);
        //candlesData = new AssetQuantityData(candlesArrayList);
    }

    public static ChartData getAllData(CryptoChart cryptoChart) {
        ChartAPI chartAPI_pricePoints_f = UnknownChartAPI.createUnknownChartAPI(null);
        ChartAPI chartAPI_candles_f = UnknownChartAPI.createUnknownChartAPI(null);
        ArrayList<PricePoint> pricePointsArrayList_f = null;
        ArrayList<Candle> candlesArrayList_f = null;

        // Get price points information.
        for(ChartAPI chartAPI : ChartAPI.chart_apis) {
            if(!chartAPI.isSupported(cryptoChart)) {
                continue;
            }

            pricePointsArrayList_f = chartAPI.getPricePoints(cryptoChart);
            if(pricePointsArrayList_f != null) {
                chartAPI_pricePoints_f = chartAPI;
                break;
            }
        }

        // Get candles information.
        for(ChartAPI chartAPI : ChartAPI.chart_apis) {
            if(!chartAPI.isSupported(cryptoChart)) {
                continue;
            }

            candlesArrayList_f = chartAPI.getCandles(cryptoChart);
            if(candlesArrayList_f != null) {
                chartAPI_candles_f = chartAPI;
                break;
            }
        }

        return new ChartData(cryptoChart, chartAPI_pricePoints_f, chartAPI_candles_f, pricePointsArrayList_f, candlesArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ChartData getPricePointsData(CryptoChart cryptoChart) {
        ChartAPI chartAPI_pricePoints_f = UnknownChartAPI.createUnknownChartAPI(null);
        ChartAPI chartAPI_candles_f = UnknownChartAPI.createUnknownChartAPI(null);
        ArrayList<PricePoint> pricePointsArrayList_f = null;
        ArrayList<Candle> candlesArrayList_f = null;

        // Get price points information.
        for(ChartAPI chartAPI : ChartAPI.chart_apis) {
            if(!chartAPI.isSupported(cryptoChart)) {
                continue;
            }

            pricePointsArrayList_f = chartAPI.getPricePoints(cryptoChart);
            if(pricePointsArrayList_f != null) {
                chartAPI_pricePoints_f = chartAPI;
                break;
            }
        }

        return new ChartData(cryptoChart, chartAPI_pricePoints_f, chartAPI_candles_f, pricePointsArrayList_f, candlesArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ChartData getCandlesData(CryptoChart cryptoChart) {
        ChartAPI chartAPI_pricePoints_f = UnknownChartAPI.createUnknownChartAPI(null);
        ChartAPI chartAPI_candles_f = UnknownChartAPI.createUnknownChartAPI(null);
        ArrayList<PricePoint> pricePointsArrayList_f = null;
        ArrayList<Candle> candlesArrayList_f = null;

        // Get candles information.
        for(ChartAPI chartAPI : ChartAPI.chart_apis) {
            if(!chartAPI.isSupported(cryptoChart)) {
                continue;
            }

            candlesArrayList_f = chartAPI.getCandles(cryptoChart);
            if(candlesArrayList_f != null) {
                chartAPI_candles_f = chartAPI;
                break;
            }
        }

        return new ChartData(cryptoChart, chartAPI_pricePoints_f, chartAPI_candles_f, pricePointsArrayList_f, candlesArrayList_f, new Timestamp(), new Timestamp());
    }

    public static ChartData getNoData(CryptoChart cryptoChart) {
        ChartAPI chartAPI_pricePoints_f = UnknownChartAPI.createUnknownChartAPI(null);
        ChartAPI chartAPI_candles_f = UnknownChartAPI.createUnknownChartAPI(null);
        ArrayList<PricePoint> pricePointsArrayList_f = null;
        ArrayList<Candle> candlesArrayList_f = null;

        return new ChartData(cryptoChart, chartAPI_pricePoints_f, chartAPI_candles_f, pricePointsArrayList_f, candlesArrayList_f, new Timestamp(), new Timestamp());
    }

    public boolean isComplete() {
        return isPricePointsComplete() && isCandlesComplete();
    }

    public boolean isPricePointsComplete() {
        return !(chartAPI_pricePoints instanceof UnknownChartAPI) && pricePointsArrayList != null;
    }

    public boolean isCandlesComplete() {
        return !(chartAPI_candles instanceof UnknownChartAPI) && candlesArrayList != null;
    }

    public static ChartData merge(ChartData oldChartData, ChartData newChartData) {
        ChartAPI chartAPI_pricePoints_f = oldChartData.chartAPI_pricePoints;
        ChartAPI chartAPI_candles_f = oldChartData.chartAPI_candles;
        ArrayList<PricePoint> pricePointsArrayList_f = oldChartData.pricePointsArrayList;
        ArrayList<Candle> candlesArrayList_f = oldChartData.candlesArrayList;
        Timestamp timestamp_pricePoints_f = oldChartData.timestamp_pricePoints;
        Timestamp timestamp_candles_f = oldChartData.timestamp_candles;

        if(newChartData.isPricePointsComplete()) {
            chartAPI_pricePoints_f = newChartData.chartAPI_pricePoints;
            pricePointsArrayList_f = newChartData.pricePointsArrayList;
            timestamp_pricePoints_f = newChartData.timestamp_pricePoints;
        }

        if(newChartData.isCandlesComplete()) {
            chartAPI_candles_f = newChartData.chartAPI_candles;
            candlesArrayList_f = newChartData.candlesArrayList;
            timestamp_candles_f = newChartData.timestamp_candles;
        }

        // Both ChartData objects should have the same cryptoChart, but just in case we favor the newer one for consistency.
        return new ChartData(newChartData.cryptoChart, chartAPI_pricePoints_f, chartAPI_candles_f, pricePointsArrayList_f, candlesArrayList_f, timestamp_pricePoints_f, timestamp_candles_f);
    }

    public boolean isPricePointsValid() {
        // There must be at least 1 price to be valid.
        // If any prices are negative, the data is generally considered to be invalid.
        // TODO Implement and use this.
        return true;
    }

    public boolean isCandlesValid() {
        // There must be at least 1 candle to be valid.
        // If any prices are negative, the data is generally considered to be invalid.
        // Also, any candles present must obey the OHLC condition.
        // TODO Implement and use this.
        return true;
    }

    public BigDecimal getMinPricePointsPrice() {
        BigDecimal minPrice;
        if(!isPricePointsComplete() || pricePointsArrayList.isEmpty()) {
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

    public BigDecimal getMaxPricePointsPrice() {
        BigDecimal maxPrice;
        if(!isPricePointsComplete() || pricePointsArrayList.isEmpty()) {
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

    public BigDecimal getMinCandlesPrice() {
        BigDecimal minPrice;
        if(!isCandlesComplete() || candlesArrayList.isEmpty()) {
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

    public BigDecimal getMaxCandlesPrice() {
        BigDecimal maxPrice;
        if(!isCandlesComplete() || candlesArrayList.isEmpty()) {
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

    public BigDecimal getMinPricePointsTime() {
        BigDecimal minTime;
        if(!isPricePointsComplete() || pricePointsArrayList.isEmpty()) {
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

    public BigDecimal getMaxPricePointsTime() {
        BigDecimal maxTime;
        if(!isPricePointsComplete() || pricePointsArrayList.isEmpty()) {
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

    public BigDecimal getMinCandlesTime() {
        BigDecimal minTime;
        if(!isCandlesComplete() || candlesArrayList.isEmpty()) {
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

    public BigDecimal getMaxCandlesTime() {
        BigDecimal maxTime;
        if(!isCandlesComplete() || candlesArrayList.isEmpty()) {
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

    public String getInfoString(boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Chart = " + cryptoChart.crypto.getSettingName());

        if(chartAPI_pricePoints == null || pricePointsArrayList == null) {
            s.appendRich("\n(Price points information not present.)");
        }
        else {
            s.appendRich("\nPrice Points Data Source = ").appendRich(chartAPI_pricePoints.getDisplayName());
            s.appendRich("\nPrice Points Data Timestamp = ").appendRich(timestamp_pricePoints.toString());
        }

        if(chartAPI_candles == null || candlesArrayList == null) {
            s.appendRich("\n(Candles information not present.)");
        }
        else {
            s.appendRich("\nCandles Data Source = ").appendRich(chartAPI_candles.getDisplayName());
            s.appendRich("\nCandles Data Timestamp = ").appendRich(timestamp_candles.toString());
        }

        return s.toString();
    }

    public String getRawFullInfoString() {
        // Same as regular info, but add price points and candles.
        RichStringBuilder s = new RichStringBuilder(false);
        s.appendRich("Chart = " + cryptoChart.crypto.getSettingName());

        if(chartAPI_pricePoints == null || pricePointsArrayList == null) {
            s.appendRich("\n(Price points information not present.)");
        }
        else {
            s.appendRich("\nPrice Points Data Source = ").appendRich(chartAPI_pricePoints.getDisplayName());
            s.appendRich("\nPrice Points Data Timestamp = ").appendRich(timestamp_pricePoints.toString());
            // TODO Add Price Points
        }

        if(chartAPI_candles == null || candlesArrayList == null) {
            s.appendRich("\n(Candles information not present.)");
        }
        else {
            s.appendRich("\nCandles Data Source = ").appendRich(chartAPI_candles.getDisplayName());
            s.appendRich("\nCandles Data Timestamp = ").appendRich(timestamp_candles.toString());
            // TODO Add Candles
        }

        return s.toString();
    }

    public static String getRawFullInfoString(ArrayList<ChartData> chartDataArrayList) {
        if(chartDataArrayList == null) { return null; }

        StringBuilder s = new StringBuilder();
        for(int i = 0; i < chartDataArrayList.size(); i++) {
            ChartData chartData = chartDataArrayList.get(i);
            s.append(chartData.getRawFullInfoString());

            if(i < chartDataArrayList.size() - 1) {
                s.append("\n\n");
            }
        }

        return s.toString();
    }
}