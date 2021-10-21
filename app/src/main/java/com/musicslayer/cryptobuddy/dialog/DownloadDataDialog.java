package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;

public class DownloadDataDialog extends BaseDialog {
    public boolean user_BALANCES;
    public boolean user_TRANSACTIONS;

    public DownloadDataDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.download_data_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_download_data);

        // Start with everything checked.
        CheckBox C_BALANCES = findViewById(R.id.download_data_dialog_balancesCheckBox);
        CheckBox C_TRANSACTIONS = findViewById(R.id.download_data_dialog_transactionsCheckBox);

        C_BALANCES.setChecked(true);
        C_TRANSACTIONS.setChecked(true);

        Button B_DOWNLOAD = findViewById(R.id.download_data_dialog_downloadButton);
        B_DOWNLOAD.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                user_BALANCES = C_BALANCES.isChecked();
                user_TRANSACTIONS = C_TRANSACTIONS.isChecked();

                isComplete = true;
                dismiss();
            }
        });
    }
}
