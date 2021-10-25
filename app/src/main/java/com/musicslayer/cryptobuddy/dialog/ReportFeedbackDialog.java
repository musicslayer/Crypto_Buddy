package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.musicslayer.cryptobuddy.activity.AddressExplorerActivity;
import com.musicslayer.cryptobuddy.activity.AddressPortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.activity.TransactionExplorerActivity;
import com.musicslayer.cryptobuddy.activity.TransactionPortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.DataDumpUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.MessageUtil;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.util.ScreenshotUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.table.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportFeedbackDialog extends BaseDialog {
    String type;
    String info;

    // Choose the constructor based on the specific information that may be attached, if any.
    //public ReportFeedbackDialog(Activity activity, String type, String info) {
    public ReportFeedbackDialog(Activity activity, String type) {
        super(activity);
        this.type = type;
        //this.info = info;
    }

    public int getBaseViewID() {
        return R.id.report_feedback_dialog;
    }

    public void createLayout() {
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

                MessageUtil.sendEmail(ReportFeedbackDialog.this.activity, "musicslayer@gmail.com", "Crypto Buddy - Bug Report/Feedback", "Selected information is attached.\n\nFeel free to add any other information below:\n\n", fileArrayList);
            }
        });
    }

    public String getInfo() {
        Table table = Table.tableStateObj[0].table;

        if(activity instanceof TransactionExplorerActivity) {
            return table.getInfo();
        }
        else if(activity instanceof TransactionPortfolioExplorerActivity) {
            TransactionPortfolioObj transactionPortfolioObj = ((TransactionPortfolioExplorerActivity)activity).activityStateObj[0].transactionPortfolioObj;
            return "Transaction Portfolio:\n\n" + Serialization.serialize(transactionPortfolioObj) + "\n\n" + table.getInfo();
        }
        else if(activity instanceof AddressExplorerActivity) {
            HashMap<CryptoAddress, AddressData> addressDataMap = ((AddressExplorerActivity)activity).activityStateObj[0].addressDataMap;

            StringBuilder s = new StringBuilder();
            for(AddressData addressData : new ArrayList<>(addressDataMap.values())) {
                s.append(addressData.getInfoString()).append("\n\n");
            }

            return "Address Info:\n\n" + s.toString() + table.getInfo();
        }
        else if(activity instanceof AddressPortfolioExplorerActivity) {
            HashMap<CryptoAddress, AddressData> addressDataMap = ((AddressPortfolioExplorerActivity)activity).activityStateObj[0].addressDataMap;
            AddressPortfolioObj addressPortfolioObj = ((AddressPortfolioExplorerActivity)activity).activityStateObj[0].addressPortfolioObj;

            StringBuilder s = new StringBuilder();
            for(AddressData addressData : new ArrayList<>(addressDataMap.values())) {
                s.append(addressData.getInfoString()).append("\n\n");
            }

            return "Address Info:\n\n" + s.toString() + "Address Portfolio:\n\n" + Serialization.serialize(addressPortfolioObj) + "\n\n" + table.getInfo();
        }

        return "?";
    }
}