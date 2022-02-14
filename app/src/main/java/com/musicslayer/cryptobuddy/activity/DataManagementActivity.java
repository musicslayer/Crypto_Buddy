package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ExportDataDialog;
import com.musicslayer.cryptobuddy.dialog.ImportDataDialog;
import com.musicslayer.cryptobuddy.util.FileUtil;

public class DataManagementActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return R.id.data_management_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_data_management);

        TextView T_FOLDER = findViewById(R.id.data_management_exportFolderTextView);
        T_FOLDER.setText("Export Folder:\n" + FileUtil.getExternalFolderPath(this, ExportDataDialog.EXPORT_FOLDER));

        Button B_EXPORT = findViewById(R.id.data_management_exportButton);
        B_EXPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ExportDataDialog.class).show(DataManagementActivity.this, "export");
            }
        });

        Button B_IMPORT = findViewById(R.id.data_management_importButton);
        B_IMPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                BaseDialogFragment.newInstance(ImportDataDialog.class).show(DataManagementActivity.this, "import");
            }
        });

        Button B_EMAIL = findViewById(R.id.data_management_emailButton);
        B_EMAIL.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
            }
        });

        Button B_CLIPBOARD = findViewById(R.id.data_management_clipboardButton);
        B_CLIPBOARD.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
            }
        });

        updateLayout();
    }

    public void updateLayout() {
    }
}