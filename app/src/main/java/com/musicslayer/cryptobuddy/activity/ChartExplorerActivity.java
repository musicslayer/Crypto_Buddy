package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.view.chart.ChartHolderView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ChartInfoDialog;
import com.musicslayer.cryptobuddy.dialog.ConfirmBackDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoConverterDialog;
import com.musicslayer.cryptobuddy.dialog.CryptoPricesDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ReportFeedbackDialog;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.TimerUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.ToggleButton;

import java.util.ArrayList;

public class ChartExplorerActivity extends BaseActivity {
    // Every five minutes, allow an auto update the charts.
    // Every ten seconds, check if we need an update.
    // Note that manual updates also count and will reset the timer.
    public static final long MAX_TIME = 3600000L; // 60 minutes
    public static final long UPDATE_CHECK_TIME = 10000L; // 10 second
    public static final long UPDATE_INTERVAL_TIME = 300000L; // 5 minutes
    public long lastUpdateTime;

    public BaseDialogFragment confirmBackDialogFragment;

    ChartHolderView chartHolderView;

    ArrayList<CryptoChart> cryptoChartArrayList = new ArrayList<>();

    // For now, these are always true.
    public ArrayList<Boolean> includePricePoints;
    public ArrayList<Boolean> includeCandles;

    FloatingActionButton refreshButton;
    ToggleButton autoUpdateButton;

    @Override
    public int getAdLayoutViewID() {
        return R.id.chart_explorer_adLayout;
    }

    @Override
    public int getProgressViewID() {
        return R.id.chart_explorer_progressBar;
    }

    @Override
    public void onBackPressedImpl() {
        confirmBackDialogFragment.show(ChartExplorerActivity.this, "back");
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chart_explorer);

        confirmBackDialogFragment = BaseDialogFragment.newInstance(ConfirmBackDialog.class);
        confirmBackDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmBackDialog)dialog).isComplete) {
                    startActivity(new Intent(ChartExplorerActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        confirmBackDialogFragment.restoreListeners(this, "back");

        CryptoChart cryptoChart = getIntent().getParcelableExtra("CryptoChart");
        cryptoChartArrayList.add(cryptoChart);
        if(savedInstanceState == null) {
            HashMapUtil.putValueInMap(StateObj.chartDataMap, cryptoChart, ChartData.getNoData(cryptoChart));
        }

        Toolbar toolbar = findViewById(R.id.chart_explorer_toolbar);
        setSupportActionBar(toolbar);

        ImageButton helpButton = findViewById(R.id.chart_explorer_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ChartExplorerActivity.this, R.raw.help_chart_explorer);
            }
        });

        chartHolderView = findViewById(R.id.chart_explorer_chartHolderView);

        FloatingActionButton fab_info = findViewById(R.id.chart_explorer_infoButton);
        fab_info.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ChartInfoDialog.class, cryptoChartArrayList).show(ChartExplorerActivity.this, "info");
            }
        });

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Downloading Chart Data...");

                CryptoChart cryptoChart = cryptoChartArrayList.get(0);

                ChartData newChartData;
                if(includePricePoints.get(0) && includeCandles.get(0)) {
                    newChartData = ChartData.getAllData(cryptoChart);
                }
                else if(includePricePoints.get(0)) {
                    newChartData = ChartData.getPricePointsData(cryptoChart);
                }
                else if(includeCandles.get(0)) {
                    newChartData = ChartData.getCandlesData(cryptoChart);
                }
                else {
                    newChartData = ChartData.getNoData(cryptoChart);
                }

                ProgressDialogFragment.setValue(DataBridge.serialize(newChartData, ChartData.class));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                ChartData newChartData = DataBridge.deserialize(ProgressDialogFragment.getValue(), ChartData.class);

                boolean isComplete;
                if(includePricePoints.get(0) && includeCandles.get(0)) {
                    isComplete = newChartData.isComplete();
                }
                else if(includePricePoints.get(0)) {
                    isComplete = newChartData.isPricePointsComplete();
                }
                else if(includeCandles.get(0)) {
                    isComplete = newChartData.isCandlesComplete();
                }
                else {
                    isComplete = true;
                }

                if(!isComplete) {
                    ToastUtil.showToast("incomplete_chart_data");
                }

                CryptoChart cryptoChart = cryptoChartArrayList.get(0);
                ChartData oldChartData = HashMapUtil.getValueFromMap(StateObj.chartDataMap, cryptoChart);
                ChartData mergedChartData = ChartData.merge(oldChartData, newChartData);
                HashMapUtil.putValueInMap(StateObj.chartDataMap, cryptoChart, mergedChartData);

                updateLayout();

                ToastUtil.showToast("chart_data_downloaded");

                // Update the time, regardless of how the update was started or ended up.
                lastUpdateTime = System.currentTimeMillis();
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        autoUpdateButton = findViewById(R.id.chart_explorer_autoUpdateButton);
        autoUpdateButton.setOptions("Auto Update Off", "Auto Update On");

        refreshButton = findViewById(R.id.chart_explorer_refreshButton);
        refreshButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                // Don't ask user - just download all types of data.
                includePricePoints = new ArrayList<>();
                includePricePoints.add(true);

                includeCandles = new ArrayList<>();
                includeCandles.add(true);

                progressDialogFragment.show(ChartExplorerActivity.this, "progress");
            }
        });

        updateLayout();

        // On first creation, download chart data.
        if(savedInstanceState == null) {
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
        chartHolderView.updateChartsFromChartDataArray(new ArrayList<>(StateObj.chartDataMap.values()));
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
            BaseDialogFragment.newInstance(CryptoPricesDialog.class).show(ChartExplorerActivity.this, "price");
            return true;
        }
        else if (id == 2) {
            BaseDialogFragment.newInstance(CryptoConverterDialog.class).show(ChartExplorerActivity.this, "converter");
            return true;
        }
        else if (id == 3) {
            runWithProgressIndicator(new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    StateObj.chartInfo = chartHolderView.getInfo();
                }
            }, new CrashRunnable(this) {
                @Override
                public void runImpl() {
                    BaseDialogFragment.newInstance(ReportFeedbackDialog.class, "Chart").show(getCurrentActivity(), "feedback");
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
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            includePricePoints = (ArrayList<Boolean>)bundle.getSerializable("includePricePoints");
            includeCandles = (ArrayList<Boolean>)bundle.getSerializable("includeCandles");
            lastUpdateTime = bundle.getLong("lastUpdateTime");
        }
    }
}