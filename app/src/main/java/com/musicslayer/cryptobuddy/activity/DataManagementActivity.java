package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;

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

        TextView T_FOLDER = findViewById(R.id.data_management_folderTextView);

        String state = Environment.getExternalStorageState();
        String folderString;

        // TODO Allow user to select which external folder?

        // Require both read and write access.
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            folderString = Environment.getExternalStorageDirectory().getAbsolutePath();
            T_FOLDER.setText(folderString);
        }
        else {
            folderString = RichStringBuilder.redText("Problem Accessing External Storage.");
            T_FOLDER.setText(Html.fromHtml(folderString));
        }

        Button B_EXPORT = findViewById(R.id.data_management_exportButton);
        B_EXPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
            }
        });

        Button B_IMPORT = findViewById(R.id.data_management_importButton);
        B_IMPORT.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
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