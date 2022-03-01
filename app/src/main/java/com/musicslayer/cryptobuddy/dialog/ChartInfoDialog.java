package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChartInfoDialog extends BaseDialog {
    ArrayList<CryptoChart> cryptoChartArrayList;
    HashMap<CryptoChart, ChartData> chartDataMap;
    int cryptoChartIdx;

    public ChartInfoDialog(Activity activity, ArrayList<CryptoChart> cryptoChartArrayList) {
        super(activity);
        this.cryptoChartArrayList = cryptoChartArrayList;
        this.chartDataMap = StateObj.chartDataMap;
    }

    public int getBaseViewID() {
        return R.id.chart_info_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_chart_info);

        ArrayList<String> options = new ArrayList<>();
        for(CryptoChart cryptoChart : cryptoChartArrayList) {
            options.add(cryptoChart.toString());
        }

        TextView T = findViewById(R.id.chart_info_dialog_textView);

        BorderedSpinnerView bsv = findViewById(R.id.chart_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                cryptoChartIdx = pos;
                updateLayout();
            }
        });

        if(cryptoChartArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoChartArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No charts found.");
        }
    }

    public void updateLayout() {
        CryptoChart cryptoChart = cryptoChartArrayList.get(cryptoChartIdx);
        ChartData chartData = HashMapUtil.getValueFromMap(chartDataMap, cryptoChart);

        TextView T = findViewById(R.id.chart_info_dialog_textView);
        T.setText(Html.fromHtml(chartData.getInfoString(true)));
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("cryptoChartIdx", cryptoChartIdx);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoChartIdx = bundle.getInt("cryptoChartIdx");
        }
    }
}