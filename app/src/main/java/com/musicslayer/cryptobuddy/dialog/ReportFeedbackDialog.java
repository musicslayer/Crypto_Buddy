package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.musicslayer.cryptobuddy.activity.AddressExplorerActivity;
import com.musicslayer.cryptobuddy.activity.AddressPortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.activity.ExchangeExplorerActivity;
import com.musicslayer.cryptobuddy.activity.ExchangePortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.activity.TransactionExplorerActivity;
import com.musicslayer.cryptobuddy.activity.TransactionPortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.serialize.Serialization;
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
import java.util.HashMap;

public class ReportFeedbackDialog extends BaseDialog {
    String type;
    String tableInfo; // May be null for Activities without a table.

    public ReportFeedbackDialog(Activity activity, String type) {
        super(activity);
        this.type = type;

        // Although this may not be needed, we must calculate this already because we do not have access to the table here.
        this.tableInfo = StateObj.tableInfo;
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

        Button B_EMAIL = findViewById(R.id.report_feedback_dialog_button);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // Attempt to create files to attach based on chosen options. If any cannot be created, proceed but show user a toast.
                ArrayList<File> fileArrayList = new ArrayList<>();
                boolean isComplete = true;

                if(checkBox_screenshot.isChecked()) {
                    try {
                        fileArrayList.add(ScreenshotUtil.writeScreenshotFile(activity));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(checkBox_install.isChecked()) {
                    try {
                        fileArrayList.add(FileUtil.writeFile(activity, DataDumpUtil.getAllData(activity)));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(checkBox_info.isChecked()) {
                    try {
                        fileArrayList.add(FileUtil.writeFile(activity, getInfo()));
                    }
                    catch(Exception e) {
                        ThrowableUtil.processThrowable(e);
                        isComplete = false;
                    }
                }

                if(!isComplete) {
                    ToastUtil.showToast(activity,"cannot_attach");
                }

                MessageUtil.sendEmail(ReportFeedbackDialog.this.activity, "musicslayer@gmail.com", "Crypto Buddy - Report Feedback", "Selected information is attached.\n\nFeel free to add any other information below:\n\n", fileArrayList);
            }
        });
    }

    public String getInfo() {
        long timestamp = new Date().getTime();

        if(activity instanceof TransactionExplorerActivity) {
            return "Timestamp: " + timestamp + "\n\nTransactionExplorerActivity:\n\n" + tableInfo;
        }
        else if(activity instanceof TransactionPortfolioExplorerActivity) {
            TransactionPortfolioObj transactionPortfolioObj = StateObj.transactionPortfolioObj;
            return "Timestamp: " + timestamp + "\n\nTransactionPortfolioExplorerActivity\n\n" + "Transaction Portfolio:\n\n" + Serialization.serialize(transactionPortfolioObj) + "\n\n" + tableInfo;
        }
        else if(activity instanceof AddressExplorerActivity) {
            HashMap<CryptoAddress, AddressData> addressDataMap = StateObj.addressDataMap;

            // Full info for the address.
            StringBuilder s = new StringBuilder();
            for(AddressData addressData : new ArrayList<>(addressDataMap.values())) {
                s.append(addressData.getFullInfoString()).append("\n\n");
            }

            return "Timestamp: " + timestamp + "\n\nAddressExplorerActivity\n\n" + "Address Info:\n\n" + s.toString() + tableInfo;
        }
        else if(activity instanceof AddressPortfolioExplorerActivity) {
            HashMap<CryptoAddress, AddressData> addressDataMap = StateObj.addressDataMap;
            HashMap<CryptoAddress, AddressData> addressDataFilterMap = StateObj.addressDataFilterMap;
            AddressPortfolioObj addressPortfolioObj = StateObj.addressPortfolioObj;

            // Full info for all addresses.
            StringBuilder s = new StringBuilder();
            for(AddressData addressData : new ArrayList<>(addressDataMap.values())) {
                s.append(addressData.getFullInfoString()).append("\n\n");
            }

            // Get the names of the addresses in the filter map.
            StringBuilder sf = new StringBuilder();
            for(AddressData addressData : new ArrayList<>(addressDataFilterMap.values())) {
                sf.append(addressData.cryptoAddress).append("\n\n");
            }

            return "Timestamp: " + timestamp + "\n\nAddressPortfolioExplorerActivity\n\n" + "Address Info:\n\n" + s.toString() + "Address Filter:\n\n" + sf.toString() + "Address Portfolio:\n\n" + Serialization.serialize(addressPortfolioObj) + "\n\n" + tableInfo;
        }
        else if(activity instanceof ExchangeExplorerActivity) {
            HashMap<CryptoExchange, ExchangeData> exchangeDataMap = StateObj.exchangeDataMap;

            // Full info for the exchange.
            StringBuilder s = new StringBuilder();
            for(ExchangeData exchangeData : new ArrayList<>(exchangeDataMap.values())) {
                s.append(exchangeData.getFullInfoString()).append("\n\n");
            }

            // Authorization info of every exchange, whether or not any data was downloaded with it.
            StringBuilder sa = new StringBuilder();
            for(ExchangeData exchangeData : new ArrayList<>(exchangeDataMap.values())) {
                CryptoExchange cryptoExchange = exchangeData.cryptoExchange;
                sa.append(cryptoExchange.getInfo()).append("\n\n");
            }

            return "Timestamp: " + timestamp + "\n\nExchangeExplorerActivity\n\n" + "Exchange Info:\n\n" + s.toString() + "Exchange Authorization:\n\n" + sa.toString() + tableInfo;
        }

        else if(activity instanceof ExchangePortfolioExplorerActivity) {
            HashMap<CryptoExchange, ExchangeData> exchangeDataMap = StateObj.exchangeDataMap;
            HashMap<CryptoExchange, ExchangeData> exchangeDataFilterMap = StateObj.exchangeDataFilterMap;
            ExchangePortfolioObj exchangePortfolioObj = StateObj.exchangePortfolioObj;

            // Full info for all exchanges.
            StringBuilder s = new StringBuilder();
            for(ExchangeData exchangeData : new ArrayList<>(exchangeDataMap.values())) {
                s.append(exchangeData.getFullInfoString()).append("\n\n");
            }

            // Authorization info of every exchange, whether or not any data was downloaded with it.
            StringBuilder sa = new StringBuilder();
            for(ExchangeData exchangeData : new ArrayList<>(exchangeDataMap.values())) {
                CryptoExchange cryptoExchange = exchangeData.cryptoExchange;
                sa.append(cryptoExchange.getInfo()).append("\n\n");
            }

            // Get the names of the exchanges in the filter map.
            StringBuilder sf = new StringBuilder();
            for(ExchangeData exchangeData : new ArrayList<>(exchangeDataFilterMap.values())) {
                sf.append(exchangeData.cryptoExchange).append("\n\n");
            }

            return "Timestamp: " + timestamp + "\n\nExchangePortfolioExplorerActivity\n\n" + "Exchange Info:\n\n" + s.toString() + "Exchange Authorization:\n\n" + sa.toString() + "Exchange Filter:\n\n" + sf.toString() + "Exchange Portfolio:\n\n" + Serialization.serialize(exchangePortfolioObj) + "\n\n" + tableInfo;
        }

        return "?";
    }
}