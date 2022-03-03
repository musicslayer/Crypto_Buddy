package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.ChartPortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.util.DataDumpUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.util.ScreenshotUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class ReportFeedbackDialog extends BaseDialog {
    String type;

    public ReportFeedbackDialog(Activity activity, String type) {
        super(activity);
        this.type = type;
    }

    public int getBaseViewID() {
        return R.id.report_feedback_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_report_feedback);

        // Always visible
        CheckBox checkBox_screenshot = findViewById(R.id.report_feedback_dialog_screenshotCheckBox);
        CheckBox checkBox_install = findViewById(R.id.report_feedback_dialog_installCheckBox);

        CheckBox checkBox_info = findViewById(R.id.report_feedback_dialog_infoCheckBox);
        checkBox_info.setVisibility("None".equals(type) ? View.GONE : View.VISIBLE);

        if("Transaction".equals(type)) {
            checkBox_info.setText("Attach transaction information.");
        }
        else if("TransactionPortfolio".equals(type)) {
            checkBox_info.setText("Attach transaction portfolio information.");
        }
        else if("Address".equals(type)) {
            checkBox_info.setText("Attach address information.");
        }
        else if("AddressPortfolio".equals(type)) {
            checkBox_info.setText("Attach address portfolio information.");
        }
        else if("Exchange".equals(type)) {
            checkBox_info.setText("Attach exchange information.");
        }
        else if("ExchangePortfolio".equals(type)) {
            checkBox_info.setText("Attach exchange portfolio information.");
        }
        else if("Chart".equals(type)) {
            checkBox_info.setText("Attach chart information.");
        }
        else if("ChartPortfolio".equals(type)) {
            checkBox_info.setText("Attach chart portfolio information.");
        }

        Button B_EMAIL = findViewById(R.id.report_feedback_dialog_button);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // Attempt to create files to attach based on chosen options. If any cannot be created, proceed but show user a toast.
                ArrayList<File> fileArrayList = new ArrayList<>();
                boolean isComplete = true;

                if(checkBox_screenshot.isChecked()) {
                    try {
                        fileArrayList.add(ScreenshotUtil.writeScreenshotFile(activity));
                        fileArrayList.add(ScreenshotUtil.writeSurfaceFile(activity));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(checkBox_install.isChecked()) {
                    try {
                        fileArrayList.add(FileUtil.writeTempFile(DataDumpUtil.getAllData(activity)));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(checkBox_info.isChecked()) {
                    try {
                        fileArrayList.add(FileUtil.writeTempFile(getInfo()));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(!isComplete) {
                    ToastUtil.showToast("cannot_attach");
                }

                MessageUtil.sendEmail(activity, "musicslayer@gmail.com", "Crypto Buddy - Report Feedback", "Selected information is attached.\n\nFeel free to add any other information below:\n\n", fileArrayList);
            }
        });
    }

    public String getInfo() {
        StringBuilder s = new StringBuilder();

        long timestamp = new Date().getTime();
        s.append("Timestamp: ").append(timestamp);

        if("Transaction".equals(type)) {
            s.append("\n\nTransactionExplorerActivity");
            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("TransactionPortfolio".equals(type)) {
            s.append("\n\nTransactionPortfolioExplorerActivity");
            s.append("\n\n").append("Transaction Portfolio:\n\n").append(DataBridge.serialize(StateObj.transactionPortfolioObj, TransactionPortfolioObj.class));
            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("Address".equals(type)) {
            s.append("\n\nAddressExplorerActivity");

            // Full info for the address.
            s.append("\n\nAddress Info:");
            s.append("\n\n").append(AddressData.getRawFullInfoString(new ArrayList<>(StateObj.addressDataMap.values())));

            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("AddressPortfolio".equals(type)) {
            s.append("\n\nAddressPortfolioExplorerActivity");

            // Full info for all addresses.
            s.append("\n\nAddress Info Full:");
            s.append("\n\n").append(AddressData.getRawFullInfoString(new ArrayList<>(StateObj.addressDataMap.values())));

            s.append("\n\nAddress Info Filtered:");
            s.append("\n\n").append(AddressData.getRawFullInfoString(new ArrayList<>(StateObj.addressDataFilterMap.values())));

            s.append("\n\nAddress Filter:\n\n").append(StateObj.filterInfo);
            s.append("\n\nAddress Portfolio:\n\n").append(DataBridge.serialize(StateObj.addressPortfolioObj, AddressPortfolioObj.class));
            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("Exchange".equals(type)) {
            s.append("\n\nExchangeExplorerActivity");

            // Full info for the exchange.
            s.append("\n\nExchange Info:");
            s.append("\n\n").append(ExchangeData.getRawFullInfoString(new ArrayList<>(StateObj.exchangeDataMap.values())));

            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("ExchangePortfolio".equals(type)) {
            s.append("\n\nExchangePortfolioExplorerActivity");

            // Full info for all exchanges.
            s.append("\n\nExchange Info Full:");
            s.append("\n\n").append(ExchangeData.getRawFullInfoString(new ArrayList<>(StateObj.exchangeDataMap.values())));

            s.append("\n\nExchange Info Filtered:");
            s.append("\n\n").append(ExchangeData.getRawFullInfoString(new ArrayList<>(StateObj.exchangeDataFilterMap.values())));

            s.append("\n\nExchange Filter:\n\n").append(StateObj.filterInfo);
            s.append("\n\nExchange Portfolio:\n\n").append(DataBridge.serialize(StateObj.exchangePortfolioObj, ExchangePortfolioObj.class));
            s.append("\n\n").append(StateObj.tableInfo);
        }
        else if("Chart".equals(type)) {
            s.append("\n\nChartExplorerActivity");

            // Full info for the chart.
            s.append("\n\nChart Info:");
            s.append("\n\n").append(ChartData.getRawFullInfoString(new ArrayList<>(StateObj.chartDataMap.values())));

            s.append("\n\n").append(StateObj.chartInfo);
        }
        else if("ChartPortfolio".equals(type)) {
            s.append("\n\nChartPortfolioExplorerActivity");

            // Full info for all charts.
            s.append("\n\nChart Info Full:");
            s.append("\n\n").append(ChartData.getRawFullInfoString(new ArrayList<>(StateObj.chartDataMap.values())));

            s.append("\n\nChart Info Filtered:");
            s.append("\n\n").append(ChartData.getRawFullInfoString(new ArrayList<>(StateObj.chartDataFilterMap.values())));

            s.append("\n\nChart Filter:\n\n").append(StateObj.filterInfo);
            s.append("\n\nChart Portfolio:\n\n").append(DataBridge.serialize(StateObj.chartPortfolioObj, ChartPortfolioObj.class));
            s.append("\n\n").append(StateObj.chartInfo);
        }
        else {
            s.append("\n\nNo Available Information.");
        }

        return s.toString();
    }
}