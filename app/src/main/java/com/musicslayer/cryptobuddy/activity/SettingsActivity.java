package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.settings.category.SettingCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class SettingsActivity extends BaseActivity {
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        LinearLayout L = findViewById(R.id.settings_linearLayout);

        BorderedSpinnerView bsv = findViewById(R.id.settings_category_spinner);
        bsv.setOptions(SettingCategory.settings_category_display_names);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                L.removeAllViews();

                SettingCategory settingCategory = SettingCategory.setting_categories.get(pos);
                for(Setting setting : settingCategory.settingArrayList) {
                    L.addView(setting.createSettingView(SettingsActivity.this));
                }
            }
        });
    }
}