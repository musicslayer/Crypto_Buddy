package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.chart.ChartHolderView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.user.ChartPortfolio;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ChartInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ChooseChartDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.DiscreteFilterDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.RemoveChartDialog;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;
import com.musicslayer.cryptobuddy.filter.Filter;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.TimerUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.ToggleButton;

import java.util.ArrayList;

public class ChartPortfolioExplorerActivity extends BaseActivity {
    // Every five minutes, allow an auto update the charts.
    // Every ten seconds, check if we need an update.
    // Note that manual updates also count and will reset the timer.
    public static final long MAX_TIME = 3600000L; // 60 minutes
    public static final long UPDATE_CHECK_TIME = 10000L; // 10 second
    public static final long UPDATE_INTERVAL_TIME = 300000L; // 5 minutes
    public long lastUpdateTime;

    public BaseDialogFragment confirmBackDialogFragment;

    ChartHolderView chartHolderView;

    DiscreteFilter chartFilter = new DiscreteFilter();

    // For now, these are always true.
    public ArrayList<Boolean> includePricePoints;
    public ArrayList<Boolean> includeCandles;

    FloatingActionButton refreshButton;
    ToggleButton autoUpdateButton;
    ProgressDialogFragment download_progressDialogFragment;

    @Override
    public int getAdLayoutViewID() {
        return R.id.chart_portfolio_explorer_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ChartPortfolioExplorerActivity.this, "back");
    }

    @Override
    public int getProgressViewID() {
        return R.id.chart_portfolio_explorer_progressBar;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chart_portfolio_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(ChartPortfolioExplorerActivity.this, ChartPortfolioViewerActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        if(savedInstanceState == null) {
            StateObj.chartPortfolioObj = PersistentUserDataStore.getInstance(ChartPortfolio.class).getFromName(getIntent().getStringExtra("ChartPortfolioName"));
        }

        updateFilter();

        for(CryptoChart cryptochart : StateObj.chartPortfolioObj.cryptoChartArrayList) {
            if(savedInstanceState == null) {
                HashMapUtil.putValueInMap(StateObj.chartDataMap, cryptochart, ChartData.getNoData(cryptochart));
                HashMapUtil.putValueInMap(StateObj.chartDataFilterMap, cryptochart, ChartData.getNoData(cryptochart));
            }
        }

        TextView T_INFO = findViewById(R.id.chart_portfolio_explorer_infoTextView);
        T_INFO.setText("Portfolio = " + StateObj.chartPortfolioObj.name);

        Toolbar toolbar = findViewById(R.id.chart_portfolio_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.chart_portfolio_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ChartPortfolioExplorerActivity.this, R.raw.help_chart_portfolio_explorer);
            }
        });

        chartHolderView = findViewById(R.id.chart_portfolio_explorer_chartHolderView);

        BaseDialogFragment chooseChartDialogFragment = BaseDialogFragment.newInstance(ChooseChartDialog.class);
        chooseChartDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseChartDialog)dialog).isComplete) {
                    // Save new crypto to the portfolio.
                    CryptoChart newCryptoChart = ((ChooseChartDialog)dialog).user_CRYPTOCHART;

                    if(StateObj.chartPortfolioObj.isSaved(newCryptoChart)) {
                        ToastUtil.showToast("chart_in_portfolio");
                    }
                    else {
                        StateObj.chartPortfolioObj.addData(newCryptoChart);
                        PersistentUserDataStore.getInstance(ChartPortfolio.class).updatePortfolio(StateObj.chartPortfolioObj);

                        HashMapUtil.putValueInMap(StateObj.chartDataMap, newCryptoChart, ChartData.getNoData(newCryptoChart));
                        HashMapUtil.putValueInMap(StateObj.chartDataFilterMap, newCryptoChart, ChartData.getNoData(newCryptoChart));

                        updateFilter();
                        updateLayout();

                        // Update this chart only.
                        includePricePoints = new ArrayList<>();
                        includeCandles = new ArrayList<>();

                        for(int i = 0; i < StateObj.chartPortfolioObj.cryptoChartArrayList.size(); i++) {
                            CryptoChart cryptoChart = StateObj.chartPortfolioObj.cryptoChartArrayList.get(i);
                            if(newCryptoChart.equals(cryptoChart)) {
                                includePricePoints.add(true);
                                includeCandles.add(true);
                            }
                            else {
                                includePricePoints.add(false);
                                includeCandles.add(false);
                            }
                        }

                        download_progressDialogFragment.show(ChartPortfolioExplorerActivity.this, "progress_download");
                    }
                }
            }
        });
        chooseChartDialogFragment.restoreListeners(this, "add");

        FloatingActionButton fab_add = findViewById(R.id.chart_portfolio_explorer_addButton);
        fab_add.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chooseChartDialogFragment.show(ChartPortfolioExplorerActivity.this, "add");
            }
        });

        BaseDialogFragment removeChartDialogFragment = BaseDialogFragment.newInstance(RemoveChartDialog.class, StateObj.chartPortfolioObj.cryptoChartArrayList);
        removeChartDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((RemoveChartDialog)dialog).isComplete) {
                    // Remove cryptos from portfolio and then remove their data from the table.
                    ArrayList<CryptoChart> toRemove = ((RemoveChartDialog)dialog).user_cryptoChartArrayList;
                    for(CryptoChart cryptoChart : toRemove) {
                        StateObj.chartPortfolioObj.removeData(cryptoChart);
                        HashMapUtil.removeValueFromMap(StateObj.chartDataMap, cryptoChart);
                        HashMapUtil.removeValueFromMap(StateObj.chartDataFilterMap, cryptoChart);
                    }

                    PersistentUserDataStore.getInstance(ChartPortfolio.class).updatePortfolio(StateObj.chartPortfolioObj);

                    updateFilter();
                    updateLayout();
                }
            }
        });
        removeChartDialogFragment.restoreListeners(this, "remove");

        FloatingActionButton fab_remove = findViewById(R.id.chart_portfolio_explorer_removeButton);
        fab_remove.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                removeChartDialogFragment.show(ChartPortfolioExplorerActivity.this, "remove");
            }
        });

        FloatingActionButton fab_info = findViewById(R.id.chart_portfolio_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ChartInfoDialog.class, StateObj.chartPortfolioObj.cryptoChartArrayList).show(ChartPortfolioExplorerActivity.this, "info");
            }
        });

        download_progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        download_progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Chart Data...");

                ArrayList<CryptoChart> cryptoChartArrayList = StateObj.chartPortfolioObj.cryptoChartArrayList;

                ArrayList<ChartData> newChartDataArrayList = new ArrayList<>();
                for(int i = 0; i < cryptoChartArrayList.size(); i++) {
                    ProgressDialogFragment.reportProgress(i, cryptoChartArrayList.size(), "Charts Finished");

                    CryptoChart cryptoChart = cryptoChartArrayList.get(i);

                    if(ProgressDialogFragment.isCancelled()) { return; }

                    ChartData newChartData;
                    if(includePricePoints.get(i) && includeCandles.get(i)) {
                        newChartData = ChartData.getAllData(cryptoChart);
                    }
                    else if(includePricePoints.get(i)) {
                        newChartData = ChartData.getPricePointsData(cryptoChart);
                    }
                    else if(includeCandles.get(i)) {
                        newChartData = ChartData.getCandlesData(cryptoChart);
                    }
                    else {
                        newChartData = ChartData.getNoData(cryptoChart);
                    }

                    newChartDataArrayList.add(newChartData);
                }

                ProgressDialogFragment.setValue(DataBridge.serializeArrayList(newChartDataArrayList, ChartData.class));
            }
        });

        download_progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ArrayList<ChartData> newChartDataArrayList = DataBridge.deserializeArrayList(ProgressDialogFragment.getValue(), ChartData.class);

                for(int i = 0; i < newChartDataArrayList.size(); i++) {
                    ChartData newChartData = newChartDataArrayList.get(i);

                    boolean isComplete;
                    if(includePricePoints.get(i) && includeCandles.get(i)) {
                        isComplete = newChartData.isComplete();
                    }
                    else if(includePricePoints.get(i)) {
                        isComplete = newChartData.isPricePointsComplete();
                    }
                    else if(includeCandles.get(i)) {
                        isComplete = newChartData.isCandlesComplete();
                    }
                    else {
                        isComplete = true;
                    }

                    if(!isComplete) {
                        // Only alert once. Others would be redundant.
                        ToastUtil.showToast("incomplete_chart_data");
                        break;
                    }
                }

                for(int i = 0; i < StateObj.chartPortfolioObj.cryptoChartArrayList.size(); i++) {
                    CryptoChart cryptoChart = StateObj.chartPortfolioObj.cryptoChartArrayList.get(i);
                    ChartData newChartData = newChartDataArrayList.get(i);
                    ChartData oldChartData = HashMapUtil.getValueFromMap(StateObj.chartDataMap, cryptoChart);
                    ChartData mergedChartData = ChartData.merge(oldChartData, newChartData);
                    HashMapUtil.putValueInMap(StateObj.chartDataMap, cryptoChart, mergedChartData);
                }

                // Apply filter after downloading data.
                ArrayList<String> choices = chartFilter.user_choices;

                StateObj.chartDataFilterMap.clear();
                for(CryptoChart cryptoChart : new ArrayList<>(StateObj.chartDataMap.keySet())) {
                    if(choices.contains(cryptoChart.toString())) {
                        ChartData chartData = HashMapUtil.getValueFromMap(StateObj.chartDataMap, cryptoChart);
                        HashMapUtil.putValueInMap(StateObj.chartDataFilterMap, cryptoChart, chartData);
                    }
                }

                updateLayout();

                ToastUtil.showToast("chart_data_downloaded");

                // Update the time, regardless of how the update was started or ended up.
                lastUpdateTime = System.currentTimeMillis();
            }
        });
        download_progressDialogFragment.restoreListeners(this, "progress_download");

        BaseDialogFragment chartFilterDialogFragment = chartFilter.getGenericDialogFragment();
        chartFilterDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DiscreteFilterDialog)dialog).isComplete) {
                    chartFilter = ((DiscreteFilterDialog)dialog).discreteFilter;

                    ArrayList<String> choices = chartFilter.user_choices;

                    StateObj.chartDataFilterMap.clear();
                    for(CryptoChart cryptoChart : new ArrayList<>(StateObj.chartDataMap.keySet())) {
                        if(choices.contains(cryptoChart.toString())) {
                            ChartData chartData = HashMapUtil.getValueFromMap(StateObj.chartDataMap, cryptoChart);
                            HashMapUtil.putValueInMap(StateObj.chartDataFilterMap, cryptoChart, chartData);
                        }
                    }

                    updateLayout();
                }
            }
        });
        chartFilterDialogFragment.restoreListeners(this, "chart_filter");

        AppCompatButton filterChartButton = findViewById(R.id.chart_portfolio_explorer_filterChartButton);
        filterChartButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                chartFilterDialogFragment.updateArguments(DiscreteFilterDialog.class, chartFilter);
                chartFilterDialogFragment.show(ChartPortfolioExplorerActivity.this, "chart_filter");
            }
        });

        autoUpdateButton = findViewById(R.id.chart_portfolio_explorer_autoUpdateButton);
        autoUpdateButton.setOptions("Auto Update Off", "Auto Update On");

        refreshButton = findViewById(R.id.chart_portfolio_explorer_refreshButton);
        refreshButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                // Don't ask user - just download all types of data.
                // However, if the chart is filtered, then download no data.
                includePricePoints = new ArrayList<>();
                includeCandles = new ArrayList<>();

                ArrayList<String> choices = chartFilter.user_choices;

                for(int i = 0; i < StateObj.chartPortfolioObj.cryptoChartArrayList.size(); i++) {
                    CryptoChart cryptoChart = StateObj.chartPortfolioObj.cryptoChartArrayList.get(i);
                    if(choices.contains(cryptoChart.toString())) {
                        includePricePoints.add(true);
                        includeCandles.add(true);
                    }
                    else {
                        includePricePoints.add(false);
                        includeCandles.add(false);
                    }
                }

                download_progressDialogFragment.show(ChartPortfolioExplorerActivity.this, "progress_download");
            }
        });

        updateLayout();

        // On first creation, download chart data.
        if(savedInstanceState == null && !StateObj.chartPortfolioObj.cryptoChartArrayList.isEmpty()) {
            doChartUpdate();
        }

        startTimer();
    }

    public void startTimer() {
        // Create timer to periodically update the charts.
        TimerUtil.startTimer("auto_update", MAX_TIME, UPDATE_CHECK_TIME, new TimerUtil.TimerUtilListener() {
            @Override
            public void onTickCallback(long millisUntilFinished) {
                long currentTime = System.currentTimeMillis();

                if(autoUpdateButton.toggleState && lastUpdateTime + UPDATE_INTERVAL_TIME < currentTime) {
                    doChartUpdate();
                }
            }

            @Override
            public void onFinishCallback() {
                // Start the timer again.
                startTimer();
            }
        });
    }

    public void doChartUpdate() {
        refreshButton.callOnClick();
    }

    public void updateLayout() {
        // Don't show plots that are filtered. Use this to preserve the same ordering as the user added the charts to the portfolio.
        ArrayList<ChartData> filteredChartDataArrayList = new ArrayList<>();
        for(CryptoChart cryptoChart : StateObj.chartPortfolioObj.cryptoChartArrayList) {
            ChartData chartData = HashMapUtil.getValueFromMap(StateObj.chartDataFilterMap, cryptoChart);
            if(chartData != null) {
                filteredChartDataArrayList.add(chartData);
            }
        }

        chartHolderView.updateChartsFromChartDataArray(new ArrayList<>(filteredChartDataArrayList));
    }

    public void updateFilter() {
        ArrayList<String> data = new ArrayList<>();
        for(CryptoChart cryptoChart : StateObj.chartPortfolioObj.cryptoChartArrayList) {
            data.add(cryptoChart.toString());
        }

        chartFilter.updateFilterData(data);
    }

    @Override
    public boolean onCreateOptionsMenuImpl(Menu menu) {
        menu.add(0, 1, 100, "Crypto Prices");
        menu.add(0, 2, 200, "Crypto Converter");
        menu.add(0, 3, 300, "Report Feedback");
        return true;
    }

    @Override
    public boolean onOptionsItemSelectedImpl(MenuItem item) {
        int id = item.getItemId();

        if (id == 1) {
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(ChartPortfolioExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(ChartPortfolioExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            runWithProgressIndicator(new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    StateObj.chartInfo = chartHolderView.getInfo();
                    StateObj.filterInfo = DataBridge.serialize(chartFilter, Filter.class);
                }
            }, new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    BaseDialogFragment.newInstance(ReportFeedbackDialog.class, "ChartPortfolio").show(getCurrentActivity(), "feedback");
                }
            });

            return true;
        }

        return false;
    }

    public ArrayList<Bitmap> getSurfaceBitmaps() {
        return chartHolderView.getChartBitmaps();
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        bundle.putSerializable("includePricePoints", includePricePoints);
        bundle.putSerializable("includeCandles", includeCandles);
        bundle.putLong("lastUpdateTime", lastUpdateTime);
        bundle.putParcelable("filter", chartFilter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            includePricePoints = (ArrayList<Boolean>)bundle.getSerializable("includePricePoints");
            includeCandles = (ArrayList<Boolean>)bundle.getSerializable("includeCandles");
            lastUpdateTime = bundle.getLong("lastUpdateTime");
            chartFilter = bundle.getParcelable("filter");
        }
    }
}