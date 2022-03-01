package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

// TODO Allow user to choose which timeframes we want? Or, use one API call to get all data at once and just split it up?

public class ChartData implements DataBridge.SerializableToJSON {
    final public CryptoChart cryptoChart;
    final public ChartAPI chartAPI_pricePoints;
    final public ChartAPI chartAPI_candles;
    final public ArrayList<PricePoint> pricePointsArrayList;
    final public ArrayList<Candle> candlesArrayList;
    final public Timestamp timestamp_pricePoints;
    final public Timestamp timestamp_candles;

    // Maps of timeframes to values.
    final public HashMap<String, ArrayList<PricePoint>> pricePointsHashMap;
    final public HashMap<String, ArrayList<Candle>> candlesHashMap;
    final public HashMap<String, BigDecimal> maxTimeHashMap;
    final public HashMap<String, BigDecimal> minTimeHashMap;
    final public HashMap<String, BigDecimal> maxPriceHashMap;
    final public HashMap<String, BigDecimal> minPriceHashMap;
    final public HashMap<String, BigDecimal> maxMarketCapHashMap;
    final public HashMap<String, BigDecimal> minMarketCapHashMap;
    final public HashMap<String, BigDecimal> maxVolumeHashMap;
    final public HashMap<String, BigDecimal> minVolumeHashMap;

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

        // For now, just hardcode the two timeframes
        pricePointsHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(pricePointsHashMap, "24H", PricePoint.filterByTimeframe(pricePointsArrayList, "24H"));
        HashMapUtil.putValueInMap(pricePointsHashMap, "30D", PricePoint.filterByTimeframe(pricePointsArrayList, "30D"));

        candlesHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(candlesHashMap, "24H", Candle.filterByTimeframe(candlesArrayList, "24H"));
        HashMapUtil.putValueInMap(candlesHashMap, "30D", Candle.filterByTimeframe(candlesArrayList, "30D"));

        maxTimeHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(maxTimeHashMap, "24H", getMaxTime("24H"));
        HashMapUtil.putValueInMap(maxTimeHashMap, "30D", getMaxTime("30D"));

        minTimeHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(minTimeHashMap, "24H", getMinTime("24H"));
        HashMapUtil.putValueInMap(minTimeHashMap, "30D", getMinTime("30D"));

        maxPriceHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(maxPriceHashMap, "24H", getMaxPrice("24H"));
        HashMapUtil.putValueInMap(maxPriceHashMap, "30D", getMaxPrice("30D"));

        minPriceHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(minPriceHashMap, "24H", getMinPrice("24H"));
        HashMapUtil.putValueInMap(minPriceHashMap, "30D", getMinPrice("30D"));

        maxMarketCapHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(maxMarketCapHashMap, "24H", getMaxMarketCap("24H"));
        HashMapUtil.putValueInMap(maxMarketCapHashMap, "30D", getMaxMarketCap("30D"));

        minMarketCapHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(minMarketCapHashMap, "24H", getMinMarketCap("24H"));
        HashMapUtil.putValueInMap(minMarketCapHashMap, "30D", getMinMarketCap("30D"));

        maxVolumeHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(maxVolumeHashMap, "24H", getMaxVolume("24H"));
        HashMapUtil.putValueInMap(maxVolumeHashMap, "30D", getMaxVolume("30D"));

        minVolumeHashMap = new HashMap<>();
        HashMapUtil.putValueInMap(minVolumeHashMap, "24H", getMinVolume("24H"));
        HashMapUtil.putValueInMap(minVolumeHashMap, "30D", getMinVolume("30D"));
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

    public BigDecimal getMinTime(String timeframe) {
        // Check both PricePoints and Candles so graphs look consistent.
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        ArrayList<Candle> timeframeCandlesArrayList = HashMapUtil.getValueFromMap(candlesHashMap, timeframe);
        return PricePoint.getMinTime(timeframePricePointsArrayList).min(Candle.getMinTime(timeframeCandlesArrayList));
    }

    public BigDecimal getMaxTime(String timeframe) {
        // Check both PricePoints and Candles so graphs look consistent.
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        ArrayList<Candle> timeframeCandlesArrayList = HashMapUtil.getValueFromMap(candlesHashMap, timeframe);
        return PricePoint.getMaxTime(timeframePricePointsArrayList).max(Candle.getMaxTime(timeframeCandlesArrayList));
    }

    public BigDecimal getMinPrice(String timeframe) {
        // Check both PricePoints and Candles so graphs look consistent.
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        ArrayList<Candle> timeframeCandlesArrayList = HashMapUtil.getValueFromMap(candlesHashMap, timeframe);
        return PricePoint.getMinPrice(timeframePricePointsArrayList).min(Candle.getMinPrice(timeframeCandlesArrayList));
    }

    public BigDecimal getMaxPrice(String timeframe) {
        // Check both PricePoints and Candles so graphs look consistent.
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        ArrayList<Candle> timeframeCandlesArrayList = HashMapUtil.getValueFromMap(candlesHashMap, timeframe);
        return PricePoint.getMaxPrice(timeframePricePointsArrayList).max(Candle.getMaxPrice(timeframeCandlesArrayList));
    }

    public BigDecimal getMinMarketCap(String timeframe) {
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        return PricePoint.getMinMarketCap(timeframePricePointsArrayList);
    }

    public BigDecimal getMaxMarketCap(String timeframe) {
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        return PricePoint.getMaxMarketCap(timeframePricePointsArrayList);
    }

    public BigDecimal getMinVolume(String timeframe) {
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        return PricePoint.getMinVolume(timeframePricePointsArrayList);
    }

    public BigDecimal getMaxVolume(String timeframe) {
        ArrayList<PricePoint> timeframePricePointsArrayList = HashMapUtil.getValueFromMap(pricePointsHashMap, timeframe);
        return PricePoint.getMaxVolume(timeframePricePointsArrayList);
    }

    public String getInfoString(boolean isRich) {
        RichStringBuilder s = new RichStringBuilder(isRich);
        s.appendRich("Chart = " + cryptoChart.toString());

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