package com.musicslayer.cryptobuddy.api.chart;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.API;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class ChartAPI extends API implements Parcelable {
    final public static String DONE = "!DONE!";
    final public static String NOTDONE = "!NOTDONE!";
    final public static String ERROR = "!ERROR!";

    public static ArrayList<ChartAPI> chart_apis;
    public static HashMap<String, ChartAPI> chart_api_map;
    public static ArrayList<String> chart_api_names;
    public static ArrayList<String> chart_api_display_names;

    public static void initialize() {
        chart_api_names = FileUtil.readFileIntoLines(R.raw.api_chart);

        chart_apis = new ArrayList<>();
        chart_api_map = new HashMap<>();
        chart_api_display_names = new ArrayList<>();

        for(String chartName : chart_api_names) {
            ChartAPI chartAPI = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.api.chart." + chartName);
            chart_apis.add(chartAPI);
            chart_api_map.put(chartName, chartAPI);
            chart_api_display_names.add(chartAPI.getDisplayName());
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getKey());
    }

    public static final Creator<ChartAPI> CREATOR = new Creator<ChartAPI>() {
        @Override
        public ChartAPI createFromParcel(Parcel in) {
            return ChartAPI.getChartAPIFromKey(in.readString());
        }

        @Override
        public ChartAPI[] newArray(int size) {
            return new ChartAPI[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public boolean isSupported(CryptoChart cryptoChart);
    abstract public ArrayList<PricePoint> getPricePoints(CryptoChart cryptoChart);
    public ArrayList<Candle> getCandles(CryptoChart cryptoChart) { return null; }

    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ChartAPI) && getKey().equals(((ChartAPI)other).getKey());
    }

    public static ChartAPI getChartAPIFromKey(String key) {
        ChartAPI chartAPI = chart_api_map.get(key);
        if(chartAPI == null) {
            chartAPI = UnknownChartAPI.createUnknownChartAPI(key);
        }

        return chartAPI;
    }

    public String getAPIType() {
        return "!CHARTAPI!";
    }
}
