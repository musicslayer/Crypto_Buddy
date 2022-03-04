package com.musicslayer.cryptobuddy.view.chart;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.WindowUtil;

import java.util.ArrayList;
import java.util.HashMap;

// Each ChartView instance represents a single graphical chart.
public class ChartHolderView extends CrashLinearLayout {
    HashMap<CryptoChart, ChartConfig> chartConfigHashMap = new HashMap<>();
    public ArrayList<TraditionalChartView> chartViewArrayList = new ArrayList<>();
    public ArrayList<CryptoChart> cryptoChartArrayList = new ArrayList<>();

    public ChartHolderView(Context context) {
        this(context, null);
    }

    public ChartHolderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void reset() {
        this.removeAllViews();
        chartViewArrayList.clear();
        cryptoChartArrayList.clear();
    }

    public void updateChartsFromChartDataArray(ArrayList<ChartData> chartDataArrayList) {
        // We want to maintain the config settings of any charts that already existed before the update.
        chartConfigHashMap.clear();
        for(int i = 0; i < getChildCount(); i++) {
            TraditionalChartView v = (TraditionalChartView)getChildAt(i);
            HashMapUtil.putValueInMap(chartConfigHashMap, v.chartData.cryptoChart, v.getChartConfig());
        }

        this.reset();
        for(ChartData chartData : chartDataArrayList) {
            cryptoChartArrayList.add(chartData.cryptoChart);
        }
        this.makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        this.setOrientation(VERTICAL);

        // Because of ScrollView quirks, we have to manually specify the device width of the children here.
        int[] dimensions = WindowUtil.getDimensions(this.activity);
        LinearLayout.LayoutParams L = new LinearLayout.LayoutParams(dimensions[0], LinearLayout.LayoutParams.WRAP_CONTENT);

        chartViewArrayList.clear();

        for(CryptoChart cryptoChart : cryptoChartArrayList) {
            // For now, we only have one type of ChartView.
            ChartConfig chartConfig = HashMapUtil.getValueFromMap(chartConfigHashMap, cryptoChart);
            TraditionalChartView v = new TraditionalChartView(context, chartConfig);
            this.addView(v, L);

            chartViewArrayList.add(v);
            v.draw(cryptoChart);
        }
    }

    public String getInfo() {
        // Get a String representation of all the charts' state.
        // Different than serialization because the info cannot be used to reconstruct the charts.
        StringBuilder s = new StringBuilder();
        s.append("Chart Info:");

        // Add in each child chart's info.
        for(int i = 0; i < getChildCount(); i++) {
            TraditionalChartView v = (TraditionalChartView)getChildAt(i);
            s.append("\n\n").append(v.getInfo());
        }

        return s.toString();
    }

    public ArrayList<Bitmap> getChartBitmaps() {
        ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();

        for(int i = 0; i < getChildCount(); i++) {
            TraditionalChartView v = (TraditionalChartView)getChildAt(i);
            Bitmap bitmap = v.bitmap;
            if(bitmap != null) {
                bitmapArrayList.add(bitmap);
            }
        }

        return bitmapArrayList;
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        for(int i = 0; i < chartViewArrayList.size(); i++) {
            bundle.putParcelable("chart_" + i, chartViewArrayList.get(i).onSaveInstanceState());
        }

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            for(int i = 0; i < chartViewArrayList.size(); i++) {
                chartViewArrayList.get(i).onRestoreInstanceState(bundle.getParcelable("chart_" + i));
            }
        }
        return state;
    }
}
