package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.AssetQuantityData;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.util.ArrayList;

public class ChartData implements DataBridge.SerializableToJSON {
    final public CryptoChart cryptoChart;
    final public ChartAPI chartAPI_pricePoints;
    final public ChartAPI chartAPI_candles;
    final public ArrayList<AssetQuantity> pricePointsArrayList;
    final public ArrayList<AssetQuantity> candlesArrayList;
    final public Timestamp timestamp_pricePoints;
    final public Timestamp timestamp_candles;

    final public AssetQuantityData pricePointsData;
    final public AssetQuantityData candlesData;

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("cryptoChart", cryptoChart, CryptoChart.class)
                .serialize("chartAPI_pricePoints", chartAPI_pricePoints, ChartAPI.class)
                .serialize("chartAPI_candles", chartAPI_candles, ChartAPI.class)
                .serializeArrayList("pricePointsArrayList", pricePointsArrayList, AssetQuantity.class)
                .serializeArrayList("candlesArrayList", candlesArrayList, AssetQuantity.class)
                .serialize("timestamp_pricePoints", timestamp_pricePoints, Timestamp.class)
                .serialize("timestamp_candles", timestamp_candles, Timestamp.class)
                .endObject();
    }

    public static ChartData deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        CryptoChart cryptoChart = o.deserialize("cryptoChart", CryptoChart.class);
        ChartAPI chartAPI_pricePoints = o.deserialize("chartAPI_pricePoints", ChartAPI.class);
        ChartAPI chartAPI_candles = o.deserialize("chartAPI_candles", ChartAPI.class);
        ArrayList<AssetQuantity> pricePointsArrayList = o.deserializeArrayList("pricePointsArrayList", AssetQuantity.class);
        ArrayList<AssetQuantity> candlesArrayList = o.deserializeArrayList("candlesArrayList", AssetQuantity.class);
        Timestamp timestamp_pricePoints = o.deserialize("timestamp_pricePoints", Timestamp.class);
        Timestamp timestamp_candles = o.deserialize("timestamp_candles", Timestamp.class);
        o.endObject();

        return new ChartData(cryptoChart, chartAPI_pricePoints, chartAPI_candles, pricePointsArrayList, candlesArrayList, timestamp_pricePoints, timestamp_candles);
    }

    public ChartData(CryptoChart cryptoChart, ChartAPI chartAPI_pricePoints, ChartAPI chartAPI_candles, ArrayList<AssetQuantity> pricePointsArrayList, ArrayList<AssetQuantity> candlesArrayList, Timestamp timestamp_pricePoints, Timestamp timestamp_candles) {
        this.cryptoChart = cryptoChart;
        this.chartAPI_pricePoints = chartAPI_pricePoints;
        this.chartAPI_candles = chartAPI_candles;
        this.pricePointsArrayList = pricePointsArrayList;
        this.candlesArrayList = candlesArrayList;
        this.timestamp_pricePoints = timestamp_pricePoints;
        this.timestamp_candles = timestamp_candles;

        pricePointsData = new AssetQuantityData(pricePointsArrayList);
        candlesData = new AssetQuantityData(candlesArrayList);
    }

    public static ChartData getAllData(CryptoChart cryptoChart) {
        ChartAPI chartAPI_pricePoints_f = UnknownChartAPI.createUnknownChartAPI(null);
        ChartAPI chartAPI_candles_f = UnknownChartAPI.createUnknownChartAPI(null);
        ArrayList<AssetQuantity> pricePointsArrayList_f = null;
        ArrayList<AssetQuantity> candlesArrayList_f = null;

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
        ArrayList<AssetQuantity> pricePointsArrayList_f = null;
        ArrayList<AssetQuantity> candlesArrayList_f = null;

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
        ArrayList<AssetQuantity> pricePointsArrayList_f = null;
        ArrayList<AssetQuantity> candlesArrayList_f = null;

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
        ArrayList<AssetQuantity> pricePointsArrayList_f = null;
        ArrayList<AssetQuantity> candlesArrayList_f = null;

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
        ArrayList<AssetQuantity> pricePointsArrayList_f = oldChartData.pricePointsArrayList;
        ArrayList<AssetQuantity> candlesArrayList_f = oldChartData.candlesArrayList;
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