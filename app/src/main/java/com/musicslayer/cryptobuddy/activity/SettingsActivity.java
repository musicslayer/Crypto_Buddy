package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.settings.category.SettingsCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        LinearLayout L = findViewById(R.id.settings_linearLayout);

        ArrayList<SettingsCategory> settingsCategoryArrayList = SettingsCategory.setting_categories;
        ArrayList<String> settingsCategoryDisplayNames = SettingsCategory.settings_category_display_names;

        BorderedSpinnerView bsv = findViewById(R.id.settings_category_spinner);
        bsv.setOptions(settingsCategoryDisplayNames);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                L.removeAllViews();

                for(Setting setting : settingsCategoryArrayList.get(pos).getSettings()) {
                    L.addView(setting.createSettingView(SettingsActivity.this));
                }
            }
        });
    }
}