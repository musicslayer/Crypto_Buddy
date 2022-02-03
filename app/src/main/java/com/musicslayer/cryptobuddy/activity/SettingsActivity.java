package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.settings.category.SettingsCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.settings.SettingsView;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsActivity extends BaseActivity {
    public HashMap<String, ArrayList<SettingsView>> settingsMap;

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

        // Populate this map so that, if needed, any SettingsView can restore its state.
        settingsMap = new HashMap<>();
        for(SettingsCategory settingsCategory : settingsCategoryArrayList) {
            ArrayList<SettingsView> settingsViewArrayList = new ArrayList<>();
            for(Setting setting : settingsCategory.getSettings()) {
                SettingsView settingsView = setting.createSettingView(SettingsActivity.this);
                settingsViewArrayList.add(settingsView);
            }
            HashMapUtil.putValueInMap(settingsMap, settingsCategory.getName(), settingsViewArrayList);
        }

        BorderedSpinnerView bsv = findViewById(R.id.settings_category_spinner);
        bsv.setOptions(settingsCategoryDisplayNames);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                L.removeAllViews();

                ArrayList<SettingsView> settingsViewArrayList = HashMapUtil.getValueFromMap(settingsMap, settingsCategoryArrayList.get(pos).getName());
                for(SettingsView settingsView : settingsViewArrayList) {
                    L.addView(settingsView);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        ArrayList<String> categoryNameKeys = new ArrayList<>(settingsMap.keySet());
        for(int i = 0; i < categoryNameKeys.size(); i++) {
            String categoryName = categoryNameKeys.get(i);
            ArrayList<SettingsView> settingsViewArrayList = HashMapUtil.getValueFromMap(settingsMap, categoryName);
            for(int j = 0; j < settingsViewArrayList.size(); j++) {
                SettingsView settingsView = settingsViewArrayList.get(j);
                bundle.putParcelable("settingsView_" + i + "_" + j, settingsView.onSaveInstanceState());
            }
        }
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            ArrayList<String> categoryNameKeys = new ArrayList<>(settingsMap.keySet());
            for(int i = 0; i < categoryNameKeys.size(); i++) {
                String categoryName = categoryNameKeys.get(i);
                ArrayList<SettingsView> settingsViewArrayList = HashMapUtil.getValueFromMap(settingsMap, categoryName);
                for(int j = 0; j < settingsViewArrayList.size(); j++) {
                    SettingsView settingsView = settingsViewArrayList.get(j);
                    settingsView.onRestoreInstanceState(bundle.getParcelable("settingsView_" + i + "_" + j));
                }
            }
        }
    }
}