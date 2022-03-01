package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

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
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class ChartExplorerActivity extends BaseActivity {
    public BaseDialogFragment confirmBackDialogFragment;

    ArrayList<CryptoChart> cryptoChartArrayList = new ArrayList<>();

    public ArrayList<Boolean> includePricePoints;
    public ArrayList<Boolean> includeCandles;

    public boolean isAutoUpdate;

    FloatingActionButton refreshButton;

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
            }
        });
        progressDialogFragment.restoreListeners(this, "progress");

        AppCompatButton autoUpdateButton = findViewById(R.id.chart_explorer_autoUpdateButton);
        autoUpdateButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                isAutoUpdate = !isAutoUpdate;
                updateAutoUpdateButton();
            }
        });

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

        updateAutoUpdateButton();
        updateLayout();

        // On first creation, try to get data to display chart.
        if(savedInstanceState == null) {
            doChartUpdate();
        }
    }

    public void updateAutoUpdateButton() {
        AppCompatButton autoUpdateButton = findViewById(R.id.chart_explorer_autoUpdateButton);
        if(isAutoUpdate) {
            autoUpdateButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_on_24, 0, 0, 0);
            autoUpdateButton.setText("Auto Update On");
        }
        else {
            autoUpdateButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_off_24, 0, 0, 0);
            autoUpdateButton.setText("Auto Update Off");
        }
    }

    public void doChartUpdate() {
        refreshButton.callOnClick();
    }

    public void updateLayout() {
        ChartHolderView chartHolderView = findViewById(R.id.chart_explorer_chartHolderView);
        chartHolderView.reset();
        chartHolderView.addChartsFromChartDataArray(new ArrayList<>(StateObj.chartDataMap.values()));
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
                    //StateObj.chartInfo = // get chart info...
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

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        super.onSaveInstanceStateImpl(bundle);
        bundle.putSerializable("includePricePoints", includePricePoints);
        bundle.putSerializable("includeCandles", includeCandles);
        bundle.putBoolean("isAutoUpdate", isAutoUpdate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        super.onRestoreInstanceStateImpl(bundle);
        if(bundle != null) {
            includePricePoints = (ArrayList<Boolean>)bundle.getSerializable("includePricePoints");
            includeCandles = (ArrayList<Boolean>)bundle.getSerializable("includeCandles");
            isAutoUpdate = bundle.getBoolean("isAutoUpdate");
        }
    }
}