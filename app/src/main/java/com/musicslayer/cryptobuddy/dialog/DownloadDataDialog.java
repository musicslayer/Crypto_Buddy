package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashView;

import java.util.ArrayList;

public class DownloadDataDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;

    CheckBox[] C_B;
    CheckBox[] C_T;

    public ArrayList<Boolean> user_BALANCES = new ArrayList<>();
    public ArrayList<Boolean> user_TRANSACTIONS = new ArrayList<>();

    public DownloadDataDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
    }

    public int getBaseViewID() {
        return R.id.download_data_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_download_data);

        Button B_DOWNLOAD = findViewById(R.id.download_data_dialog_downloadButton);
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    user_BALANCES.add(C_B[i].isChecked());
                    user_TRANSACTIONS.add(C_T[i].isChecked());
                }

                isComplete = true;
                dismiss();
            }
        });

        Button B_SELECTALL = findViewById(R.id.download_data_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    C_B[i].setChecked(true);
                    C_T[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.download_data_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    C_B[i].setChecked(false);
                    C_T[i].setChecked(false);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.download_data_dialog_checkBoxLayout);

        if(C_B == null && C_T == null) {
            // First time displaying dialog.
            C_B = new CheckBox[cryptoAddressArrayList.size()];
            C_T = new CheckBox[cryptoAddressArrayList.size()];

            for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                TextView T = new TextView(this.activity);
                T.setText(cryptoAddressArrayList.get(i).toString());

                LinearLayout L_ROW = new LinearLayout(activity);
                L_ROW.setPadding(0, 0, 0, 50);

                C_B[i] = new CheckBox(this.activity);
                C_B[i].setChecked(true);
                C_B[i].setText("Balances");

                C_T[i] = new CheckBox(this.activity);
                C_T[i].setChecked(true);
                C_T[i].setText("Transactions");

                L_ROW.addView(C_B[i]);
                L_ROW.addView(C_T[i]);

                L.addView(T);
                L.addView(L_ROW);
            }
        }
        else {
            // After onRestore
            for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                TextView T = new TextView(this.activity);
                T.setText(cryptoAddressArrayList.get(i).toString());

                LinearLayout L_ROW = new LinearLayout(activity);
                L_ROW.setPadding(0, 0, 0, 50);

                L_ROW.addView(C_B[i]);
                L_ROW.addView(C_T[i]);

                L.addView(T);
                L.addView(L_ROW);
            }
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
            bundle.putBoolean("checkbox_b" + i, C_B[i].isChecked());
            bundle.putBoolean("checkbox_t" + i, C_T[i].isChecked());
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            C_B = new CheckBox[cryptoAddressArrayList.size()];
            C_T = new CheckBox[cryptoAddressArrayList.size()];
            for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                C_B[i] = new CheckBox(this.activity);
                C_B[i].setChecked(bundle.getBoolean("checkbox_b" + i));
                C_B[i].setText("Balances");

                C_T[i] = new CheckBox(this.activity);
                C_T[i].setChecked(bundle.getBoolean("checkbox_t" + i));
                C_T[i].setText("Transactions");
            }
        }
    }
}
