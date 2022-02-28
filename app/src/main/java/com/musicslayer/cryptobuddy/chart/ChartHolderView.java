package com.musicslayer.cryptobuddy.chart;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.util.WindowUtil;

import java.util.ArrayList;

// TODO Top/Bottom Numbers should be formatted to look good.

// Each ChartView instance represents a single graphical chart.
public class ChartHolderView extends CrashLinearLayout {
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

    public void addChartsFromChartDataArray(ArrayList<ChartData> chartDataArrayList) {
        for(ChartData chartData : chartDataArrayList) {
            cryptoChartArrayList.add(chartData.cryptoChart);
        }
        this.makeLayout();
    }

    public void addChart(CryptoChart cryptoChart) {
        cryptoChartArrayList.add(cryptoChart);
        this.makeLayout();
    }

    public void removeChart(CryptoChart cryptoChart) {
        cryptoChartArrayList.remove(cryptoChart);
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
            TraditionalChartView v = new TraditionalChartView(context);
            this.addView(v, L);

            chartViewArrayList.add(v);
            v.draw(cryptoChart);
        }
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
